package com.cacuango.blockcraft.builder.ui.editor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.cacuango.blockcraft.builder.data.local.entity.Bloque
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class IsometricView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    // ← AGREGAR al inicio de la clase

    private val texturas = mutableMapOf<String, Bitmap>()
    private val paintBitmap = Paint(Paint.ANTI_ALIAS_FLAG)
    // ===== CONFIGURACIÓN =====
    private val GRID_SIZE = 20
    private val MAX_HEIGHT = 10
    private val BLOCK_WIDTH = 80f
    private val BLOCK_HEIGHT = 40f
    private val BLOCK_DEPTH = 24f

    // ===== CÁMARA =====
    private var cameraX = 0f
    private var cameraY = 0f
    private var rotacion = 0f          // ángulo de rotación en radianes
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var isDragging = false
    private var dragStartX = 0f
    private var dragStartY = 0f
    private val DRAG_THRESHOLD = 10f


    private var zoom = 1f              // ← AGREGAR
    private val ZOOM_MIN = 0.3f        // ← AGREGAR
    private val ZOOM_MAX = 3f          // ← AGREGAR
    private var lastFingerDistance = 0f // ← AGREGAR

    // ===== ROTACIÓN CON DOS DEDOS =====
    private var isRotating = false
    private var lastAngle = 0f
    private var initialFingerDistance = 0f

    // Agregar variables al inicio de la clase
    private var hoverCol = -1
    private var hoverFila = -1

    private val paintHover = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(120, 100, 200, 100)
        style = Paint.Style.FILL
    }
    private val paintHoverBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(200, 50, 180, 50)
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    private var alturaSeleccionada: Int = 0
    fun setAlturaSeleccionada(altura: Int) {
        alturaSeleccionada = altura
    }

    fun getAlturaSeleccionada(): Int = alturaSeleccionada

    // ===== ESTADO =====
    private val bloques = Array(GRID_SIZE) {
        Array(GRID_SIZE) { mutableListOf<String>() }
    }

    // Pila para deshacer — guarda (col, fila, altura, tipo)
    private val pilaDeshacer = ArrayDeque<Triple<Int, Int, String>>()
    // Pila para rehacer — guarda lo que se deshizo
    private val pilaRehacer = ArrayDeque<Triple<Int, Int, String>>()
    private var tipoSeleccionado: String = "madera"
    private var mostrarCuadricula: Boolean = false

    // ===== COLORES =====
