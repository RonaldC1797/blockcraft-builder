// data/local/database/AppDatabase.kt
package com.cacuango.blockcraft.builder.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.cacuango.blockcraft.builder.data.local.dao.BloqueDao
import com.cacuango.blockcraft.builder.data.local.dao.ProyectoDao
import com.cacuango.blockcraft.builder.data.local.entity.Bloque
import com.cacuango.blockcraft.builder.data.local.entity.Proyecto

@Database(
    entities = [Proyecto::class, Bloque::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun proyectoDao(): ProyectoDao
    abstract fun bloqueDao(): BloqueDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "blockcraft_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}