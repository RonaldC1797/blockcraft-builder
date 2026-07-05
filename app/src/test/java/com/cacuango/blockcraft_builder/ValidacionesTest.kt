package com.cacuango.blockcraft_builder

import org.junit.Test

import org.junit.Assert.*



class ValidacionesTest {
    private fun validarNombreProyecto(nombre: String): String? {
        if (nombre.isEmpty()) return "El nombre no puede estar vacío"
        if (nombre.isBlank()) return "El nombre no puede ser solo espacios"
        if (nombre.length < 3) return "El nombre debe tener al menos 3 caracteres"
        if (!nombre.matches(Regex("^[a-zA-Z0-9 ]+\$")))
            return "Solo letras, números y espacios"
        return null
    }

    private fun formatearFecha(timestamp: String): String {
        return try {
            val ms = timestamp.toLong()
            val sdf = java.text.SimpleDateFormat(
                "dd/MM/yyyy HH:mm", java.util.Locale.getDefault()
            )
            sdf.format(java.util.Date(ms))
        } catch (e: Exception) {
            timestamp
        }
    }

    // ════════════════════════════════════════════
    // VALIDACIÓN DE NOMBRE
    // ════════════════════════════════════════════

    // ── TEST 1 ───────────────────────────────────
    // Verifica que un nombre vacío es rechazado.
    @Test
    fun validarNombre_vacio_debeRetornarMensajeDeError() {
        // Arrange
        val nombreVacio = ""
        // Act
        val resultado = validarNombreProyecto(nombreVacio)
        // Assert
        assertNotNull(resultado)
        assertEquals("El nombre no puede estar vacío", resultado)    }

    // ── TEST 1B — CASO DE BORDE ──────────────────
    // Solo espacios no está vacío pero tampoco es
    // un nombre válido — el regex debe rechazarlo.
    @Test
    fun validarNombre_soloEspacios_debeRetornarMensajeDeError() {
        // Arrange — borde: no vacío pero sin contenido real
        val nombreSoloEspacios = "   "
        // Act
        val resultado = validarNombreProyecto(nombreSoloEspacios)
        // Assert
        // assertNotNull: los espacios solos deben generar error
        assertNotNull(resultado)
        assertEquals("El nombre no puede ser solo espacios", resultado)
    }

    // ── TEST 2 ───────────────────────────────────
    // Verifica que un nombre de 2 caracteres es rechazado.
    @Test
    fun validarNombre_menorDeTresCaracteres_debeRetornarMensajeDeError() {
        // Arrange
        val nombreCorto = "AB"
        // Act
        val resultado = validarNombreProyecto(nombreCorto)
        // Assert
        assertNotNull(resultado)
        assertEquals("El nombre debe tener al menos 3 caracteres", resultado)
    }

    // ── TEST 2B — CASO DE BORDE ──────────────────
    // Exactamente 3 caracteres es el límite mínimo
    // válido — debe pasar sin error.
    @Test
    fun validarNombre_exactamenteTresCaracteres_debeRetornarNull() {
        // Arrange — borde: longitud mínima exacta
        val nombreLimite = "ABC"
        // Act
        val resultado = validarNombreProyecto(nombreLimite)
        // Assert
        // assertNull: 3 caracteres es válido, no debe haber error
        assertNull(resultado)
    }

    // ── TEST 3 ───────────────────────────────────
    // Verifica que caracteres especiales son rechazados.
    @Test
    fun validarNombre_conCaracteresEspeciales_debeRetornarMensajeDeError() {
        // Arrange
        val nombreInvalido = "Castillo#1"
        // Act
        val resultado = validarNombreProyecto(nombreInvalido)
        // Assert
        assertNotNull(resultado)
        assertEquals("Solo letras, números y espacios", resultado)
    }

