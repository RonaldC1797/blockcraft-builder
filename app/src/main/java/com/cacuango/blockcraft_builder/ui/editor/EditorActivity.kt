package com.cacuango.blockcraft.builder.ui.editor

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.cacuango.blockcraft.builder.databinding.ActivityEditorBinding
import com.cacuango.blockcraft.builder.viewmodel.ProyectoViewModel

class EditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditorBinding
    private lateinit var viewModel: ProyectoViewModel
    private var proyectoId: Int = 0
    private var ordenAccion: Int = 0
    private var tipoActual: String = "madera"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        proyectoId = intent.getIntExtra("PROYECTO_ID", 0)
        viewModel = ViewModelProvider(this)[ProyectoViewModel::class.java]

        setupCanvas()
        setupUI()
        setupObservers()
        cargarProyecto()
    }

    private fun setupCanvas() {
        // Colocar bloque — ahora incluye altura
        binding.isometricView.onBloqueColocado = { col, fila, altura, tipo ->
            if (tipo == "limite") {
                Toast.makeText(this, "⚠️ Altura máxima alcanzada", Toast.LENGTH_SHORT).show()
            } else {
                ordenAccion++
                viewModel.agregarBloque(proyectoId, tipo, col, altura, fila)
                viewModel.registrarAccion(
                    proyectoId = proyectoId,
                    tipoAccion = "COLOCAR",
                    tipoBloque = tipo,
                    posX = col, posY = altura, posZ = fila,
                    orden = ordenAccion
                )
            }
        }

        // Deshacer desde botón
        binding.btnUndo.setOnClickListener {
            val resultado = binding.isometricView.deshacerUltimo()
            if (resultado != null) {
                val (col, fila, altura) = resultado
                viewModel.deshacerUltimaAccion(proyectoId)
                viewModel.registrarAccion(
                    proyectoId = proyectoId,
                    tipoAccion = "DESHACER",
                    tipoBloque = tipoActual,
                    posX = col, posY = altura, posZ = fila,
                    orden = ++ordenAccion
                )
            } else {
                Toast.makeText(this, "No hay bloques para deshacer", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupUI() {
        // Botón regresar
        binding.btnBack.setOnClickListener { finish() }

        // Botón guardar
        binding.btnSave.setOnClickListener {
            viewModel.guardarProyecto(proyectoId)
        }

        // Botón deshacer — conectado al motor
        binding.btnUndo.setOnClickListener {
            binding.isometricView.deshacerUltimo()
            viewModel.deshacerUltimaAccion(proyectoId)
        }

        // Botón rehacer
        binding.btnRedo.setOnClickListener {
            Toast.makeText(this, "↪ Rehacer no disponible aún", Toast.LENGTH_SHORT).show()
        }

        // Switch cuadrícula — HU-05
        binding.switchGrid.setOnCheckedChangeListener { _, isChecked ->
            binding.isometricView.mostrarCuadricula(isChecked)
            viewModel.activarCuadricula(isChecked)
        }

        // Tabs de categorías — HU-03
        binding.tabStructure.setOnClickListener { seleccionarCategoria("Estructura") }
        binding.tabDecoration.setOnClickListener { seleccionarCategoria("Decoración") }
        binding.tabNature.setOnClickListener { seleccionarCategoria("Naturaleza") }
        binding.tabSpecial.setOnClickListener { seleccionarCategoria("Especiales") }

        seleccionarCategoria("Estructura")
    }

    private fun seleccionarCategoria(categoria: String) {
        // Resetear estilos
        listOf(
            binding.tabStructure,
            binding.tabDecoration,
            binding.tabNature,
            binding.tabSpecial
        ).forEach { tab ->
            tab.setTextColor(resources.getColor(android.R.color.black, null))
            tab.setBackgroundColor(resources.getColor(android.R.color.transparent, null))
        }

        // Marcar seleccionado y cambiar tipo de bloque activo
        when (categoria) {
            "Estructura" -> {
                binding.tabStructure.setTextColor(
                    resources.getColor(com.cacuango.blockcraft.builder.R.color.purple_500, null)
                )
                tipoActual = "madera"
            }
            "Decoración" -> {
                binding.tabDecoration.setTextColor(
                    resources.getColor(com.cacuango.blockcraft.builder.R.color.purple_500, null)
                )
                tipoActual = "ladrillo"
            }
            "Naturaleza" -> {
                binding.tabNature.setTextColor(
                    resources.getColor(com.cacuango.blockcraft.builder.R.color.purple_500, null)
                )
                tipoActual = "tierra"
            }
            "Especiales" -> {
                binding.tabSpecial.setTextColor(
                    resources.getColor(com.cacuango.blockcraft.builder.R.color.purple_500, null)
                )
                tipoActual = "cristal"
            }
        }

        // Actualizar tipo en la vista
        binding.isometricView.setTipoSeleccionado(tipoActual)
        Toast.makeText(this, "Categoría: $categoria — Bloque: $tipoActual", Toast.LENGTH_SHORT).show()
    }

    private fun setupObservers() {
        viewModel.mensajeExito.observe(this) { mensaje ->
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
        }

        viewModel.error.observe(this) { mensaje ->
            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
        }

        viewModel.contadorBloques.observe(this) { contador ->
            binding.tvProjectTitle.text = "Bloques: $contador"
        }
    }

    private fun cargarProyecto() {
        viewModel.cargarProyecto(proyectoId)
        viewModel.obtenerContadorBloques(proyectoId)
    }
}