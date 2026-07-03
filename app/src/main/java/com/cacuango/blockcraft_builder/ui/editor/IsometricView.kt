package com.cacuango.blockcraft.builder.ui.editor

import android.content.Context
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

    // ===== ESTADO =====
    private val bloques = Array(GRID_SIZE) {
        Array(GRID_SIZE) { mutableListOf<String>() }
    }
    private var tipoSeleccionado: String = "madera"
    private var mostrarCuadricula: Boolean = true

    // ===== COLORES =====
    private val coloresTipo = mapOf(
        "madera"   to Triple(0xFFB5651D.toInt(), 0xFF8B4513.toInt(), 0xFFA0522D.toInt()),
        "piedra"   to Triple(0xFF808080.toInt(), 0xFF696969.toInt(), 0xFF778899.toInt()),
        "ladrillo" to Triple(0xFFCC4444.toInt(), 0xFF993333.toInt(), 0xFFBB3333.toInt()),
        "tierra"   to Triple(0xFF8B6914.toInt(), 0xFF6B4F10.toInt(), 0xFF7A5C12.toInt()),
        "arena"    to Triple(0xFFF4D03F.toInt(), 0xFFD4AC0D.toInt(), 0xFFE8C51A.toInt()),
        "cristal"  to Triple(0xFF85C1E9.toInt(), 0xFF5DADE2.toInt(), 0xFF7FB3D3.toInt())
    )

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

    init {
        post {
            cameraX = width / 2f
            cameraY = BLOCK_HEIGHT * 3
        }
    }

    // ===== DIBUJO =====
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Ordenar celdas según rotación para perspectiva correcta
        val orden = generarOrdenDibujo()

        for ((fila, col) in orden) {
            val (sx, sy) = toScreen(col, fila, 0)

            if (sx < -BLOCK_WIDTH * 2 || sx > width + BLOCK_WIDTH * 2) continue
            if (sy < -BLOCK_HEIGHT * (MAX_HEIGHT + 2) || sy > height + BLOCK_HEIGHT * 2) continue

            dibujarCeldaBase(canvas, sx, sy)

            val pila = bloques[fila][col]
            for (altura in pila.indices) {
                val (bsx, bsy) = toScreen(col, fila, altura)
                dibujarBloque(canvas, bsx, bsy, pila[altura])
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
        val colores = coloresTipo[tipo] ?: Triple(Color.GRAY, Color.DKGRAY, Color.LTGRAY)
        val bw = BLOCK_WIDTH * zoom
        val bh = BLOCK_HEIGHT * zoom
        val bd = BLOCK_DEPTH * zoom

        paintTop.color = colores.first
        val top = Path().apply {
            moveTo(sx, sy - bd)
            lineTo(sx + bw / 2, sy + bh / 2 - bd)
            lineTo(sx + bw, sy - bd)
            lineTo(sx + bw / 2, sy - bh / 2 - bd)
            close()
        }
        canvas.drawPath(top, paintTop)
        canvas.drawPath(top, paintBorder)

        paintLeft.color = colores.second
        val left = Path().apply {
            moveTo(sx, sy - bd)
            lineTo(sx + bw / 2, sy + bh / 2 - bd)
            lineTo(sx + bw / 2, sy + bh / 2)
            lineTo(sx, sy)
            close()
        }
        canvas.drawPath(left, paintLeft)
        canvas.drawPath(left, paintBorder)

        paintRight.color = colores.third
        val right = Path().apply {
            moveTo(sx + bw, sy - bd)
            lineTo(sx + bw / 2, sy + bh / 2 - bd)
            lineTo(sx + bw / 2, sy + bh / 2)
            lineTo(sx + bw, sy)
            close()
        }
        canvas.drawPath(right, paintRight)
        canvas.drawPath(right, paintBorder)
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

        // Invertir la proyección isométrica y rotación
        val isoX = (relX / (BLOCK_WIDTH / 2) + relY / (BLOCK_HEIGHT / 2)) / 2
        val isoZ = (relY / (BLOCK_HEIGHT / 2) - relX / (BLOCK_WIDTH / 2)) / 2

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
                            }

                            if (isDragging) {
                                cameraX += dx
                                cameraY += dy
                                invalidate()
                            }

                            lastTouchX = event.x
                            lastTouchY = event.y
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        if (!isDragging && !isRotating) {
                            val (col, fila) = toGrid(event.x, event.y)
                            val pila = bloques[fila][col]

                            if (pila.size < MAX_HEIGHT) {
                                val altura = pila.size
                                pila.add(tipoSeleccionado)
                                onBloqueColocado?.invoke(col, fila, altura, tipoSeleccionado)
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
        for (fila in GRID_SIZE - 1 downTo 0) {
            for (col in GRID_SIZE - 1 downTo 0) {
                val pila = bloques[fila][col]
                if (pila.isNotEmpty()) {
                    val altura = pila.size - 1
                    pila.removeAt(altura)
                    invalidate()
                    return Triple(col, fila, altura)
                }
            }
        }
        return null
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
        invalidate()
    }

    private fun getDistance(event: MotionEvent): Float {
        val dx = event.getX(1) - event.getX(0)
        val dy = event.getY(1) - event.getY(0)
        return sqrt(dx * dx + dy * dy)
    }
}