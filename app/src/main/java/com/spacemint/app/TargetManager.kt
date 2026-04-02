package com.spacemint.app

import android.content.Context
import android.util.Log

object TargetManager {

    private const val PREFS_NAME           = "spacemint_target"
    private const val KEY_USER_TARGET      = "target_mb"
    private const val KEY_CURRENT_TARGET   = "current_target_mb"
    private const val KEY_SESSION_HISTORY  = "session_mb_history"
    private const val KEY_LOW_NOTIF_SENT   = "low_notif_sent"

    // ── GET CURRENT TARGET ────────────────────────────────────
    fun getCurrentTarget(context: Context): Float {
        val prefs = prefs(context)
        return prefs.getFloat(KEY_CURRENT_TARGET, 50f)
    }

    // ── GET USER SET TARGET ───────────────────────────────────
    fun getUserTarget(context: Context): Float {
        return prefs(context).getFloat(KEY_USER_TARGET, 50f)
    }

    // ── USER MANUALLY SETS TARGET ─────────────────────────────
    fun setUserTarget(context: Context, mb: Float) {
        prefs(context).edit()
            .putFloat(KEY_USER_TARGET, mb)
            .putFloat(KEY_CURRENT_TARGET, mb)
            .putBoolean(KEY_LOW_NOTIF_SENT, false) // reset notification
            .apply()
        Log.d("TargetManager", "User set target to $mb MB")
    }

    // ── RECORD SESSION ────────────────────────────────────────
    // call after each session with actual MB shown
    fun recordSession(context: Context, actualMB: Float) {
        val prefs = prefs(context)

        // get history — last 7 sessions stored as comma separated
        val historyStr = prefs.getString(KEY_SESSION_HISTORY, "") ?: ""
        val history = if (historyStr.isEmpty()) mutableListOf()
        else historyStr.split(",").mapNotNull { it.toFloatOrNull() }.toMutableList()

        // add this session
        history.add(actualMB)

        // keep only last 7
        if (history.size > 7) history.removeAt(0)

        // save history
        prefs.edit()
            .putString(KEY_SESSION_HISTORY, history.joinToString(","))
            .apply()

        // auto adjust target
        autoAdjust(context, history)
    }

    // ── AUTO ADJUST TARGET ────────────────────────────────────
    private fun autoAdjust(context: Context, history: List<Float>) {
        if (history.size < 3) return // need at least 3 sessions

        val prefs         = prefs(context)
        val currentTarget = prefs.getFloat(KEY_CURRENT_TARGET, 50f)
        val userTarget    = prefs.getFloat(KEY_USER_TARGET, 50f)

        // average of last 3 sessions
        val recentAvg = history.takeLast(3).average().toFloat()

        // if recent average is 30% below current target for 3 sessions
        val threshold = currentTarget * 0.7f

        if (recentAvg < threshold && currentTarget > 10f) {
            // reduce target by 10 MB — minimum 10 MB
            val newTarget = (currentTarget - 10f).coerceAtLeast(10f)
            prefs.edit()
                .putFloat(KEY_CURRENT_TARGET, newTarget)
                .apply()
            Log.d("TargetManager", "Auto reduced target from $currentTarget to $newTarget MB")

            // check if we should send the funny low notification
            if (newTarget <= 15f && recentAvg < 10f) {
                sendLowNotification(context, prefs)
            }
        }

        // if user manually raised target — respect it
        if (recentAvg > currentTarget * 1.2f && currentTarget < userTarget) {
            val newTarget = (currentTarget + 10f).coerceAtMost(userTarget)
            prefs.edit().putFloat(KEY_CURRENT_TARGET, newTarget).apply()
        }
    }

    // ── FUNNY LOW NOTIFICATION ────────────────────────────────
    private fun sendLowNotification(
        context: Context,
        prefs: android.content.SharedPreferences
    ) {
        // only send once ever
        if (prefs.getBoolean(KEY_LOW_NOTIF_SENT, false)) return

        prefs.edit().putBoolean(KEY_LOW_NOTIF_SENT, true).apply()

        val messages = listOf(
            "SpaceMint thinks your phone might actually be clean. Impressive.",
            "We are running out of junk to show you. Your phone is basically a museum now.",
            "SpaceMint is bored. Your phone is too clean.",
            "Congratulations. You have defeated clutter. SpaceMint has nothing left to do.",
            "Your phone is so clean SpaceMint is considering retirement."
        )

        NotificationHelper.sendCustomNotification(
            context  = context,
            title    = "Something unusual happened",
            message  = messages.random()
        )
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}