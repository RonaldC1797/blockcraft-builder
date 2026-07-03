// data/repository/ProyectoRepository.kt
package com.cacuango.blockcraft.builder.data.repository

import com.cacuango.blockcraft.builder.data.local.dao.BloqueDao
import com.cacuango.blockcraft.builder.data.local.dao.ProyectoDao
import com.cacuango.blockcraft.builder.data.local.entity.Bloque
import com.cacuango.blockcraft.builder.data.local.entity.Proyecto
import com.cacuango.blockcraft.builder.data.local.entity.TipoBloque
import com.cacuango.blockcraft.builder.data.local.entity.HistorialAccion
import com.cacuango.blockcraft.builder.data.local.dao.TipoBloqueDao
import com.cacuango.blockcraft.builder.data.local.dao.HistorialAccionDao
import kotlinx.coroutines.flow.Flow


class ProyectoRepository(
    private val proyectoDao: ProyectoDao,
    private val bloqueDao: BloqueDao,
    private val tipoBloqueDao: TipoBloqueDao,        // ← agregar
    private val historialAccionDao: HistorialAccionDao // ← agregar



) {

    // ==================== PROYECTOS ====================

    fun obtenerProyectosFlow(): Flow<List<Proyecto>> {
        return proyectoDao.obtenerTodosLosProyectos()

    }

    suspend fun obtenerTodosLosProyectosSuspend(): List<Proyecto> {
        return proyectoDao.obtenerTodosLosProyectosSuspend()
    }
    suspend fun obtenerProyectoPorId(id: Int): Proyecto? {
        return proyectoDao.obtenerProyectoPorId(id)
    }

    suspend fun guardarProyecto(proyecto: Proyecto): Long {
        return if (proyecto.id == 0) {
            proyectoDao.insertarProyecto(proyecto)
        } else {
            proyectoDao.actualizarProyecto(proyecto)
            proyecto.id.toLong()
        }
    }

    suspend fun eliminarProyectoPorId(id: Int): Boolean {
        return try {
            bloqueDao.eliminarBloquesPorProyecto(id)
            proyectoDao.eliminarProyectoPorId(id)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun buscarProyectosPorNombre(query: String): List<Proyecto> {
        return proyectoDao.buscarProyectosPorNombre(query)
    }

    suspend fun contarProyectos(): Int {
        return proyectoDao.contarProyectos()
    }

    // ==================== BLOQUES ====================

    suspend fun obtenerBloquesPorProyecto(proyectoId: Int): List<Bloque> {
        return bloqueDao.obtenerBloquesPorProyecto(proyectoId)
    }

    suspend fun agregarBloque(bloque: Bloque): Long {
        return bloqueDao.insertarBloque(bloque)
    }

    suspend fun eliminarBloquePorId(id: Int): Boolean {
        return try {
            bloqueDao.eliminarBloquePorId(id)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun obtenerUltimoBloque(proyectoId: Int): Bloque? {
        return bloqueDao.obtenerUltimoBloque(proyectoId)
    }

    suspend fun contarBloquesPorProyecto(proyectoId: Int): Int {
        return bloqueDao.contarBloquesPorProyecto(proyectoId)
    }

    suspend fun eliminarBloquesPorProyecto(proyectoId: Int) {
        bloqueDao.eliminarBloquesPorProyecto(proyectoId)
    }


    // ==================== TIPOS DE BLOQUE ====================

    suspend fun obtenerTiposActivos(): List<TipoBloque> {
        return tipoBloqueDao.obtenerTiposActivos()
    }

    suspend fun obtenerTodosLosTipos(): List<TipoBloque> {
        return tipoBloqueDao.obtenerTodos()
    }

    suspend fun insertarTipoBloque(tipoBloque: TipoBloque): Long {
        return tipoBloqueDao.insertarTipoBloque(tipoBloque)
    }

    // ==================== HISTORIAL ====================

    suspend fun registrarAccion(accion: HistorialAccion) {
        // Verificar límite de 20 acciones (CA-02.2)
        val total = historialAccionDao.contarAccionesPorProyecto(accion.id_proyecto)
        if (total >= 20) {
            historialAccionDao.eliminarAccionMasAntigua(accion.id_proyecto)
        }
        historialAccionDao.insertarAccion(accion)
    }

    suspend fun obtenerUltimaAccion(proyectoId: Int): HistorialAccion? {
        return historialAccionDao.obtenerUltimaAccion(proyectoId)
    }

    suspend fun obtenerHistorial(proyectoId: Int): List<HistorialAccion> {
        return historialAccionDao.obtenerHistorialPorProyecto(proyectoId)
    }

    suspend fun limpiarHistorial(proyectoId: Int) {
        historialAccionDao.limpiarHistorialPorProyecto(proyectoId)
    }
}