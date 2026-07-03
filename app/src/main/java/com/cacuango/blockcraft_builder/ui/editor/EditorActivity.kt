package com.cacuango.blockcraft.builder.ui.editor

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.cacuango.blockcraft.builder.R
import com.cacuango.blockcraft.builder.databinding.ActivityEditorBinding
import com.cacuango.blockcraft.builder.viewmodel.ProyectoViewModel

class EditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditorBinding
    private lateinit var viewModel: ProyectoViewModel
    private var proyectoId: Int = 0
    private var ordenAccion: Int = 0
    private var tipoActual: String = "madera"

    private var bloqueSeleccionadoView: android.view.ViewGroup? = null

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

    override fun onPause() {
        super.onPause()
        // Guardar automáticamente al salir
        val bloques = binding.isometricView.obtenerBloquesParaGuardar(proyectoId)
        if (bloques.isNotEmpty()) {
            viewModel.guardarTodosLosBloques(proyectoId, bloques)
        }
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
        binding.btnBack.setOnClickListener { finish() }

        binding.btnSave.setOnClickListener {
            binding.btnSave.isEnabled = false
            val bloques = binding.isometricView.obtenerBloquesParaGuardar(proyectoId)
            viewModel.guardarTodosLosBloques(proyectoId, bloques)
            viewModel.guardarProyecto(proyectoId)
        }

        binding.btnUndo.setOnClickListener {
            val resultado = binding.isometricView.deshacerUltimo()
            if (resultado == null) {
                Toast.makeText(this, "No hay bloques para deshacer", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnRedo.setOnClickListener {
            Toast.makeText(this, "↪ Rehacer no disponible aún", Toast.LENGTH_SHORT).show()
        }

        binding.switchGrid.setOnCheckedChangeListener { _, isChecked ->
            binding.isometricView.mostrarCuadricula(isChecked)
        }

        // ===== SELECTOR VISUAL DE BLOQUES =====
        val botonesBloques = listOf(
            Pair(binding.btnBloquesMadera, "madera"),
            Pair(binding.btnBloquesPiedra, "piedra"),
            Pair(binding.btnBloquesLadrillo, "ladrillo"),
            Pair(binding.btnBloquesTierra, "tierra"),
            Pair(binding.btnBloquesArena, "arena"),
            Pair(binding.btnBloquesCristal, "cristal")
        )

        botonesBloques.forEach { (boton, tipo) ->
            boton.setOnClickListener {
                // Resetear todos a normal
                botonesBloques.forEach { (b, _) ->
                    b.setBackgroundResource(R.drawable.bg_bloque_normal)
                }
                // Marcar seleccionado
                boton.setBackgroundResource(R.drawable.bg_bloque_seleccionado)
                bloqueSeleccionadoView = boton
                // Actualizar tipo en el motor
                tipoActual = tipo
                binding.isometricView.setTipoSeleccionado(tipo)
            }
        }

        // Seleccionar Madera por defecto
        binding.btnBloquesMadera.setBackgroundResource(R.drawable.bg_bloque_seleccionado)
        bloqueSeleccionadoView = binding.btnBloquesMadera
        binding.isometricView.setTipoSeleccionado("madera")
    }



    private fun setupObservers() {
        // Cargar bloques en el grid cuando Room los devuelve
        viewModel.bloquesDelProyecto.observe(this) { bloques ->
            if (bloques.isNotEmpty()) {
                binding.isometricView.cargarBloques(bloques)
            }
        }

        viewModel.mensajeExito.observe(this) { mensaje ->
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
            // Si el mensaje es de guardado, habilitar botón de nuevo
            binding.btnSave.isEnabled = true
        }

        viewModel.error.observe(this) { mensaje ->
            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
            binding.btnSave.isEnabled = true
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.btnSave.isEnabled = !isLoading
        }

        viewModel.contadorBloques.observe(this) { contador ->
            binding.tvProjectTitle.text = "Bloques: $contador"
        }
    }

    private fun cargarProyecto() {
        // Cargar info del proyecto
        viewModel.cargarProyecto(proyectoId)
        // Cargar bloques guardados en Room
        viewModel.cargarBloquesDelProyecto(proyectoId)
    }
}