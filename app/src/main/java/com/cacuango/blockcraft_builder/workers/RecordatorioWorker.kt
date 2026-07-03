package com.cacuango.blockcraft_builder.workers


import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cacuango.blockcraft.builder.BlockcraftApp
import com.cacuango.blockcraft.builder.R
import com.cacuango.blockcraft.builder.data.local.database.AppDatabase


class RecordatorioWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Consultar si hay proyectos en la base de datos
            val db = AppDatabase.getInstance(context)
            val proyectos = db.proyectoDao().obtenerTodosLosProyectosSuspend()

            if (proyectos.isNotEmpty()) {
                val proyectoReciente = proyectos.first()
                enviarNotificacion(proyectoReciente.nombre)
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun enviarNotificacion(nombreProyecto: String) {
        val notificationManager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        val notificacion = NotificationCompat.Builder(
            context,
            BlockcraftApp.CANAL_RECORDATORIOS  // Canal creado en BlockcraftApp
        )
            .setSmallIcon(R.drawable.ic_empty_world)
            .setContentTitle("¡Tu construcción te espera!")
            .setContentText(
                "\"$nombreProyecto\" lleva días sin actualizarse. ¡Sigue construyendo!"
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)  // Se descarta al tocarla
            .build()

        notificationManager.notify(
            NOTIFICATION_ID,
            notificacion
        )
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        const val WORK_NAME = "recordatorio_proyecto"
    }
}