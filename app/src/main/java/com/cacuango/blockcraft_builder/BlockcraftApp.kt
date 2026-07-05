package com.cacuango.blockcraft.builder

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.cacuango.blockcraft_builder.workers.RecordatorioWorker
import java.util.concurrent.TimeUnit
import com.cacuango.blockcraft.builder.data.local.database.AppDatabase
import com.cacuango.blockcraft.builder.data.local.entity.TipoBloque
import com.cacuango.blockcraft.builder.data.local.entity.HistorialAccion
import kotlinx.coroutines.launch
import com.cacuango.blockcraft.builder.data.local.dao.TipoBloqueDao
import com.cacuango.blockcraft.builder.data.local.dao.HistorialAccionDao


import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
class BlockcraftApp : Application() {

    companion object {
        // IDs de canales — se usan al crear cada notificación
        const val CANAL_RECORDATORIOS = "canal_recordatorios"
        const val CANAL_GUARDADO      = "canal_guardado"
        const val CANAL_LOGROS        = "canal_logros"
        const val CANAL_EXPORTACION   = "canal_exportacion"
    }

    override fun onCreate() {
        super.onCreate()
        crearCanalesDeNotificacion()
        programarRecordatorio()
        poblarTiposDeBloque()  // ← agregar
    }


    private fun programarRecordatorio() {
        // Crear la solicitud periódica — se repite cada 3 días
        val solicitud = PeriodicWorkRequestBuilder<RecordatorioWorker>(
            3, TimeUnit.DAYS          // Intervalo: cada 3 días
        ).build()

        // Programar con KEEP: si ya existe una tarea con ese nombre,
        // la mantiene y no crea una duplicada
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            RecordatorioWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            solicitud
        )
    }


    private fun crearCanalesDeNotificacion() {
        // Los canales solo existen desde Android 8 (API 26)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val manager = getSystemService(NotificationManager::class.java)

            // ── Canal 1: Recordatorios de proyecto abandonado ──────────────
            val canalRecordatorios = NotificationChannel(
                CANAL_RECORDATORIOS,
                "Recordatorios de construcción",
                NotificationManager.IMPORTANCE_DEFAULT  // Sonido + notificación
            ).apply {
                description = "Te avisa cuando llevas días sin abrir un proyecto"
            }

            // ── Canal 2: Guardado automático ───────────────────────────────
            val canalGuardado = NotificationChannel(
                CANAL_GUARDADO,
                "Guardado automático",
                NotificationManager.IMPORTANCE_LOW  // Sin sonido, solo ícono
            ).apply {
                description = "Confirma que tu construcción fue guardada"
            }

            // ── Canal 3: Logros ────────────────────────────────────────────
            val canalLogros = NotificationChannel(
                CANAL_LOGROS,
                "Logros desbloqueados",
                NotificationManager.IMPORTANCE_HIGH  // Heads-up (aparece flotante)
            ).apply {
                description = "Te notifica cuando alcanzas un hito de bloques"
            }

            // ── Canal 4: Exportación completada ───────────────────────────
            val canalExportacion = NotificationChannel(
                CANAL_EXPORTACION,
                "Exportación de capturas",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Avisa cuando tu captura está lista en la galería"
            }

            // Registrar todos los canales en el sistema
            manager.createNotificationChannels(listOf(
                canalRecordatorios,
                canalGuardado,
                canalLogros,
                canalExportacion
            ))
        }
    }

    private fun poblarTiposDeBloque() {
        val tiposIniciales = listOf(
            TipoBloque(id = "madera",   nombre_display = "Madera",   activo = 1, orden_paleta = 1),
            TipoBloque(id = "piedra",   nombre_display = "Piedra",   activo = 1, orden_paleta = 2),
            TipoBloque(id = "ladrillo", nombre_display = "Ladrillo", activo = 1, orden_paleta = 3),
            TipoBloque(id = "tierra",   nombre_display = "Tierra",   activo = 1, orden_paleta = 4),
            TipoBloque(id = "arena",    nombre_display = "Arena",    activo = 1, orden_paleta = 5),
            TipoBloque(id = "cristal",  nombre_display = "Cristal",  activo = 1, orden_paleta = 6)
        )

        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val db = AppDatabase.getInstance(applicationContext)
                tiposIniciales.forEach { tipo ->
                    db.tipoBloqueDao().insertarTipoBloque(tipo)
                }
            } catch (e: Exception) {
                android.util.Log.e("BlockcraftApp", "Error poblando tipos: ${e.message}")
            }
        }
    }
}