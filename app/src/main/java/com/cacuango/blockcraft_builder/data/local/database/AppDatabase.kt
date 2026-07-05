package com.cacuango.blockcraft.builder.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.cacuango.blockcraft.builder.data.local.dao.BloqueDao
import com.cacuango.blockcraft.builder.data.local.dao.ProyectoDao
import com.cacuango.blockcraft.builder.data.local.entity.Bloque
import com.cacuango.blockcraft.builder.data.local.entity.Proyecto
import com.cacuango.blockcraft.builder.data.local.entity.TipoBloque
import com.cacuango.blockcraft.builder.data.local.entity.HistorialAccion
import com.cacuango.blockcraft.builder.data.local.dao.HistorialAccionDao
import com.cacuango.blockcraft.builder.data.local.dao.TipoBloqueDao

@Database(
    entities = [
        Proyecto::class,
        Bloque::class,
        TipoBloque::class,
        HistorialAccion::class
    ],
    version = 3,  // ← cambiar de 2 a 3
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun proyectoDao(): ProyectoDao
    abstract fun bloqueDao(): BloqueDao
    abstract fun tipoBloqueDao(): TipoBloqueDao
    abstract fun historialAccionDao(): HistorialAccionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // ✅ Migración de versión 2 a 3
        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE proyectos ADD COLUMN categoria TEXT NOT NULL DEFAULT 'Todos'"
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "blockcraft_database"
                )
                    .fallbackToDestructiveMigration() // ← agregar esta línea
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}