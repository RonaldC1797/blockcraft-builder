package com.cacuango.blockcraft.builder.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
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
    val camaraZ: Float,
    val categoria: String = "Todos"
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

@Entity(tableName = "TipoBloque")
data class TipoBloque(
    @PrimaryKey
    val id: String,           // 'madera', 'piedra', 'ladrillo'
    val nombre_display: String, // "Madera", "Piedra", "Ladrillo"
    val activo: Int = 1,      // 1 = activo, 0 = desactivado
    val orden_paleta: Int     // posición en la barra de selección
)

@Entity(
    tableName = "HistorialAccion",
    foreignKeys = [
        ForeignKey(
            entity = Proyecto::class,
            parentColumns = ["id"],
            childColumns = ["id_proyecto"],
            onDelete = ForeignKey.CASCADE  // si se borra el proyecto, se borra su historial
        )
    ]
)
data class HistorialAccion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val id_proyecto: Int,
    val tipo_accion: String,   // "COLOCAR" o "DESHACER"
    val tipo_bloque: String,   // copia del tipo, no FK
    val pos_x: Int,
    val pos_y: Int,
    val pos_z: Int,
    val orden: Int,            // máximo 20 (CA-02.2)
    val fecha_hora: String
)