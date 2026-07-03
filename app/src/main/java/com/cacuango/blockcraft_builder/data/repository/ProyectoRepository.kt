// data/repository/ProyectoRepository.kt
package com.cacuango.blockcraft.builder.data.repository

import com.cacuango.blockcraft.builder.data.local.dao.BloqueDao
import com.cacuango.blockcraft.builder.data.local.dao.ProyectoDao
import com.cacuango.blockcraft.builder.data.local.entity.Bloque
import com.cacuango.blockcraft.builder.data.local.entity.Proyecto

class ProyectoRepository(
    private val proyectoDao: ProyectoDao,
    private val bloqueDao: BloqueDao
) {

    // ==================== PROYECTOS ====================

    suspend fun obtenerTodosLosProyectos(): List<Proyecto> {
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
}