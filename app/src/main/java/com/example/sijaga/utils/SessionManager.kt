package com.example.sijaga.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("sijaga_session", Context.MODE_PRIVATE)

    fun saveSession(id: Int, nama: String, email: String, role: String) {
        prefs.edit().apply {
            putInt("user_id", id); putString("nama", nama)
            putString("email", email); putString("role", role)
            putBoolean("is_logged_in", true)
        }.apply()
    }

    fun getUserId()   = prefs.getInt("user_id", -1)
    fun getNama()     = prefs.getString("nama", "") ?: ""
    fun getEmail()    = prefs.getString("email", "") ?: ""
    fun getRole()     = prefs.getString("role", "") ?: ""
    fun isLoggedIn()  = prefs.getBoolean("is_logged_in", false)
    fun logout()      = prefs.edit().clear().apply()
}
