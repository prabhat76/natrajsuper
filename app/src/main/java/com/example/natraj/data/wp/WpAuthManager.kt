package com.example.natraj.data.wp

import android.content.Context
import android.content.SharedPreferences
import com.example.natraj.data.repository.WpRepository

class WpAuthManager(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("wp_auth", Context.MODE_PRIVATE)
    private val wpRepository = WpRepository(context)
    
    companion object {
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_DISPLAY_NAME = "user_display_name"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }
    
    suspend fun login(username: String, password: String): Result<WpLoginResponse> {
        return try {
            val request = WpLoginRequest(username, password)
            val response = wpRepository.api.login(request)
            
            // Save credentials
            prefs.edit().apply {
                putString(KEY_TOKEN, response.token)
                putString(KEY_USER_EMAIL, response.userEmail)
                putString(KEY_USER_NAME, response.userNicename)
                putString(KEY_USER_DISPLAY_NAME, response.userDisplayName)
                putBoolean(KEY_IS_LOGGED_IN, true)
                apply()
            }
            
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun register(
        username: String,
        email: String,
        password: String,
        firstName: String? = null,
        lastName: String? = null
    ): Result<WpRegisterResponse> {
        return try {
            val request = WpRegisterRequest(username, email, password, firstName, lastName)
            val response = wpRepository.api.register(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun logout() {
        prefs.edit().clear().apply()
    }
    
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && !getToken().isNullOrBlank()
    }
    
    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }
    
    fun getUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }
    
    fun getUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }
    
    fun getUserDisplayName(): String? {
        return prefs.getString(KEY_USER_DISPLAY_NAME, null)
    }
}
