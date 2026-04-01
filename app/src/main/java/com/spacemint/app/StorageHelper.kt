package com.spacemint.app

import android.content.Context
import android.os.Environment
import android.os.StatFs
import android.content.SharedPreferences

object StorageHelper {

    private const val PREFS_NAME      = "spacemint_stats"
    private const val KEY_DELETED     = "total_deleted"
    private const val KEY_FREED_BYTES = "total_freed_bytes"
    private const val KEY_STREAK      = "streak_count"
    private const val KEY_LAST_DATE   = "last_session_date"

    // ── REAL STORAGE INFO ─────────────────────────────────────
    fun getTotalStorage(): Long {
        val stat = StatFs(Environment.getExternalStorageDirectory().path)
        return stat.totalBytes
    }

    fun getUsedStorage(): Long {
        return getTotalStorage() - getFreeStorage()
    }

    fun getFreeStorage(): Long {
        val stat = StatFs(Environment.getExternalStorageDirectory().path)
        return stat.availableBytes
    }

    fun getUsedPercent(): Float {
        val total = getTotalStorage()
        if (total == 0L) return 0f
        return getUsedStorage().toFloat() / total.toFloat()
    }

    fun formatBytes(bytes: Long): String = when {
        bytes >= 1_000_000_000 -> "%.1f GB".format(bytes / 1_000_000_000.0)
        bytes >= 1_000_000     -> "%.1f MB".format(bytes / 1_000_000.0)
        bytes >= 1_000         -> "%.0f KB".format(bytes / 1_000.0)
        else                   -> "$bytes B"
    }

    // ── STATS ─────────────────────────────────────────────────
    fun getTotalDeleted(context: Context): Int =
        prefs(context).getInt(KEY_DELETED, 0)

    fun getTotalFreedBytes(context: Context): Long =
        prefs(context).getLong(KEY_FREED_BYTES, 0L)

    fun getStreak(context: Context): Int =
        prefs(context).getInt(KEY_STREAK, 0)

    // call this after each session completes
    fun recordSession(context: Context, deletedCount: Int, freedBytes: Long) {
        val p       = prefs(context)
        val today   = todayString()
        val lastDate= p.getString(KEY_LAST_DATE, "") ?: ""
        val yesterday = yesterdayString()

        // update streak
        val currentStreak = p.getInt(KEY_STREAK, 0)
        val newStreak = when {
            lastDate == today     -> currentStreak // same day — no change
            lastDate == yesterday -> currentStreak + 1 // consecutive — increase
            else                  -> 1 // broke streak — reset to 1
        }

        p.edit()
            .putInt(KEY_DELETED,      p.getInt(KEY_DELETED, 0) + deletedCount)
            .putLong(KEY_FREED_BYTES, p.getLong(KEY_FREED_BYTES, 0L) + freedBytes)
            .putInt(KEY_STREAK,       newStreak)
            .putString(KEY_LAST_DATE, today)
            .apply()
    }

    // ── HELPERS ───────────────────────────────────────────────
    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun todayString(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }

    private fun yesterdayString(): String {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return sdf.format(cal.time)
    }
}