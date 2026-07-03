package com.cacuango.blockcraft_builder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cacuango.blockcraft.builder.data.local.entity.HistorialAccion

@Dao
interface HistorialAccionDao {

    // Registrar una nueva acción (COLOCAR o DESHACER)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarAccion(accion: HistorialAccion): Long

    // Obtener todo el historial de un proyecto ordenado por orden DESC
    @Query("SELECT * FROM HistorialAccion WHERE id_proyecto = :proyectoId ORDER BY orden DESC")
    suspend fun obtenerHistorialPorProyecto(proyectoId: Int): List<HistorialAccion>

    // Obtener la última acción de un proyecto (para deshacer)
    @Query("SELECT * FROM HistorialAccion WHERE id_proyecto = :proyectoId ORDER BY orden DESC LIMIT 1")
    suspend fun obtenerUltimaAccion(proyectoId: Int): HistorialAccion?

    // Contar acciones de un proyecto (máximo 20 según CA-02.2)
    @Query("SELECT COUNT(*) FROM HistorialAccion WHERE id_proyecto = :proyectoId")
    suspend fun contarAccionesPorProyecto(proyectoId: Int): Int

    // Eliminar la acción más antigua cuando se supera el límite de 20
    @Query("""
        DELETE FROM HistorialAccion 
        WHERE id = (
            SELECT id FROM HistorialAccion 
            WHERE id_proyecto = :proyectoId 
            ORDER BY orden ASC 
            LIMIT 1
        )
    """)
    suspend fun eliminarAccionMasAntigua(proyectoId: Int)

    // Eliminar todo el historial de un proyecto
    @Query("DELETE FROM HistorialAccion WHERE id_proyecto = :proyectoId")
    suspend fun limpiarHistorialPorProyecto(proyectoId: Int)
}