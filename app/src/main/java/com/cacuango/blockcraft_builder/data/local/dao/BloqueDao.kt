// data/local/dao/BloqueDao.kt
package com.cacuango.blockcraft.builder.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.cacuango.blockcraft.builder.data.local.entity.Bloque

@Dao
interface BloqueDao {

    // ===== INSERT =====
    @Insert
    suspend fun insertarBloque(bloque: Bloque): Long

    // ===== DELETE =====
    @Delete
    suspend fun eliminarBloque(bloque: Bloque)

    @Query("DELETE FROM bloques WHERE id = :id")
    suspend fun eliminarBloquePorId(id: Int)

    @Query("DELETE FROM bloques WHERE proyectoId = :proyectoId")
    suspend fun eliminarBloquesPorProyecto(proyectoId: Int)

    // ===== SELECT - OBTENER BLOQUES DE UN PROYECTO =====
    @Query("SELECT * FROM bloques WHERE proyectoId = :proyectoId ORDER BY ordenColocacion ASC")
    suspend fun obtenerBloquesPorProyecto(proyectoId: Int): List<Bloque>

    // ===== SELECT - OBTENER ÚLTIMO BLOQUE =====
    @Query("SELECT * FROM bloques WHERE proyectoId = :proyectoId ORDER BY ordenColocacion DESC LIMIT 1")
    suspend fun obtenerUltimoBloque(proyectoId: Int): Bloque?

    // ===== SELECT - CONTAR BLOQUES =====
    @Query("SELECT COUNT(*) FROM bloques WHERE proyectoId = :proyectoId")
    suspend fun contarBloquesPorProyecto(proyectoId: Int): Int

    // ===== SELECT - BLOQUES POR TIPO =====
    @Query("SELECT * FROM bloques WHERE proyectoId = :proyectoId AND tipoId = :tipoId")
    suspend fun obtenerBloquesPorTipo(proyectoId: Int, tipoId: String): List<Bloque>
}