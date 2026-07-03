// data/local/dao/ProyectoDao.kt
package com.cacuango.blockcraft.builder.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.cacuango.blockcraft.builder.data.local.entity.Proyecto

@Dao
interface ProyectoDao {

    // ===== INSERT =====
    @Insert
    suspend fun insertarProyecto(proyecto: Proyecto): Long

    // ===== UPDATE =====
    @Update
    suspend fun actualizarProyecto(proyecto: Proyecto)

    // ===== DELETE =====
    @Delete
    suspend fun eliminarProyecto(proyecto: Proyecto)

    @Query("DELETE FROM proyectos WHERE id = :id")
    suspend fun eliminarProyectoPorId(id: Int)

    // ===== SELECT - OBTENER TODOS =====
    @Query("SELECT * FROM proyectos ORDER BY fechaModificacion DESC")
    suspend fun obtenerTodosLosProyectosSuspend(): List<Proyecto>

    // ===== SELECT - OBTENER POR ID =====
    @Query("SELECT * FROM proyectos WHERE id = :id")
    suspend fun obtenerProyectoPorId(id: Int): Proyecto?

    // ===== SELECT - BUSCAR POR NOMBRE =====
    @Query("SELECT * FROM proyectos WHERE nombre LIKE '%' || :query || '%' ORDER BY fechaModificacion DESC")
    suspend fun buscarProyectosPorNombre(query: String): List<Proyecto>

    // ===== SELECT - CONTAR =====
    @Query("SELECT COUNT(*) FROM proyectos")
    suspend fun contarProyectos(): Int
}