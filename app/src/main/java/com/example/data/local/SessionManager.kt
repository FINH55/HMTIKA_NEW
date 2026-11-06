package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.UserRole

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("hmtika_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LOGGED_IN_NIM = "logged_in_nim"
        private const val KEY_LOGGED_IN_ROLE = "logged_in_role"
    }

    fun saveUserSession(nim: String, role: UserRole) {
        prefs.edit()
            .putString(KEY_LOGGED_IN_NIM, nim)
            .putString(KEY_LOGGED_IN_ROLE, role.name)
            .apply()
    }

    fun getLoggedInNim(): String? {
        return prefs.getString(KEY_LOGGED_IN_NIM, null)
    }

    fun getLoggedInRole(): UserRole {
        val roleStr = prefs.getString(KEY_LOGGED_IN_ROLE, UserRole.MAHASISWA.name)
        return try {
            UserRole.valueOf(roleStr ?: UserRole.MAHASISWA.name)
        } catch (_: Exception) {
            UserRole.MAHASISWA
        }
    }

    fun isLoggedIn(): Boolean {
        return getLoggedInNim() != null
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
