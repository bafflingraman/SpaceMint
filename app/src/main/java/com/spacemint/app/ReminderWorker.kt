package com.spacemint.app

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

// Worker = a task that runs in the background
// Even if the app is closed, WorkManager runs this
class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    // This function runs when WorkManager fires the task
    override fun doWork(): Result {
        // Send the notification
        NotificationHelper.sendReminder(applicationContext)

        // Tell WorkManager the task succeeded
        return Result.success()
    }
}