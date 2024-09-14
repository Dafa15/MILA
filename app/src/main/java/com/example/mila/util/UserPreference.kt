package com.example.mila.util

import android.content.Context
import android.content.SharedPreferences

class UserPreference(context: Context) {

    companion object {
        private const val PREFS_NAME = "user_pref"
    }
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun putBoolean(key: String, value: Boolean) {
        val editor = preferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getBoolean(key: String):Boolean {
        return preferences.getBoolean(key, false)
    }

    fun putString(key: String, value: String) {
        val editor = preferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(key: String):String? {
        return preferences.getString(key, null)
    }

    fun clear() {
        val editor = preferences.edit()
        editor.clear()
        editor.apply()
    }
}