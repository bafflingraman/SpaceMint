package com.spacemint.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra("type") ?: "morning"
        Log.d("NotificationReceiver", "Alarm fired: $type")

        when (type) {
            "morning" -> {
                // always send morning notification
                NotificationHelper.sendReminder(context, isMorning = true)
            }
            "evening" -> {
                // only send evening if session NOT done today
                if (!NotificationScheduler.isSessionDoneToday(context)) {
                    NotificationHelper.sendReminder(context, isMorning = false)
                } else {
                    Log.d("NotificationReceiver", "Session done — skipping evening")
                }
            }
        }
    }
}