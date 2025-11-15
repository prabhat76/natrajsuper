package com.example.natraj

import android.content.Context
import android.content.SharedPreferences

object AuthManager {
    private const val PREFS_NAME = "natraj_auth"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_PHONE = "user_phone"
    private const val KEY_CUSTOMER_ID = "customer_id"
    
    private lateinit var prefs: SharedPreferences
    
    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    fun login(name: String, email: String, phone: String, customerId: Int = 0) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_PHONE, phone)
            if (customerId > 0) {
                putInt(KEY_CUSTOMER_ID, customerId)
            }
            apply()
        }
    }
    
    fun logout() {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, false)
            remove(KEY_USER_NAME)
            remove(KEY_USER_EMAIL)
            remove(KEY_USER_PHONE)
            remove(KEY_CUSTOMER_ID)
            apply()
        }
    }
    
    fun getUserName(): String {
        return prefs.getString(KEY_USER_NAME, "Guest") ?: "Guest"
    }
    
    fun getUserEmail(): String {
        return prefs.getString(KEY_USER_EMAIL, "") ?: ""
    }
    
    fun getUserPhone(): String {
        return prefs.getString(KEY_USER_PHONE, "") ?: ""
    }
    
    fun getCustomerId(): Int {
        return prefs.getInt(KEY_CUSTOMER_ID, 0)
    }
}
