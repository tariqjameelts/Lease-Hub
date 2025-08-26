package com.mindblowers.leasehub.data.prefs

import android.content.Context

class AppPrefs(context: Context) {

    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    fun saveUserSession(userId: Long) {
        prefs.edit()
            .putLong(KEY_USER_ID, userId)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }

    fun getUserId(): Long? {
        val id = prefs.getLong(KEY_USER_ID, -1)
        return if (id != -1L) id else null
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
