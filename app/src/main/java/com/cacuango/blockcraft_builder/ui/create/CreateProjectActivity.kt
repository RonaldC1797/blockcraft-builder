// ui/create/CreateProjectActivity.kt
package com.cacuango.blockcraft.builder.ui.create

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.cacuango.blockcraft.builder.databinding.ActivityCreateProjectBinding
import com.cacuango.blockcraft.builder.ui.editor.EditorActivity
import com.cacuango.blockcraft.builder.viewmodel.ProyectoViewModel

class CreateProjectActivity : AppCompatActivity() {


    private var proyectoId: Int = 0
    private var esEdicion: Boolean = false

    private lateinit var binding: ActivityCreateProjectBinding
    private lateinit var viewModel: ProyectoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateProjectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ProyectoViewModel::class.java]

        // Recibir datos si viene en modo edición
        proyectoId = intent.getIntExtra("PROYECTO_ID", 0)
        esEdicion = proyectoId > 0

        if (esEdicion) {
            binding.etNombreProyecto.setText(intent.getStringExtra("NOMBRE_PROYECTO") ?: "")
            binding.btnStartBuilding.text = "Guardar cambios"
        }

        setupUI()
        setupObservers()
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
                viewModel.actualizarNombreProyecto(proyectoId, nombre)
            } else {
                viewModel.crearProyecto(nombre)
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
        // ✅ Observar cuando se crea el proyecto
        viewModel.proyectoCreado.observe(this) { proyectoId ->
            if (proyectoId > 0) {
                // Proyecto creado exitosamente - abrir editor
                val nombre = binding.etNombreProyecto.text.toString().trim()
                val intent = Intent(this, EditorActivity::class.java).apply {
                    putExtra("PROYECTO_ID", proyectoId)
                    putExtra("ES_NUEVO", true)
                    putExtra("NOMBRE_PROYECTO", nombre)
                }
                startActivity(intent)
                finish() // Cerrar esta actividad
            }
        }

        // ✅ Observar errores
        viewModel.error.observe(this) { mensaje ->
            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
            binding.btnStartBuilding.isEnabled = true
            binding.progressBar.visibility = View.GONE
        }

        // ✅ Observar mensajes de éxito
        viewModel.mensajeExito.observe(this) { mensaje ->
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
        }

        // ✅ Observar estado de carga
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