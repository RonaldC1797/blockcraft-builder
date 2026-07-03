package com.cacuango.blockcraft.builder.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "proyectos")
data class Proyecto(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val fechaCreacion: String,
    val fechaModificacion: String,
    val camaraX: Float,
    val camaraY: Float,
    val camaraZ: Float

)

// ✅ AGREGAR ESTA ENTIDAD - Te falta esta
@Entity(tableName = "bloques")
data class Bloque(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val proyectoId: Int,
    val tipoId: String,
    val posX: Int,
    val posY: Int,
    val posZ: Int,
    val ordenColocacion: Int = 0
)