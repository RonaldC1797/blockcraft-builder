package com.cacuango.blockcraft.builder

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser

    // Variables para la UI
    private lateinit var tvAvatar: TextView
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvBlocksCount: TextView
    private lateinit var tvHoursCount: TextView
    private lateinit var btnLogout: ImageButton
    private lateinit var btnNotifications: ImageButton
    private lateinit var btnNewWorld: MaterialButton
    private lateinit var btnCreateFirst: MaterialButton
    private lateinit var tabLayout: TabLayout
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var llEmptyState: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("MI_APP", "========== MAIN ACTIVITY INICIADA ==========")

        auth = FirebaseAuth.getInstance()
        user = auth.currentUser ?: run {
            Log.e("MI_APP", "❌ No hay usuario, redirigiendo a Login")
            goToLogin()
            return
        }

        Log.d("MI_APP", "✅ Usuario autenticado: ${user.email}")
        Log.d("MI_APP", "📛 Nombre en Firebase: ${user.displayName}")

        // Inicializar vistas
        initViews()

        // ✅ Mostrar información del usuario con verificación forzada
        showUserInfo()

        // 🔥 Forzar actualización después de 500ms (por si el layout no se ha renderizado)
        android.os.Handler(mainLooper).postDelayed({
            Log.d("MI_APP", "🔍 Forzando actualización de UI...")
            showUserInfo()
        }, 500)

        // Configurar listeners
        setupListeners()

        // Configurar tabs
        setupTabs()

        // Configurar bottom navigation
        setupBottomNav()

        // Mostrar datos de ejemplo
        showExampleData()

        Log.d("MI_APP", "========== MAIN ACTIVITY CONFIGURADA ==========")
    }

    private fun initViews() {
        tvAvatar = findViewById(R.id.tvAvatar)
        tvUserName = findViewById(R.id.tvUserName)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        tvBlocksCount = findViewById(R.id.tvBlocksCount)
        tvHoursCount = findViewById(R.id.tvHoursCount)
        btnLogout = findViewById(R.id.btnLogout)
        btnNotifications = findViewById(R.id.btnNotifications)
        btnNewWorld = findViewById(R.id.btnNewWorld)
        btnCreateFirst = findViewById(R.id.btnCreateFirst)
        tabLayout = findViewById(R.id.tabLayout)
        bottomNav = findViewById(R.id.bottomNav)
        llEmptyState = findViewById(R.id.llEmptyState)

        Log.d("MI_APP", "✅ Views inicializados correctamente")

        // 🔥 Verificar que los TextView no sean null
        if (tvUserName == null) {
            Log.e("MI_APP", "❌ tvUserName es NULL")
        } else {
            Log.d("MI_APP", "✅ tvUserName encontrado: ${tvUserName.text}")
        }
        if (tvUserEmail == null) {
            Log.e("MI_APP", "❌ tvUserEmail es NULL")
        } else {
            Log.d("MI_APP", "✅ tvUserEmail encontrado: ${tvUserEmail.text}")
        }
    }

    /**
     * ✅ MUESTRA LA INFORMACIÓN DEL USUARIO
     */
    private fun showUserInfo() {
        try {
            val displayName = user.displayName
            Log.d("MI_APP", "🔍 DisplayName obtenido: '$displayName'")

            val nombreMostrar = if (!displayName.isNullOrEmpty()) {
                displayName
            } else {
                "Usuario"
            }

            Log.d("MI_APP", "📛 Nombre a mostrar: '$nombreMostrar'")

            val initial = if (nombreMostrar.isNotEmpty()) {
                nombreMostrar[0].toString().uppercase()
            } else {
                "U"
            }

            // 🔥 ACTUALIZAR DIRECTAMENTE CON TEXTO CLARO
            tvAvatar.text = initial
            tvUserName.text = "¡Hola, $nombreMostrar!"
            tvUserEmail.text = user.email ?: "usuario@email.com"

            // 🔥 FORZAR VISIBILIDAD
            tvUserName.visibility = TextView.VISIBLE
            tvUserEmail.visibility = TextView.VISIBLE

            Log.d("MI_APP", "✅ Avatar: ${tvAvatar.text}")
            Log.d("MI_APP", "✅ Saludo: ${tvUserName.text}")
            Log.d("MI_APP", "✅ Email: ${tvUserEmail.text}")

            // 🔥 Forzar refresco visual
            tvAvatar.invalidate()
            tvUserName.invalidate()
            tvUserEmail.invalidate()

        } catch (e: Exception) {
            Log.e("MI_APP", "❌ Error al mostrar info: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun setupListeners() {
        // 🔥 Botón de cerrar sesión
        btnLogout.setOnClickListener {
            Log.d("MI_APP", "🔴 Botón Cerrar Sesión presionado")
            auth.signOut()
            Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()
            goToLogin()
        }

        // 🔥 Botón de notificaciones
        btnNotifications.setOnClickListener {
            Toast.makeText(this, "Notificaciones", Toast.LENGTH_SHORT).show()
        }

        // Botón "Nuevo mundo"
        btnNewWorld.setOnClickListener {
            Toast.makeText(this, "Abriendo creación de mundo...", Toast.LENGTH_SHORT).show()
        }

        // Botón "Crear ahora" (estado vacío)
        btnCreateFirst.setOnClickListener {
            Toast.makeText(this, "¡Vamos a crear tu primer mundo!", Toast.LENGTH_SHORT).show()
        }

        // "Ver todos"
        findViewById<TextView>(R.id.tvVerTodos).setOnClickListener {
            Toast.makeText(this, "Mostrando todos los mundos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupTabs() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val position = tab?.position ?: 0
                val filter = when (position) {
                    0 -> "Todos"
                    1 -> "Naturaleza"
                    2 -> "Construcción"
                    else -> "Todos"
                }
                Toast.makeText(this@MainActivity, "Filtrando: $filter", Toast.LENGTH_SHORT).show()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupBottomNav() {
        bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> true
                R.id.nav_worlds -> {
                    Toast.makeText(this, "Mis Mundos", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_inventory -> {
                    Toast.makeText(this, "Inventario", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_profile -> {
                    Toast.makeText(this, "Perfil", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    private fun showExampleData() {
        tvBlocksCount.text = "14.2k"
        tvHoursCount.text = "128h"
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        Log.d("MI_APP", "🔄 onResume ejecutado")
        if (auth.currentUser == null) {
            Log.d("MI_APP", "👤 No hay usuario, redirigiendo a Login")
            goToLogin()
        }
    }
}