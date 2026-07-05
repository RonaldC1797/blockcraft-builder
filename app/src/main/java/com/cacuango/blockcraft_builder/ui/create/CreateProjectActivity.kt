// ui/create/CreateProjectActivity.kt
package com.cacuango.blockcraft.builder.ui.create

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.cacuango.blockcraft.builder.R
import com.cacuango.blockcraft.builder.databinding.ActivityCreateProjectBinding
import com.cacuango.blockcraft.builder.ui.editor.EditorActivity
import com.cacuango.blockcraft.builder.viewmodel.ProyectoViewModel

class CreateProjectActivity : AppCompatActivity() {

    private var proyectoId: Int = 0
    private var esEdicion: Boolean = false
    private var categoriaSeleccionada: String = "Todos"  // ← AGREGAR

    private lateinit var binding: ActivityCreateProjectBinding
    private lateinit var viewModel: ProyectoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateProjectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ProyectoViewModel::class.java]

        proyectoId = intent.getIntExtra("PROYECTO_ID", 0)
        esEdicion = proyectoId > 0

        if (esEdicion) {
            binding.etNombreProyecto.setText(intent.getStringExtra("NOMBRE_PROYECTO") ?: "")
            binding.btnStartBuilding.text = "Guardar cambios"
            categoriaSeleccionada = intent.getStringExtra("CATEGORIA_PROYECTO") ?: "Todos"

        }

        setupUI()
        setupCategorias()  // ← AGREGAR
        setupObservers()
    }

    // ✅ PASO 6 — Selector de categorías
    private fun setupCategorias() {
        val botones = listOf(
            Pair(binding.btnCategoriaTodos, "Todos"),
            Pair(binding.btnCategoriaNaturaleza, "Naturaleza"),
            Pair(binding.btnCategoriaConstructor, "Construcción"),
            Pair(binding.btnCategoriaMecanismo, "Mecanismo")
        )

        botones.forEach { (boton, categoria) ->
            boton.setOnClickListener {
                botones.forEach { (b, _) ->
                    b.setBackgroundResource(R.drawable.bg_button_outline)
                    b.setTextColor(resources.getColor(R.color.purple_500, null))
                }
                boton.setBackgroundResource(R.drawable.bg_button_primary)
                boton.setTextColor(resources.getColor(android.R.color.white, null))
                categoriaSeleccionada = categoria
            }
        }

        // ✅ Resaltar el botón de la categoría actual (modo edición o "Todos" por defecto)
        botones.forEach { (boton, categoria) ->
            if (categoria == categoriaSeleccionada) {
                boton.setBackgroundResource(R.drawable.bg_button_primary)
                boton.setTextColor(resources.getColor(android.R.color.white, null))
            } else {
                boton.setBackgroundResource(R.drawable.bg_button_outline)
                boton.setTextColor(resources.getColor(R.color.purple_500, null))
            }
        }
    }

    private fun setupUI() {
        binding.btnStartBuilding.setOnClickListener {
            val nombre = binding.etNombreProyecto.text.toString().trim()

            if (nombre.isEmpty()) {
                binding.etNombreProyecto.error = "El nombre no puede estar vacío"
                return@setOnClickListener
            }
            if (nombre.length < 3) {
                binding.etNombreProyecto.error = "El nombre debe tener al menos 3 caracteres"
                return@setOnClickListener
            }
            if (!nombre.matches(Regex("^[a-zA-Z0-9 ]+$"))) {
                binding.etNombreProyecto.error = "Solo letras, números y espacios"
                return@setOnClickListener
            }

            if (esEdicion) {
                viewModel.actualizarProyecto(proyectoId, nombre, categoriaSeleccionada) // ← cambiar
            } else {
                // ✅ PASO 6 — Pasar categoría al crear
                viewModel.crearProyecto(nombre, categoriaSeleccionada)
            }
            binding.btnStartBuilding.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE
        }

        binding.btnBack.setOnClickListener { finish() }

        binding.etNombreProyecto.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.etNombreProyecto.error = null
        }
    }

    private fun setupObservers() {
        viewModel.proyectoCreado.observe(this) { proyectoId ->
            if (proyectoId > 0) {
                val nombre = binding.etNombreProyecto.text.toString().trim()
                val intent = Intent(this, EditorActivity::class.java).apply {
                    putExtra("PROYECTO_ID", proyectoId)
                    putExtra("ES_NUEVO", true)
                    putExtra("NOMBRE_PROYECTO", nombre)
                }
                startActivity(intent)
                finish()
            }
        }

        viewModel.error.observe(this) { mensaje ->
            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
            binding.btnStartBuilding.isEnabled = true
            binding.progressBar.visibility = View.GONE
        }

        viewModel.mensajeExito.observe(this) { mensaje ->
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
        }

        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE
                binding.btnStartBuilding.isEnabled = false
            } else {
                binding.progressBar.visibility = View.GONE
                binding.btnStartBuilding.isEnabled = true
            }
        }
    }
}