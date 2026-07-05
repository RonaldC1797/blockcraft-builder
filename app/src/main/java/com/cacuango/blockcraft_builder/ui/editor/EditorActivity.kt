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

        // ===== BOTONES DE NIVEL =====
        var nivelActual = 0

        binding.btnNivelSubir.setOnClickListener {
            if (nivelActual < 9) {
                nivelActual++
                binding.tvNivelActual.text = nivelActual.toString()
                binding.isometricView.setAlturaSeleccionada(nivelActual)
            }
        }

        binding.btnNivelBajar.setOnClickListener {
            if (nivelActual > 0) {
                nivelActual--
                binding.tvNivelActual.text = nivelActual.toString()
                binding.isometricView.setAlturaSeleccionada(nivelActual)
            }
        }


        binding.btnRedo.setOnClickListener {
            val resultado = binding.isometricView.rehacerUltimo()
            if (resultado != null) {
                val (col, fila, altura) = resultado
                viewModel.agregarBloque(proyectoId, tipoActual, col, altura, fila)
                viewModel.registrarAccion(
                    proyectoId = proyectoId,
                    tipoAccion = "COLOCAR",
                    tipoBloque = tipoActual,
                    posX = col, posY = altura, posZ = fila,
                    orden = ++ordenAccion
                )
            } else {
                Toast.makeText(this, "No hay acciones para rehacer", Toast.LENGTH_SHORT).show()
            }
        }
        binding.switchGrid.setOnCheckedChangeListener { _, isChecked ->
            binding.isometricView.mostrarCuadricula(isChecked)
        }

        // Categoría por defecto
        cargarBloquesDeCategoría("Estructura")

        // Tabs de categorías
        binding.tabStructure.setOnClickListener { cargarBloquesDeCategoría("Estructura") }
        binding.tabDecoration.setOnClickListener { cargarBloquesDeCategoría("Decoración") }
        binding.tabNature.setOnClickListener { cargarBloquesDeCategoría("Naturaleza") }
        binding.tabSpecial.setOnClickListener { cargarBloquesDeCategoría("Especiales") }
    }

    private fun cargarBloquesDeCategoría(categoria: String) {
        // Resetear color de todos los tabs
        binding.tabStructure.setTextColor(
            resources.getColor(android.R.color.black, null))
        binding.tabDecoration.setTextColor(
            resources.getColor(android.R.color.black, null))
        binding.tabNature.setTextColor(
            resources.getColor(android.R.color.black, null))
        binding.tabSpecial.setTextColor(
            resources.getColor(android.R.color.black, null))

        // Marcar tab activo
        val colorActivo = resources.getColor(R.color.purple_500, null)
        when (categoria) {
            "Estructura" -> binding.tabStructure.setTextColor(colorActivo)
            "Decoración" -> binding.tabDecoration.setTextColor(colorActivo)
            "Naturaleza" -> binding.tabNature.setTextColor(colorActivo)
            "Especiales" -> binding.tabSpecial.setTextColor(colorActivo)
        }

        // Obtener lista de bloques
        val lista: List<String> = when (categoria) {
            "Estructura" -> IsometricView.BLOQUES_ESTRUCTURA
            "Decoración" -> IsometricView.BLOQUES_DECORACION
            "Naturaleza" -> IsometricView.BLOQUES_NATURALEZA
            "Especiales" -> IsometricView.BLOQUES_ESPECIALES
            else         -> IsometricView.BLOQUES_ESTRUCTURA
        }

        // Reconstruir barra de bloques
        binding.layoutBloques.removeAllViews()

        lista.forEach { nombreBloque ->
            val resId = resources.getIdentifier(
                nombreBloque, "drawable", packageName
            )

            val item = android.widget.LinearLayout(this).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                gravity = android.view.Gravity.CENTER
                setBackgroundResource(R.drawable.bg_bloque_normal)
                val lp = android.widget.LinearLayout.LayoutParams(72, 72)
                lp.marginEnd = 8
                layoutParams = lp
                setPadding(4, 4, 4, 4)
            }

            val img = android.widget.ImageView(this).apply {
                val lp = android.widget.LinearLayout.LayoutParams(52, 52)
                layoutParams = lp
                scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                if (resId != 0) setImageResource(resId)
            }

            item.addView(img)
            item.setOnClickListener {
                for (i in 0 until binding.layoutBloques.childCount) {
                    binding.layoutBloques.getChildAt(i)
                        .setBackgroundResource(R.drawable.bg_bloque_normal)
                }
                item.setBackgroundResource(R.drawable.bg_bloque_seleccionado)
                tipoActual = nombreBloque
                binding.isometricView.setTipoSeleccionado(nombreBloque)
            }

            binding.layoutBloques.addView(item)
        }

        // Seleccionar primero por defecto
        if (lista.isNotEmpty()) {
            tipoActual = lista[0]
            binding.isometricView.setTipoSeleccionado(lista[0])
            binding.layoutBloques.getChildAt(0)
                ?.setBackgroundResource(R.drawable.bg_bloque_seleccionado)
        }
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