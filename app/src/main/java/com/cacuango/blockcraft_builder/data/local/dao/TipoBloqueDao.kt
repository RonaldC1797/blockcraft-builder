package com.cacuango.blockcraft.builder.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cacuango.blockcraft.builder.data.local.entity.TipoBloque

@Dao
interface TipoBloqueDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTipoBloque(tipoBloque: TipoBloque): Long

    @Query("SELECT * FROM TipoBloque WHERE activo = 1 ORDER BY orden_paleta ASC")
    suspend fun obtenerTiposActivos(): List<TipoBloque>

    @Query("SELECT * FROM TipoBloque ORDER BY orden_paleta ASC")
    suspend fun obtenerTodos(): List<TipoBloque>

    @Query("SELECT * FROM TipoBloque WHERE id = :id")
    suspend fun obtenerPorId(id: String): TipoBloque?

    @Update
    suspend fun actualizarTipoBloque(tipoBloque: TipoBloque)

    @Delete
    suspend fun eliminarTipoBloque(tipoBloque: TipoBloque)

    @Query("UPDATE TipoBloque SET activo = 0 WHERE id = :id")
    suspend fun desactivarTipo(id: String)
}