package com.cacuango.blockcraft_builder.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cacuango.blockcraft.builder.data.local.entity.TipoBloque

@Dao
interface TipoBloqueDao {

    // Insertar un nuevo tipo de bloque
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTipoBloque(tipoBloque: TipoBloque): Long

    // Obtener todos los tipos activos ordenados por posición en paleta
    @Query("SELECT * FROM TipoBloque WHERE activo = 1 ORDER BY orden_paleta ASC")
    suspend fun obtenerTiposActivos(): List<TipoBloque>

    // Obtener todos los tipos sin filtro
    @Query("SELECT * FROM TipoBloque ORDER BY orden_paleta ASC")
    suspend fun obtenerTodos(): List<TipoBloque>

    // Obtener un tipo por su id (ej: 'madera', 'piedra')
    @Query("SELECT * FROM TipoBloque WHERE id = :id")
    suspend fun obtenerPorId(id: String): TipoBloque?

    // Actualizar un tipo existente
    @Update
    suspend fun actualizarTipoBloque(tipoBloque: TipoBloque)

    // Eliminar un tipo
    @Delete
    suspend fun eliminarTipoBloque(tipoBloque: TipoBloque)

    // Desactivar un tipo sin eliminarlo
    @Query("UPDATE TipoBloque SET activo = 0 WHERE id = :id")
    suspend fun desactivarTipo(id: String)
}