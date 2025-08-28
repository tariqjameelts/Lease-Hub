package com.mindblowers.leasehub.data.prefs

import android.content.Context
import androidx.core.content.edit

class AppPrefs(context: Context) {

    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_IS_NEW_USER = "is_new_user"

        private const val KEY_THEME = "theme_option"
        private const val KEY_DYNAMIC = "dynamic_color"
    }

    // --- User session ---
    fun saveUserSession(userId: Long, isNewUser: Boolean = false) {
        prefs.edit {
            putLong(KEY_USER_ID, userId)
            putBoolean(KEY_IS_LOGGED_IN, true)
            putBoolean(KEY_IS_NEW_USER, isNewUser)
        }
    }

    fun getUserId(): Long? {
        val id = prefs.getLong(KEY_USER_ID, -1)
        return if (id != -1L) id else null
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun clearSession() {
        prefs.edit { clear() }
        setNotNewUser()
    }

    // --- New User ---
    fun isNewUser(): Boolean = prefs.getBoolean(KEY_IS_NEW_USER, true)

    fun setNotNewUser() {
        prefs.edit { putBoolean(KEY_IS_NEW_USER, false) }
    }

    // --- Theme ---
    fun setTheme(option: ThemeOption) {
        prefs.edit { putString(KEY_THEME, option.name) }
    }

    fun getTheme(): ThemeOption {
        val saved = prefs.getString(KEY_THEME, ThemeOption.SYSTEM.name)
        return runCatching { ThemeOption.valueOf(saved!!) }
            .getOrDefault(ThemeOption.SYSTEM)
    }

    // --- Dynamic Color ---
    fun setDynamicColor(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_DYNAMIC, enabled) }
    }

    fun getDynamicColor(): Boolean {
        return prefs.getBoolean(KEY_DYNAMIC, true)
    }
}


enum class ThemeOption {
    LIGHT,
    DARK,
    SYSTEM
}