    // ── TEST 3B — CASO DE BORDE ──────────────────
    // Un nombre con solo números es técnicamente válido
    // según el regex — verifica que la función lo acepta.
    @Test
    fun validarNombre_soloNumeros_debeRetornarNull() {
        // Arrange — borde: válido según regex pero inusual
        val nombreSoloNumeros = "123"
        // Act
        val resultado = validarNombreProyecto(nombreSoloNumeros)
        // Assert
        // assertNull: el regex permite números solos
        assertNull(resultado)
    }

    // ── TEST 4 ───────────────────────────────────
    // Verifica que un nombre correcto pasa todas las validaciones.
    @Test
    fun validarNombre_nombreValido_debeRetornarNull() {
        // Arrange
        val nombreValido = "Castillo Medieval"
        // Act
        val resultado = validarNombreProyecto(nombreValido)
        // Assert
        assertNull(resultado)
    }

    // ── TEST 4B — CASO DE BORDE ──────────────────
    // Un nombre muy largo (100 caracteres) no tiene
    // límite superior definido — debe pasar igual.
    @Test
    fun validarNombre_nombreMuyLargo_debeRetornarNull() {
        // Arrange — borde: límite superior extremo
        val nombreLargo = "A".repeat(100)
        // Act
        val resultado = validarNombreProyecto(nombreLargo)
        // Assert
        // assertNull: no hay límite máximo definido, debe pasar
        assertNull(resultado)
    }

    // ════════════════════════════════════════════
    // FORMATEO DE FECHA
    // ════════════════════════════════════════════

    // ── TEST 5 ───────────────────────────────────
    // Verifica que un timestamp válido se convierte
    // correctamente a formato legible.
    @Test
    fun formatearFecha_timestampValido_debeRetornarFechaLegible() {
        // Arrange
        val timestamp = "1751414400000"
        // Act
        val resultado = formatearFecha(timestamp)
        // Assert
        assertTrue(resultado.length == 16)
        assertTrue(resultado.contains("/"))
    }

    // ── TEST 5B — CASO DE BORDE ──────────────────
    // Timestamp cero representa el origen del tiempo
    @Test
    fun formatearFecha_timestampCero_debeRetornarFechaValida() {
        // Arrange — borde: valor mínimo posible del timestamp
        val timestampCero = "0"
        // Act
        val resultado = formatearFecha(timestampCero)
        // Assert
        // Verificamos formato válido dd/MM/yyyy HH:mm = 16 caracteres
        // sin asumir el año exacto porque depende del timezone del sistema
        assertEquals(16, resultado.length)
        assertTrue(resultado.contains("/"))
    }

    // ── TEST 5C — CASO DE BORDE ──────────────────
    // Timestamp negativo representa una fecha antes
    // de 1970 — la función no debe lanzar excepción.
    @Test
    fun formatearFecha_timestampNegativo_debeRetornarFechaValida() {
        // Arrange — borde: timestamp negativo (antes de 1970)
        val timestampNegativo = "-86400000"
        // Act
        val resultado = formatearFecha(timestampNegativo)
        // Assert
        // assertTrue: aunque sea negativo, debe formatear sin error
        assertTrue(resultado.contains("/"))
    }

    // ── TEST 6 ───────────────────────────────────
    // Verifica que texto inválido retorna el original.
    @Test
    fun formatearFecha_textoInvalido_debeRetornarElTextoOriginal() {
        // Arrange
        val textoInvalido = "no-es-un-timestamp"
        // Act
        val resultado = formatearFecha(textoInvalido)
        // Assert
        assertEquals("no-es-un-timestamp", resultado)
    }

    // ── TEST 6B — CASO DE BORDE ──────────────────
    // String vacío falla el toLong() de forma diferente
    // a un texto con letras — debe retornar el string
    // vacío original sin lanzar excepción.
    @Test
    fun formatearFecha_stringVacio_debeRetornarStringVacio() {
        // Arrange — borde: entrada vacía
        val vacio = ""
        // Act
        val resultado = formatearFecha(vacio)
        // Assert
        // assertEquals: retorna el string vacío original
        assertEquals("", resultado)
    }
}