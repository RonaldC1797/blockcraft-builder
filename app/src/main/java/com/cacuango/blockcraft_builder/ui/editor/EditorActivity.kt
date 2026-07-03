// ui/editor/EditorActivity.kt
package com.cacuango.blockcraft.builder.ui.editor

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cacuango.blockcraft.builder.databinding.ActivityEditorBinding

class EditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditorBinding
    private var proyectoId: Int = 0
    private var esNuevo: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener datos del Intent
        proyectoId = intent.getIntExtra("PROYECTO_ID", 0)
        esNuevo = intent.getBooleanExtra("ES_NUEVO", false)

        setupUI()
        cargarProyecto()
    }

    private fun setupUI() {
        // Botón de regreso
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Botón Guardar
        binding.btnSave.setOnClickListener {
            Toast.makeText(this, "✅ Proyecto guardado", Toast.LENGTH_SHORT).show()
        }

        // Botón Deshacer (HU-02)
        binding.btnUndo.setOnClickListener {
            Toast.makeText(this, "↩ Deshacer último bloque", Toast.LENGTH_SHORT).show()
        }

        // Botón Rehacer
        binding.btnRedo.setOnClickListener {
            Toast.makeText(this, "↪ Rehacer", Toast.LENGTH_SHORT).show()
        }

        // Switch de cuadrícula (HU-05)
        binding.switchGrid.setOnCheckedChangeListener { _, isChecked ->
            val mensaje = if (isChecked) "Cuadrícula activada" else "Cuadrícula desactivada"
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
        }

        // Tabs de categorías (HU-03)
        binding.tabStructure.setOnClickListener {
            seleccionarCategoria("Estructura")
        }
        binding.tabDecoration.setOnClickListener {
            seleccionarCategoria("Decoración")
        }
        binding.tabNature.setOnClickListener {
            seleccionarCategoria("Naturaleza")
        }
        binding.tabSpecial.setOnClickListener {
            seleccionarCategoria("Especiales")
        }

        // Seleccionar "Estructura" por defecto
        seleccionarCategoria("Estructura")
    }

    private fun seleccionarCategoria(categoria: String) {
        // Resetear estilos de todos los tabs
        val tabs = listOf(
            binding.tabStructure,
            binding.tabDecoration,
            binding.tabNature,
            binding.tabSpecial
        )

        tabs.forEach { tab ->
            tab.setTextColor(resources.getColor(android.R.color.black, null))
            tab.setBackgroundColor(resources.getColor(android.R.color.transparent, null))
        }

        // Marcar el seleccionado
        when (categoria) {
            "Estructura" -> {
                binding.tabStructure.setTextColor(resources.getColor(com.cacuango.blockcraft.builder.R.color.purple_500, null))
                binding.tabStructure.setBackgroundColor(resources.getColor(android.R.color.darker_gray, null))
            }
            "Decoración" -> {
                binding.tabDecoration.setTextColor(resources.getColor(com.cacuango.blockcraft.builder.R.color.purple_500, null))
                binding.tabDecoration.setBackgroundColor(resources.getColor(android.R.color.darker_gray, null))
            }
            "Naturaleza" -> {
                binding.tabNature.setTextColor(resources.getColor(com.cacuango.blockcraft.builder.R.color.purple_500, null))
                binding.tabNature.setBackgroundColor(resources.getColor(android.R.color.darker_gray, null))
            }
            "Especiales" -> {
                binding.tabSpecial.setTextColor(resources.getColor(com.cacuango.blockcraft.builder.R.color.purple_500, null))
                binding.tabSpecial.setBackgroundColor(resources.getColor(android.R.color.darker_gray, null))
            }
        }

        Toast.makeText(this, "Categoría: $categoria", Toast.LENGTH_SHORT).show()
    }

    private fun cargarProyecto() {
        if (proyectoId > 0) {
            binding.tvProjectTitle.text = if (esNuevo) {
                "Nuevo Proyecto"
            } else {
                "Proyecto #$proyectoId"
            }
        } else {
            binding.tvProjectTitle.text = "Sin proyecto"
        }
    }
}