// ===== BLOQUES POR CATEGORÍA =====
    val BLOQUES_ESTRUCTURA = listOf(
        "blk_estructura_piedra", "blk_estructura_madera",
        "blk_estructura_madera_oscura", "blk_estructura_madera_clara",
        "blk_estructura_ladrillo", "blk_estructura_metal",
        "blk_estructura_metal_oscuro", "blk_estructura_cemento",
        "blk_estructura_cemento_oscuro", "blk_estructura_roca",
        "blk_estructura_roca_oscura"
    )

    val BLOQUES_DECORACION = listOf(
        "blk_deco_amarillo", "blk_deco_naranja",
        "blk_deco_morado", "blk_deco_especial"
    )

    val BLOQUES_NATURALEZA = listOf(
        "blk_nat_cesped", "blk_nat_tierra", "blk_nat_tierra_oscura",
        "blk_nat_arcilla", "blk_nat_arena", "blk_nat_nieve",
        "blk_nat_hielo", "blk_nat_agua", "blk_nat_lava",
        "blk_nat_roca_verde", "blk_nat_musgo", "blk_nat_madera_arbol",
        "blk_nat_flor", "blk_nat_hoja"
    )

    val BLOQUES_ESPECIALES = listOf(
        "blk_esp_cristal_rojo", "blk_esp_cristal_azul",
        "blk_esp_cristal_verde", "blk_esp_oro", "blk_esp_diamante",
        "blk_esp_esmeralda", "blk_esp_rubi", "blk_esp_zafiro",
        "blk_esp_neon_azul", "blk_esp_neon_verde", "blk_esp_gris_especial"
    )

    companion object {
        val BLOQUES_ESTRUCTURA = listOf(
            "blk_estructura_piedra", "blk_estructura_madera",
            "blk_estructura_madera_oscura", "blk_estructura_madera_clara",
            "blk_estructura_ladrillo", "blk_estructura_metal",
            "blk_estructura_metal_oscuro", "blk_estructura_cemento",
            "blk_estructura_cemento_oscuro", "blk_estructura_roca",
            "blk_estructura_roca_oscura"
        )
        val BLOQUES_DECORACION = listOf(
            "blk_deco_amarillo", "blk_deco_naranja",
            "blk_deco_morado", "blk_deco_especial"
        )
        val BLOQUES_NATURALEZA = listOf(
            "blk_nat_cesped", "blk_nat_tierra", "blk_nat_tierra_oscura",
            "blk_nat_arcilla", "blk_nat_arena", "blk_nat_nieve",
            "blk_nat_hielo", "blk_nat_agua", "blk_nat_lava",
            "blk_nat_roca_verde", "blk_nat_musgo", "blk_nat_madera_arbol",
            "blk_nat_flor", "blk_nat_hoja"
        )
        val BLOQUES_ESPECIALES = listOf(
            "blk_esp_cristal_rojo", "blk_esp_cristal_azul",
            "blk_esp_cristal_verde", "blk_esp_oro", "blk_esp_diamante",
            "blk_esp_esmeralda", "blk_esp_rubi", "blk_esp_zafiro",
            "blk_esp_neon_azul", "blk_esp_neon_verde", "blk_esp_gris_especial"
        )
    }

    // ===== PAINTS =====
    private val paintTop   = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintLeft  = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintRight = Paint(Paint.ANTI_ALIAS_FLAG)

    private val paintGridLine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(90, 80, 80, 80)
        style = Paint.Style.STROKE
        strokeWidth = 1.2f
    }

    private val paintGridFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(30, 200, 200, 200)
        style = Paint.Style.FILL
    }

    private val paintBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(60, 0, 0, 0)
        style = Paint.Style.STROKE
        strokeWidth = 0.8f
    }

    // Callbacks
    var onBloqueColocado: ((col: Int, fila: Int, altura: Int, tipo: String) -> Unit)? = null
    var onBloqueEliminado: ((col: Int, fila: Int, altura: Int) -> Unit)? = null

    // Historial de colocación en orden exacto
    private val historialColocacion = ArrayDeque<Triple<Int, Int, Int>>() // col, fila, altura

    init {
        cargarTexturas(context)
        post {
            cameraX = width / 2f
            cameraY = BLOCK_HEIGHT * 3
        }
    }

    // ===== DIBUJO =====
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val orden = generarOrdenDibujo()

        for ((fila, col) in orden) {
            val (sx, sy) = toScreen(col, fila, 0)

            if (sx < -BLOCK_WIDTH * 2 || sx > width + BLOCK_WIDTH * 2) continue
            if (sy < -BLOCK_HEIGHT * (MAX_HEIGHT + 2) || sy > height + BLOCK_HEIGHT * 2) continue

            dibujarCeldaBase(canvas, sx, sy)

            // ✅ Highlight de la celda seleccionada
            if (col == hoverCol && fila == hoverFila) {
                val pila = bloques[fila][col]
                val alturaHover = pila.size
                val (hsx, hsy) = toScreen(col, fila, alturaHover)
                val path = celdaPath(hsx, hsy)
                canvas.drawPath(path, paintHover)
                canvas.drawPath(path, paintHoverBorder)
            }

            val pila = bloques[fila][col]
            for (altura in pila.indices) {
                val tipo = pila[altura]
                if (tipo.isEmpty()) continue  // ← AGREGAR esta línea
                val (bsx, bsy) = toScreen(col, fila, altura)
                dibujarBloque(canvas, bsx, bsy, tipo)
            }
        }
    }

    // Genera el orden de dibujo según el ángulo de rotación
    private fun generarOrdenDibujo(): List<Pair<Int, Int>> {
        val celdas = mutableListOf<Pair<Int, Int>>()
        for (fila in 0 until GRID_SIZE)
            for (col in 0 until GRID_SIZE)
                celdas.add(Pair(fila, col))

        // Ordenar por profundidad en pantalla (Y proyectado)
        return celdas.sortedWith(compareBy { (fila, col) ->
            val (_, sy) = toScreen(col, fila, 0)
            sy
        })
    }

    private fun dibujarCeldaBase(canvas: Canvas, sx: Float, sy: Float) {
        val path = celdaPath(sx, sy)
        canvas.drawPath(path, paintGridFill)
        if (mostrarCuadricula) canvas.drawPath(path, paintGridLine)
    }

    private fun dibujarBloque(canvas: Canvas, sx: Float, sy: Float, tipo: String) {
        val bw = BLOCK_WIDTH * zoom
        val bh = BLOCK_HEIGHT * zoom
        val bd = BLOCK_DEPTH * zoom
        val textura = texturas[tipo]

        if (textura != null) {
            val dstRect = android.graphics.RectF(
                sx,
                sy - bd * 1.5f,
                sx + bw,
                sy + bh * 0.5f
            )
            paintBitmap.alpha = 255
            canvas.drawBitmap(textura, null, dstRect, paintBitmap)
        } else {
            // Fallback simple sin coloresTipo
            paintTop.color = android.graphics.Color.GRAY
            val top = Path().apply {
                moveTo(sx, sy - bd)
                lineTo(sx + bw/2, sy + bh/2 - bd)
                lineTo(sx + bw, sy - bd)
                lineTo(sx + bw/2, sy - bh/2 - bd)
                close()
            }
            canvas.drawPath(top, paintTop)
            canvas.drawPath(top, paintBorder)

            paintLeft.color = android.graphics.Color.DKGRAY
            val left = Path().apply {
                moveTo(sx, sy - bd)
                lineTo(sx + bw/2, sy + bh/2 - bd)
                lineTo(sx + bw/2, sy + bh/2)
                lineTo(sx, sy)
                close()
            }
            canvas.drawPath(left, paintLeft)

            paintRight.color = android.graphics.Color.LTGRAY
            val right = Path().apply {
                moveTo(sx + bw, sy - bd)
                lineTo(sx + bw/2, sy + bh/2 - bd)
                lineTo(sx + bw/2, sy + bh/2)
                lineTo(sx + bw, sy)
                close()
            }
            canvas.drawPath(right, paintRight)
        }
    }
    private fun celdaPath(sx: Float, sy: Float): Path {
        val bw = BLOCK_WIDTH * zoom
        val bh = BLOCK_HEIGHT * zoom
        return Path().apply {
            moveTo(sx + bw / 2, sy - bh / 2)
            lineTo(sx + bw, sy)
            lineTo(sx + bw / 2, sy + bh / 2)
            lineTo(sx, sy)
            close()
        }
    }

    // ===== COORDENADAS CON ROTACIÓN y ZOOM =====
    private fun toScreen(col: Int, fila: Int, altura: Int): Pair<Float, Float> {
        val cx = col - GRID_SIZE / 2f
        val cz = fila - GRID_SIZE / 2f

        // Rotación horizontal
        val rx = cx * cos(rotacion) - cz * sin(rotacion)
        val rz = cx * sin(rotacion) + cz * cos(rotacion)

        // Proyección isométrica con zoom aplicado
        val bw = BLOCK_WIDTH * zoom   // ← ancho con zoom
        val bh = BLOCK_HEIGHT * zoom  // ← alto con zoom
        val bd = BLOCK_DEPTH * zoom   // ← profundidad con zoom

        val sx = cameraX + rx * (bw / 2) - rz * (bw / 2)
        val sy = cameraY + rx * (bh / 2) + rz * (bh / 2) - altura * bd

        return Pair(sx - bw / 2, sy - bh / 2)
    }

    private fun toGrid(touchX: Float, touchY: Float): Pair<Int, Int> {
        val relX = touchX - cameraX
        val relY = touchY - cameraY

        // ✅ Aplicar zoom igual que en toScreen()
        val bw = BLOCK_WIDTH * zoom
        val bh = BLOCK_HEIGHT * zoom

        // Invertir la proyección isométrica con zoom
        val isoX = (relX / (bw / 2) + relY / (bh / 2)) / 2
        val isoZ = (relY / (bh / 2) - relX / (bw / 2)) / 2

        // Invertir rotación
        val col = (isoX * cos(-rotacion) - isoZ * sin(-rotacion) + GRID_SIZE / 2).toInt()
        val fila = (isoX * sin(-rotacion) + isoZ * cos(-rotacion) + GRID_SIZE / 2).toInt()

        return Pair(
            col.coerceIn(0, GRID_SIZE - 1),
            fila.coerceIn(0, GRID_SIZE - 1)
        )
    }

    // ===== TOUCH — 1 dedo mover, 2 dedos rotar =====
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val pointerCount = event.pointerCount

        when {
            // ===== DOS DEDOS — ROTAR + ZOOM =====
            pointerCount >= 2 -> {
                when (event.actionMasked) {
                    MotionEvent.ACTION_POINTER_DOWN -> {
                        isRotating = true
                        isDragging = false
                        lastAngle = getAngle(event)
                        lastFingerDistance = getDistance(event) // ← AGREGAR
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (isRotating) {
                            // Rotación
                            val newAngle = getAngle(event)
                            val deltaAngulo = newAngle - lastAngle
                            rotacion += deltaAngulo * 0.5f
                            lastAngle = newAngle

                            // Zoom — pellizco
                            val newDistance = getDistance(event)
                            if (lastFingerDistance > 0) {
                                val escala = newDistance / lastFingerDistance
                                zoom = (zoom * escala).coerceIn(ZOOM_MIN, ZOOM_MAX)
                            }
                            lastFingerDistance = newDistance

                            invalidate()
                        }
                    }
                    MotionEvent.ACTION_POINTER_UP -> {
                        isRotating = false
                        lastFingerDistance = 0f  // ← AGREGAR
                        lastTouchX = event.getX(0)
                        lastTouchY = event.getY(0)
                    }
                }
                return true
            }

            // ===== UN DEDO — MOVER O COLOCAR BLOQUE =====
            else -> {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        if (!isRotating) {
                            lastTouchX = event.x
                            lastTouchY = event.y
                            dragStartX = event.x
                            dragStartY = event.y
                            isDragging = false
                            // Mostrar celda hover
                            val (col, fila) = toGrid(event.x, event.y)
                            hoverCol = col
                            hoverFila = fila
                            invalidate()
                        }
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (!isRotating) {
                            val dx = event.x - lastTouchX
                            val dy = event.y - lastTouchY
                            val totalDx = event.x - dragStartX
                            val totalDy = event.y - dragStartY

                            if (!isDragging &&
                                (kotlin.math.abs(totalDx) > DRAG_THRESHOLD ||
                                        kotlin.math.abs(totalDy) > DRAG_THRESHOLD)) {
                                isDragging = true
                                hoverCol = -1
                                hoverFila = -1
                            }

                            if (isDragging) {
                                cameraX += dx
                                cameraY += dy
                            } else {
                                // Actualizar hover mientras el dedo se mueve
                                val (col, fila) = toGrid(event.x, event.y)
                                hoverCol = col
                                hoverFila = fila
                            }

                            lastTouchX = event.x
                            lastTouchY = event.y
                            invalidate()
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        hoverCol = -1
                        hoverFila = -1
                        if (!isDragging && !isRotating) {
                            val (col, fila) = toGrid(event.x, event.y)
                            val pila = bloques[fila][col]

                            // ✅ Colocar en la altura seleccionada por el slider
                            if (alturaSeleccionada < MAX_HEIGHT) {
                                // Rellenar niveles vacíos debajo si es necesario
                                while (pila.size < alturaSeleccionada) {
                                    pila.add("")  // celda vacía — espacio aéreo
                                }
                                if (pila.size == alturaSeleccionada) {
                                    // Nivel vacío — colocar bloque nuevo
                                    pila.add(tipoSeleccionado)
                                } else {
                                    // Nivel ya ocupado — reemplazar
                                    pila[alturaSeleccionada] = tipoSeleccionado
                                }

                                historialColocacion.addLast(Triple(col, fila, alturaSeleccionada))
                                pilaRehacer.clear()
                                onBloqueColocado?.invoke(col, fila, alturaSeleccionada, tipoSeleccionado)
                            } else {
                                onBloqueColocado?.invoke(col, fila, -1, "limite")
                            }
                            invalidate()
                        }
                        isRotating = false
                    }
                }
                return true
            }
        }
    }

    // Calcular ángulo entre dos dedos
    private fun getAngle(event: MotionEvent): Float {
        val dx = event.getX(1) - event.getX(0)
        val dy = event.getY(1) - event.getY(0)
        return atan2(dy, dx)
    }

    // ===== API PÚBLICA =====
    fun setTipoSeleccionado(tipo: String) {
        tipoSeleccionado = tipo
    }

    fun mostrarCuadricula(mostrar: Boolean) {
        mostrarCuadricula = mostrar
        invalidate()
    }

    fun deshacerUltimo(): Triple<Int, Int, Int>? {
        if (historialColocacion.isEmpty()) return null

        // Sacar el último bloque colocado en orden exacto
        val (col, fila, altura) = historialColocacion.removeLast()
        val pila = bloques[fila][col]

        if (altura < pila.size && pila[altura].isNotEmpty()) {
            val tipo = pila[altura]
            pila[altura] = ""  // limpiar la posición

            // Guardar en pila de rehacer
            pilaRehacer.addLast(Triple(col * 1000 + fila, altura, tipo))
            invalidate()
            return Triple(col, fila, altura)
        }
        invalidate()
        return null
    }

    fun rehacerUltimo(): Triple<Int, Int, Int>? {
        if (pilaRehacer.isEmpty()) return null

        val (colFila, altura, tipo) = pilaRehacer.removeLast()
        val col = colFila / 1000
        val fila = colFila % 1000

        // Restaurar el bloque
        val pilaBloque = bloques[fila][col]
        while (pilaBloque.size <= altura) {
            pilaBloque.add("")
        }
        pilaBloque[altura] = tipo
        invalidate()
        return Triple(col, fila, altura)
    }

    fun limpiarPilaRehacer() {
        pilaRehacer.clear()
    }
    fun cargarBloques(listaBloques: List<Bloque>) {
        // Limpiar grid actual
        for (fila in 0 until GRID_SIZE)
            for (col in 0 until GRID_SIZE)
                bloques[fila][col].clear()

        // Reconstruir grid desde Room
        // posX = col, posY = altura, posZ = fila
        for (bloque in listaBloques) {
            val col = bloque.posX.coerceIn(0, GRID_SIZE - 1)
            val fila = bloque.posZ.coerceIn(0, GRID_SIZE - 1)
            val altura = bloque.posY

            // Asegurar que la pila tenga el tamaño correcto
            while (bloques[fila][col].size <= altura) {
                bloques[fila][col].add("")
            }
            bloques[fila][col][altura] = bloque.tipoId
        }

        invalidate()
    }

    fun obtenerBloquesParaGuardar(proyectoId: Int): List<Bloque> {
        val lista = mutableListOf<Bloque>()
        var orden = 0
        for (fila in 0 until GRID_SIZE) {
            for (col in 0 until GRID_SIZE) {
                val pila = bloques[fila][col]
                for (altura in pila.indices) {
                    val tipo = pila[altura]
                    if (tipo.isNotEmpty()) {
                        lista.add(
                            Bloque(
                                proyectoId = proyectoId,
                                tipoId = tipo,
                                posX = col,
                                posY = altura,
                                posZ = fila,
                                ordenColocacion = orden++
                            )
                        )
                    }
                }
            }
        }
        return lista
    }


    fun contarBloques(): Int =
        bloques.sumOf { fila -> fila.sumOf { it.size } }

    fun resetCamera() {
        cameraX = width / 2f
        cameraY = BLOCK_HEIGHT * 3
        rotacion = 0f
        zoom = 1f  // ← AGREGAR
        invalidate()
    }
    fun limpiarTodo() {
        for (fila in 0 until GRID_SIZE)
            for (col in 0 until GRID_SIZE)
                bloques[fila][col].clear()
        historialColocacion.clear()
        pilaRehacer.clear()
        invalidate()
    }

    private fun getDistance(event: MotionEvent): Float {
        val dx = event.getX(1) - event.getX(0)
        val dy = event.getY(1) - event.getY(0)
        return sqrt(dx * dx + dy * dy)
    }


    private fun cargarTexturas(context: Context) {
        val todosLosBloques = BLOQUES_ESTRUCTURA + BLOQUES_DECORACION +
                BLOQUES_NATURALEZA + BLOQUES_ESPECIALES
        todosLosBloques.forEach { nombre ->
            val resId = context.resources.getIdentifier(
                nombre, "drawable", context.packageName
            )
            if (resId != 0) {
                val bmp = BitmapFactory.decodeResource(context.resources, resId)
                if (bmp != null) texturas[nombre] = bmp
            }
        }
    }
}