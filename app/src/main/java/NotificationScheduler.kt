package com.spacemint.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import java.util.Calendar

object NotificationScheduler {

    private const val PREFS_NAME       = "spacemint_notifications"
    private const val KEY_SESSION_DONE = "session_done_date"
    private const val KEY_MORNING_HOUR = "morning_hour"
    private const val KEY_MORNING_MIN  = "morning_min"
    private const val KEY_EVENING_HOUR = "evening_hour"
    private const val KEY_EVENING_MIN  = "evening_min"

    private const val ALARM_MORNING = 1001
    private const val ALARM_EVENING = 1002

    // ── DEFAULT TIMES ─────────────────────────────────────────
    private fun defaultMorningHour() = 10
    private fun defaultMorningMin()  = 0
    private fun defaultEveningHour() = 17
    private fun defaultEveningMin()  = 0

    // ── SCHEDULE BOTH ALARMS ──────────────────────────────────
    fun scheduleDailyAlarms(context: Context) {
        val prefs = prefs(context)

        val morningHour = prefs.getInt(KEY_MORNING_HOUR, defaultMorningHour())
        val morningMin  = prefs.getInt(KEY_MORNING_MIN,  defaultMorningMin())
        val eveningHour = prefs.getInt(KEY_EVENING_HOUR, defaultEveningHour())
        val eveningMin  = prefs.getInt(KEY_EVENING_MIN,  defaultEveningMin())

        // save defaults if first time
        prefs.edit()
            .putInt(KEY_MORNING_HOUR, morningHour)
            .putInt(KEY_MORNING_MIN,  morningMin)
            .putInt(KEY_EVENING_HOUR, eveningHour)
            .putInt(KEY_EVENING_MIN,  eveningMin)
            .apply()

        scheduleMorning(context, morningHour, morningMin)
        scheduleEvening(context, eveningHour, eveningMin)

        Log.d("Scheduler", "Alarms set — morning $morningHour:$morningMin — evening $eveningHour:$eveningMin")
    }

    // ── SCHEDULE MORNING ──────────────────────────────────────
    internal fun scheduleMorning(context: Context, hour: Int, min: Int) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, min)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1)
        }

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("type", "morning")
        }
        val pending = PendingIntent.getBroadcast(
            context, ALARM_MORNING, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pending
        )
        Log.d("Scheduler", "Morning alarm set for $hour:$min")
    }

    // ── SCHEDULE EVENING ──────────────────────────────────────
    internal fun scheduleEvening(context: Context, hour: Int, min: Int) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, min)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1)
        }

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("type", "evening")
        }
        val pending = PendingIntent.getBroadcast(
            context, ALARM_EVENING, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pending
        )
        Log.d("Scheduler", "Evening alarm set for $hour:$min")
    }

    // ── MARK SESSION COMPLETE ─────────────────────────────────
    fun markSessionComplete(context: Context) {
        val today = todayString()
        prefs(context).edit()
            .putString(KEY_SESSION_DONE, today)
            .apply()
        Log.d("Scheduler", "Session marked complete for $today")
    }

    // ── CHECK IF SESSION DONE TODAY ───────────────────────────
    fun isSessionDoneToday(context: Context): Boolean {
        val done = prefs(context).getString(KEY_SESSION_DONE, "") ?: ""
        return done == todayString()
    }

    // ── CANCEL EVENING IF DONE ────────────────────────────────
    fun cancelEveningIfDone(context: Context) {
        if (isSessionDoneToday(context)) {
            val intent = Intent(context, NotificationReceiver::class.java)
            val pending = PendingIntent.getBroadcast(
                context, ALARM_EVENING, intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            pending?.let { alarm.cancel(it) }
            Log.d("Scheduler", "Evening cancelled — session done")

            // reschedule evening for tomorrow
            val prefs = prefs(context)
            val hour = prefs.getInt(KEY_EVENING_HOUR, defaultEveningHour())
            val min  = prefs.getInt(KEY_EVENING_MIN,  defaultEveningMin())
            scheduleEvening(context, hour, min)
        }
    }

    // ── GET TIMES ─────────────────────────────────────────────
    fun getMorningTime(context: Context): Pair<Int, Int> {
        val p = prefs(context)
        return Pair(
            p.getInt(KEY_MORNING_HOUR, defaultMorningHour()),
            p.getInt(KEY_MORNING_MIN,  defaultMorningMin())
        )
    }

    fun getEveningTime(context: Context): Pair<Int, Int> {
        val p = prefs(context)
        return Pair(
            p.getInt(KEY_EVENING_HOUR, defaultEveningHour()),
            p.getInt(KEY_EVENING_MIN,  defaultEveningMin())
        )
    }

    // ── SET TIMES ─────────────────────────────────────────────
    fun setMorningTime(context: Context, hour: Int, min: Int) {
        prefs(context).edit()
            .putInt(KEY_MORNING_HOUR, hour)
            .putInt(KEY_MORNING_MIN,  min)
            .apply()
        scheduleMorning(context, hour, min)
    }

    fun setEveningTime(context: Context, hour: Int, min: Int) {
        prefs(context).edit()
            .putInt(KEY_EVENING_HOUR, hour)
            .putInt(KEY_EVENING_MIN,  min)
            .apply()
        scheduleEvening(context, hour, min)
    }

    // ── HELPERS ───────────────────────────────────────────────
    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun todayString(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }
}