package com.spacemint.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Phone restarted — rescheduling notifications")
            NotificationHelper.createChannel(context)
            NotificationScheduler.scheduleDailyAlarms(context)
        }
    }
}