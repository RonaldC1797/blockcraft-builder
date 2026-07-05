// ui/home/MainActivity.kt
package com.cacuango.blockcraft.builder.ui.home

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cacuango.blockcraft.builder.R
import com.cacuango.blockcraft.builder.ui.auth.LoginActivity
import com.cacuango.blockcraft.builder.ui.create.CreateProjectActivity
import com.cacuango.blockcraft.builder.ui.load.LoadWorldActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import android.Manifest
import com.cacuango.blockcraft.builder.data.local.entity.Proyecto
import com.cacuango.blockcraft.builder.ui.load.MundoAdapter
import com.cacuango.blockcraft.builder.viewmodel.ProyectoViewModel
import com.cacuango.blockcraft_builder.workers.RecordatorioWorker
import androidx.lifecycle.ViewModelProvider
import com.cacuango.blockcraft.builder.ui.editor.EditorActivity


class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var viewModel: ProyectoViewModel
    private lateinit var adapter: MundoAdapter

    // Vistas
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvAvatar: TextView
    private lateinit var tvBlocksCount: TextView
    private lateinit var tvHoursCount: TextView
    private lateinit var btnLogout: ImageButton
    private lateinit var btnNotifications: ImageButton
    private lateinit var btnNewWorld: Button
    private lateinit var btnCargarMundo: Button
    private lateinit var tvVerTodos: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var recyclerViewMundos: RecyclerView
    private lateinit var llEmptyState: LinearLayout  // ✅ CORRECTO: llEmptyState (dos L)

    // ✅ AGREGAR: Launcher para solicitar el permiso
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Usuario aceptó — las notificaciones funcionarán
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Notificaciones activadas",
                    Snackbar.LENGTH_SHORT
                ).show()
            } else {
                // Usuario rechazó — informar qué pierde
                manejarPermisoRechazado()
            }
        }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        // Si no hay sesión activa, ir al login
        if (auth.currentUser == null) {
            goToLogin()
            return
        }

        solicitarPermisoNotificaciones()
        initViews()
        setupUI()
        showUserInfo()
        setupListeners()
        setupTabs()
        setupBottomNav()
    }

    //✅ AGREGAR: Función que verifica y solicita el permiso
    private fun solicitarPermisoNotificaciones() {
        // Solo necesario en Android 13 (API 33) en adelante
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                // Caso 1: Ya tiene el permiso — no hacer nada
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permiso ya concedido, notificaciones listas
                }

                // Caso 2: El usuario rechazó antes — mostrar explicación
                shouldShowRequestPermissionRationale(
                    Manifest.permission.POST_NOTIFICATIONS
                ) -> {
                    mostrarExplicacionPermiso()
                }

                // Caso 3: Primera vez — solicitar directamente
                else -> {
                    requestPermissionLauncher.launch(
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                }
            }
        }
        // En Android 12 o menor no se necesita pedir permiso
    }

    // ✅ AGREGAR: Explicación antes de pedir el permiso (buena práctica UX)
    private fun mostrarExplicacionPermiso() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Activar notificaciones")
            .setMessage(
                "Blockcraft Builder te notificará cuando:\n\n" +
                        "• Tu construcción lleve 3 días sin guardarse\n" +
                        "• Una exportación de imagen esté lista\n" +
                        "• Alcances un nuevo logro de bloques"
            )
            .setPositiveButton("Activar") { _, _ ->
                requestPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
            .setNegativeButton("Ahora no") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    // ✅ AGREGAR: Manejo cuando el usuario rechaza el permiso
    private fun manejarPermisoRechazado() {
        Snackbar.make(
            findViewById(android.R.id.content),
            "Sin notificaciones no podrás recibir recordatorios de tus construcciones",
            Snackbar.LENGTH_LONG
        ).setAction("Configurar") {
            // Llevar al usuario a configuración del sistema
            val intent = android.content.Intent(
                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            ).apply {
                data = android.net.Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
        }.show()
    }



    private fun initViews() {
        // ✅ TODOS LOS IDs CORRECTOS
        tvUserName = findViewById(R.id.tvUserName)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        tvAvatar = findViewById(R.id.tvAvatar)
        tvBlocksCount = findViewById(R.id.tvBlocksCount)
        tvHoursCount = findViewById(R.id.tvHoursCount)
        btnLogout = findViewById(R.id.btnLogout)
        btnNotifications = findViewById(R.id.btnNotifications)
        btnNewWorld = findViewById(R.id.btnNewWorld)
        btnCargarMundo = findViewById(R.id.btnCargarMundo)
        tvVerTodos = findViewById(R.id.tvVerTodos)
        tabLayout = findViewById(R.id.tabLayout)
        bottomNav = findViewById(R.id.bottomNav)
        recyclerViewMundos = findViewById(R.id.recyclerViewMundos)
        llEmptyState = findViewById(R.id.llEmptyState)  // ✅ CORRECTO
    }

    private fun setupUI() {
        // Crear adapter con sus callbacks
        adapter = MundoAdapter(
            onItemClick = { proyecto ->
                // Abre formulario de edición (igual que antes)
                val intent = Intent(this, CreateProjectActivity::class.java).apply {
                    putExtra("PROYECTO_ID", proyecto.id)
                    putExtra("NOMBRE_PROYECTO", proyecto.nombre)
                    putExtra("CATEGORIA_PROYECTO", proyecto.categoria)
                }
                startActivity(intent)
            },
            onDeleteClick = { proyecto -> mostrarDialogoConfirmacion(proyecto) },
            onCargarClick = { proyecto ->
                // ✅ Flecha va directo al editor
                val intent = Intent(this, EditorActivity::class.java).apply {
                    putExtra("PROYECTO_ID", proyecto.id)
                    putExtra("NOMBRE_PROYECTO", proyecto.nombre)
                }
                startActivity(intent)
            }
        )

        // Conectar al RecyclerView
        recyclerViewMundos.layoutManager = LinearLayoutManager(this)
        recyclerViewMundos.adapter = adapter

        // Inicializar ViewModel
        viewModel = ViewModelProvider(this)[ProyectoViewModel::class.java]

        // Observar LiveData — actualiza la lista automáticamente
        viewModel.proyectosLiveData.observe(this) { proyectos ->
            if (proyectos.isNullOrEmpty()) {
                llEmptyState.visibility = View.VISIBLE
                recyclerViewMundos.visibility = View.GONE
            } else {
                llEmptyState.visibility = View.GONE
                recyclerViewMundos.visibility = View.VISIBLE
                adapter.actualizarLista(proyectos)
                tvHoursCount.text = "${proyectos.size * 2}h"  // ← AGREGAR
                viewModel.cargarEstadisticas()                  // ← AGREGAR
            }
        }


        // ✅ Observer de bloques totales
        viewModel.totalBloques.observe(this) { total ->
            tvBlocksCount.text = if (total >= 1000) {
                String.format("%.1fk", total / 1000f)
            } else {
                total.toString()
            }
        }
    }

    private fun showUserInfo() {
        try {
            val user = auth.currentUser
            if (user != null) {
                val nombre = user.displayName ?: "Constructor "
                tvUserName.text = "¡Hola, $nombre!"
                tvUserEmail.text = "Blockcraft Builder"
                tvAvatar.text = if (nombre.isNotEmpty()) nombre[0].uppercase() else "C"
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error: ${e.message}")
        }
    }

    private fun setupListeners() {
        btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
            goToLogin()
        }

        btnNotifications.setOnClickListener {
            Toast.makeText(this, "Notificaciones", Toast.LENGTH_SHORT).show()
        }

        btnNewWorld.setOnClickListener {
            startActivity(Intent(this, CreateProjectActivity::class.java))
        }

        btnCargarMundo.setOnClickListener {
            startActivity(Intent(this, LoadWorldActivity::class.java))
        }

        tvVerTodos.setOnClickListener {
            startActivity(Intent(this, LoadWorldActivity::class.java))
        }



    }

    private fun setupTabs() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val categoria = when (tab?.position) {
                    0 -> "Todos"
                    1 -> "Naturaleza"
                    2 -> "Construcción"
                    3 -> "Mecanismo"
                    else -> "Todos"
                }
                filtrarPorCategoria(categoria)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun filtrarPorCategoria(categoria: String) {
        if (categoria == "Todos") {
            viewModel.proyectosLiveData.observe(this) { proyectos ->
                actualizarLista(proyectos)
            }
        } else {
            viewModel.filtrarPorCategoria(categoria).observe(this) { proyectos ->
                actualizarLista(proyectos)
            }
        }
    }

    private fun actualizarLista(proyectos: List<Proyecto>) {
        if (proyectos.isNullOrEmpty()) {
            llEmptyState.visibility = View.VISIBLE
            recyclerViewMundos.visibility = View.GONE
        } else {
            llEmptyState.visibility = View.GONE
            recyclerViewMundos.visibility = View.VISIBLE
            adapter.actualizarLista(proyectos)
            tvHoursCount.text = "${proyectos.size * 2}h"
            viewModel.cargarEstadisticas()
        }
    }

    private fun setupBottomNav() {
        bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> true
                R.id.nav_worlds -> {
                    startActivity(Intent(this, LoadWorldActivity::class.java))
                    true
                }
                R.id.nav_inventory -> {
                    mostrarInventario()
                    true
                }
                else -> false
            }
        }
    }


    private fun mostrarEstadoVacio() {
        llEmptyState.visibility = View.VISIBLE
        recyclerViewMundos.visibility = View.GONE
    }




    private fun mostrarDialogoConfirmacion(proyecto: Proyecto) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Eliminar proyecto")
            .setMessage("¿Seguro que quieres eliminar \"${proyecto.nombre}\"?")
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.eliminarProyecto(proyecto.id)
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "\"${proyecto.nombre}\" eliminado",
                    Snackbar.LENGTH_LONG
                ).setAction("Deshacer") {
                    viewModel.crearProyecto(proyecto.nombre)
                }.show()
            }
            .show()
    }
    private fun mostrarInventario() {
        val bottomSheet = InventarioBottomSheet()
        bottomSheet.show(supportFragmentManager, "inventario")
    }
    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        if (auth.currentUser == null) {
            goToLogin()
        }else {
            viewModel.cargarEstadisticas()  // ← AGREGAR
        }
    }
}