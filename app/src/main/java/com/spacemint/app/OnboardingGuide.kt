package com.spacemint.app

import android.content.Context

object OnboardingGuide {
    private const val PREFS_NAME = "spacemint_guide"
    private const val KEY_SHOWN  = "guide_shown"

    fun markShown(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_SHOWN, true).apply()
    }

    fun isShown(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_SHOWN, false)
    }
}