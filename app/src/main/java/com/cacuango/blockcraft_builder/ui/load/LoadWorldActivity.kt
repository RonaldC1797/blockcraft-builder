// ui/load/LoadWorldActivity.kt
package com.cacuango.blockcraft.builder.ui.load

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.cacuango.blockcraft.builder.R
import com.cacuango.blockcraft.builder.data.local.entity.Proyecto
import com.cacuango.blockcraft.builder.databinding.ActivityLoadWorldBinding
import com.cacuango.blockcraft.builder.ui.create.CreateProjectActivity
import com.cacuango.blockcraft.builder.ui.editor.EditorActivity
import com.cacuango.blockcraft.builder.viewmodel.ProyectoViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar



class LoadWorldActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoadWorldBinding
    private lateinit var viewModel: ProyectoViewModel
    private lateinit var adapter: MundoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoadWorldBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Inicializar ViewModel correctamente
        viewModel = ViewModelProvider(this)[ProyectoViewModel::class.java]

        setupUI()
        setupObservers()

    }

    private fun setupUI() {
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

        binding.recyclerViewMundos.apply {
            layoutManager = LinearLayoutManager(this@LoadWorldActivity)
            adapter = this@LoadWorldActivity.adapter
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filtrarMundos(query); return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                filtrarMundos(newText); return true
            }
        })

        setupFilterChips()
        binding.btnBack.setOnClickListener { finish() }
        actualizarAlmacenamiento()
    }

    private fun mostrarDialogoConfirmacion(mundo: Proyecto) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Eliminar proyecto")
            .setMessage("¿Seguro que quieres eliminar \"${mundo.nombre}\"? Esta acción no se puede deshacer.")
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarConDeshacer(mundo)
            }
            .show()
    }
    private fun eliminarConDeshacer(mundo: Proyecto) {
        // ✅ PATRÓN: Acción reversible — Snackbar con opción Deshacer
        viewModel.eliminarProyecto(mundo.id)

        Snackbar.make(
            binding.root,
            "\"${mundo.nombre}\" eliminado",
            Snackbar.LENGTH_LONG
        ).setAction("Deshacer") {
            viewModel.crearProyecto(mundo.nombre)
        }.show()
    }


    private fun setupFilterChips() {
        val chips = listOf(
            binding.chipTodos,
            binding.chipPradera,
            binding.chipDesierto,
            binding.chipNieve
        )

        chips.forEach { chip ->
            chip.setOnClickListener {
                chips.forEach { it.isChecked = false }
                chip.isChecked = true

                val bioma = when (chip.id) {
                    R.id.chipTodos -> null
                    R.id.chipPradera -> "Pradera"
                    R.id.chipDesierto -> "Desierto"
                    R.id.chipNieve -> "Nieve"
                    else -> null
                }
                filtrarPorBioma(bioma)
            }
        }

        binding.chipTodos.isChecked = true
    }

    private fun filtrarMundos(query: String?) {
        if (query.isNullOrEmpty()) {
            // ✅ Sin búsqueda — el Flow ya muestra todos automáticamente
            setupObservers()
        } else {
            viewModel.buscarProyectos(query)
        }
    }

    private fun filtrarPorBioma(bioma: String?) {
        viewModel.filtrarProyectosPorBioma(bioma)
    }

    private fun actualizarAlmacenamiento() {
        val uso = 42
        binding.progressStorage.progress = uso
        binding.tvStoragePercentage.text = String.format("%d%%", uso)
    }

    private fun setupObservers() {
        // Observer principal — Flow reactivo de Room
        viewModel.proyectosLiveData.observe(this) { proyectos ->
            if (proyectos.isEmpty()) {
                binding.emptyState.visibility = View.VISIBLE
                binding.recyclerViewMundos.visibility = View.GONE
            } else {
                binding.emptyState.visibility = View.GONE
                binding.recyclerViewMundos.visibility = View.VISIBLE
                adapter.actualizarLista(proyectos)
            }
        }

        // ✅ Observer de búsqueda — resultado de buscarProyectos()
        viewModel.proyectosBusqueda.observe(this) { proyectos ->
            if (proyectos.isNullOrEmpty()) {
                binding.emptyState.visibility = View.VISIBLE
                binding.recyclerViewMundos.visibility = View.GONE
            } else {
                binding.emptyState.visibility = View.GONE
                binding.recyclerViewMundos.visibility = View.VISIBLE
                adapter.actualizarLista(proyectos)
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { mensaje ->
            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
        }

        viewModel.mensajeExito.observe(this) { mensaje ->
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
        }
    }
}