package com.spacemint.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

object NotificationHelper {

    private const val CHANNEL_ID    = "spacemint_reminders"
    private const val CHANNEL_NAME  = "Daily Reminders"
    private const val NOTIF_MORNING = 2001
    private const val NOTIF_EVENING = 2002

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "SpaceMint daily clean up reminders"
            enableVibration(true)
            enableLights(true)
        }
        val manager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    fun sendReminder(context: Context, isMorning: Boolean = true) {

        // main tap — opens app
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "review")
        }
        val mainPending = PendingIntent.getActivity(
            context, 0, mainIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // review now action button
        val reviewIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "review")
        }
        val reviewPending = PendingIntent.getActivity(
            context, 1, reviewIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // later action button
        val dismissIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val dismissPending = PendingIntent.getActivity(
            context, 2, dismissIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // morning messages
        val morningMessages = listOf(
            Pair(
                "Good morning! Time to breathe 🌿",
                "5 files waiting for review. Takes under 2 minutes!"
            ),
            Pair(
                "Start fresh today 🌱",
                "Review 5 files and free up some space this morning."
            ),
            Pair(
                "Morning clean up time ☀️",
                "Keep your streak going — 5 quick reviews await."
            )
        )

        // evening messages — only sent if morning session was skipped
        val eveningMessages = listOf(
            Pair(
                "Still time to clean up today 🌙",
                "You haven't reviewed your files yet. Takes under 2 minutes!"
            ),
            Pair(
                "Evening reminder 🌿",
                "5 files are waiting. Review before the day ends!"
            ),
            Pair(
                "Don't break your streak! 🔥",
                "Quick review before bed — just 5 files."
            )
        )

        val message = if (isMorning) morningMessages.random() else eveningMessages.random()
        val notifId = if (isMorning) NOTIF_MORNING else NOTIF_EVENING

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(message.first)
            .setContentText(message.second)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message.second)
                    .setBigContentTitle(message.first)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(mainPending)
            .setAutoCancel(true)
            .addAction(
                android.R.drawable.ic_menu_view,
                "Review Now",
                reviewPending
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Later",
                dismissPending
            )
            .build()

        val manager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager
        manager.notify(notifId, notification)
    }fun sendCustomNotification(context: Context, title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pending = PendingIntent.getActivity(
            context, 99, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager
        manager.notify(3001, notification)
    }
}