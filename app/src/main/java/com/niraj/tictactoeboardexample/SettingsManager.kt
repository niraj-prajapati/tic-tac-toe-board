package com.niraj.tictactoeboardexample

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("game_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
        private const val KEY_HAPTIC_ENABLED = "haptic_enabled"
        private const val KEY_DIFFICULTY = "difficulty"
    }

    var soundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND_ENABLED, value).apply()

    var vibrationEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIBRATION_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_VIBRATION_ENABLED, value).apply()

    var hapticEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTIC_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTIC_ENABLED, value).apply()

    var difficulty: String
        get() = prefs.getString(KEY_DIFFICULTY, "Easy") ?: "Easy"
        set(value) = prefs.edit().putString(KEY_DIFFICULTY, value).apply()
} 