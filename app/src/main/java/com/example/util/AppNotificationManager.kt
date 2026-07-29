package com.example.util

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object NotificationChannels {
    const val DOWNLOADS = "channel_downloads"
    const val AUTO_UPLOAD = "channel_auto_upload"
    const val AI_PROCESSING = "channel_ai_processing"
    const val SECURITY_ALERTS = "channel_security_alerts"
    const val SYSTEM = "channel_system"
}

interface AppNotificationManager {
    fun createChannels()
    fun showProgressNotification(channelId: String, id: Int, title: String, progress: Int, maxProgress: Int, groupKey: String? = null): Notification
    fun showCompletionNotification(channelId: String, id: Int, title: String, message: String, intent: PendingIntent?)
    fun showAlertNotification(channelId: String, id: Int, title: String, message: String, fullScreenIntent: PendingIntent?)
    fun cancelNotification(id: Int)
}

class AppNotificationManagerImpl(
    private val context: Context,
    private val settingsRepository: SettingsRepository
) : AppNotificationManager {

    private val notificationManager = NotificationManagerCompat.from(context)
    private var lastProgressUpdateTime = 0L

    override fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(NotificationChannels.DOWNLOADS, "Downloads", NotificationManager.IMPORTANCE_LOW).apply {
                    description = "Notifikasi progress dan hasil download"
                },
                NotificationChannel(NotificationChannels.AUTO_UPLOAD, "Auto Upload", NotificationManager.IMPORTANCE_LOW).apply {
                    description = "Notifikasi upload di background"
                },
                NotificationChannel(NotificationChannels.AI_PROCESSING, "AI Processing", NotificationManager.IMPORTANCE_LOW).apply {
                    description = "Notifikasi proses AI editor"
                },
                NotificationChannel(NotificationChannels.SECURITY_ALERTS, "Security Alerts", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Peringatan ancaman dan keamanan"
                },
                NotificationChannel(NotificationChannels.SYSTEM, "System", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Update dan backup"
                }
            )
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannels(channels)
        }
    }

    private fun hasPermissionAndEnabled(channelId: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        
        // Cek preferensi dari Settings Manager
        return runBlocking {
            when (channelId) {
                // Asumsi SettingsKeys ditambahkan key ini
                NotificationChannels.AI_PROCESSING -> settingsRepository.getSetting(SettingKey.BooleanKey("notif_ai", true)).first()
                NotificationChannels.AUTO_UPLOAD -> settingsRepository.getSetting(SettingKey.BooleanKey("notif_upload", true)).first()
                else -> true
            }
        }
    }

    override fun showProgressNotification(
        channelId: String,
        id: Int,
        title: String,
        progress: Int,
        maxProgress: Int,
        groupKey: String?
    ): Notification {
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(title)
            .setContentText(if (maxProgress > 0) "$progress / $maxProgress" else "Processing...")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(maxProgress, progress, maxProgress == 0)

        groupKey?.let { 
            builder.setGroup(it) 
            builder.setGroupSummary(false)
        }

        val notification = builder.build()

        // Debounce: update UI tiap 1 detik maksimal supaya tidak spam/bikin lag UI
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastProgressUpdateTime > 1000 || progress == maxProgress) {
            if (hasPermissionAndEnabled(channelId)) {
                try {
                    notificationManager.notify(id, notification)
                } catch (e: SecurityException) { }
            }
            lastProgressUpdateTime = currentTime
        }
        return notification
    }

    override fun showCompletionNotification(
        channelId: String, id: Int, title: String, message: String, intent: PendingIntent?
    ) {
        if (!hasPermissionAndEnabled(channelId)) return

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        intent?.let { builder.setContentIntent(it) }

        try {
            notificationManager.notify(id, builder.build())
        } catch (e: SecurityException) { }
    }

    override fun showAlertNotification(
        channelId: String, id: Int, title: String, message: String, fullScreenIntent: PendingIntent?
    ) {
        if (!hasPermissionAndEnabled(channelId)) return

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)

        fullScreenIntent?.let { builder.setFullScreenIntent(it, true) }

        try {
            notificationManager.notify(id, builder.build())
        } catch (e: SecurityException) { }
    }

    override fun cancelNotification(id: Int) {
        notificationManager.cancel(id)
    }
}
