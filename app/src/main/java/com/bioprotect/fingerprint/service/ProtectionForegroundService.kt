package com.bioprotect.fingerprint.service

import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.bioprotect.fingerprint.R
import com.bioprotect.fingerprint.data.AppPreferences
import com.bioprotect.fingerprint.receiver.RestartProtectionReceiver
import com.bioprotect.fingerprint.util.EventLogger
import com.bioprotect.fingerprint.util.PermissionChecker
import com.bioprotect.fingerprint.ui.MainActivity

class ProtectionForegroundService : Service() {

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        scheduleSelfRestart("task_removed")
    }

    override fun onDestroy() {
        scheduleSelfRestart("service_destroyed")
        super.onDestroy()
    }

    private fun buildNotification(): Notification {
        val launchIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = android.app.PendingIntent.getActivity(
            this,
            1,
            launchIntent,
            android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.notificacion_titulo))
            .setContentText(getString(R.string.notificacion_texto))
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun createChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "BioProtect",
                NotificationManager.IMPORTANCE_LOW
            )
            channel.setSound(null, null)
            channel.enableVibration(false)
            manager.createNotificationChannel(channel)
        }
    }

    private fun scheduleSelfRestart(reason: String) {
        if (!AppPreferences.isProtectionEnabled()) return
        if (!PermissionChecker.hasAllEssentialPermissions(this)) return

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val restartIntent = Intent(this, RestartProtectionReceiver::class.java).apply {
            action = ACTION_RESTART_PROTECTION
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            2001,
            restartIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerAt = System.currentTimeMillis() + 1200L
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        EventLogger.log("Rearme programado tras $reason")
    }

    companion object {
        const val ACTION_RESTART_PROTECTION = "com.bioprotect.fingerprint.action.RESTART_PROTECTION"
        private const val CHANNEL_ID = "bioprotect_foreground"
        private const val NOTIFICATION_ID = 1001
    }
}
