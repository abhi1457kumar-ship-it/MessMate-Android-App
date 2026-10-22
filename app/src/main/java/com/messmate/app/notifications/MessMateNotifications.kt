package com.messmate.app.notifications

import android.content.Context
import android.content.Intent
import android.os.Build
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.messmate.app.MainActivity
import com.messmate.app.R

class MessMateNotifications(private val context: Context) {
    fun enabled(): Boolean = NotificationManagerCompat.from(context).areNotificationsEnabled()

    fun showDiningUpdate(message: String) {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                android.app.NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.notification_channel_name),
                    android.app.NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description = context.getString(R.string.notification_channel_description)
                },
            )
        }

        val launchApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_messmate)
            .setContentTitle("MessMate • Dining update")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setColor(0xFF173F35.toInt())
            .setContentIntent(launchApp)
            .setAutoCancel(true)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }

    private companion object {
        const val CHANNEL_ID = "messmate_dining_updates"
        const val NOTIFICATION_ID = 4101
    }
}
