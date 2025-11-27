package com.example.natraj.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.natraj.AuthManager
import com.example.natraj.R
import com.example.natraj.data.WooRepository
import com.example.natraj.util.CustomToast

import com.example.natraj.util.sync.AccountSyncManager
import com.example.natraj.util.error.ErrorType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignupActivity : AppCompatActivity() {
    
    private lateinit var nameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var signupBtn: Button
    private lateinit var loginLink: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        
        // Hide action bar
        supportActionBar?.hide()
        
        initializeViews()
        setupClickListeners()
    }
    
    private fun initializeViews() {
        nameInput = findViewById(R.id.signup_name_input)
        emailInput = findViewById(R.id.signup_email_input)
        phoneInput = findViewById(R.id.signup_phone_input)
        passwordInput = findViewById(R.id.signup_password_input)
        confirmPasswordInput = findViewById(R.id.signup_confirm_password_input)
        signupBtn = findViewById(R.id.signup_btn)
        loginLink = findViewById(R.id.login_link)
    }
    
    private fun setupClickListeners() {
        signupBtn.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val phone = phoneInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()
            
            if (validateInput(name, email, phone, password, confirmPassword)) {
                performSignup(name, email, phone, password)
            }
        }
        
        loginLink.setOnClickListener {
            finish() // Go back to login
        }
    }
    
    private fun validateInput(
        name: String, 
        email: String, 
        phone: String, 
        password: String, 
        confirmPassword: String
    ): Boolean {
        if (name.isEmpty()) {
            nameInput.error = "Name is required"
            nameInput.requestFocus()
            return false
        }
        
        if (email.isEmpty()) {
            emailInput.error = "Email is required"
            emailInput.requestFocus()
            return false
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.error = "Enter a valid email"
            emailInput.requestFocus()
            return false
        }
        
        if (phone.isEmpty()) {
            phoneInput.error = "Phone number is required"
            phoneInput.requestFocus()
            return false
        }
        
        if (phone.length < 10) {
            phoneInput.error = "Enter a valid phone number"
            phoneInput.requestFocus()
            return false
        }
        
        if (password.isEmpty()) {
            passwordInput.error = "Password is required"
            passwordInput.requestFocus()
            return false
        }
        
        if (password.length < 6) {
            passwordInput.error = "Password must be at least 6 characters"
            passwordInput.requestFocus()
            return false
        }
        
        if (password != confirmPassword) {
            confirmPasswordInput.error = "Passwords do not match"
            confirmPasswordInput.requestFocus()
            return false
        }
        
        return true
    }
    
    private fun performSignup(name: String, email: String, phone: String, password: String) {
        // Disable button and show progress
        signupBtn.isEnabled = false
        signupBtn.text = "Creating account..."
        
        lifecycleScope.launch {
            try {
                // Create WooCommerce customer
                val repo = WooRepository(this@SignupActivity)
                
                val nameParts = name.split(" ", limit = 2)
                val firstName = nameParts.getOrNull(0) ?: name
                val lastName = nameParts.getOrNull(1) ?: ""
                
                // Create username from email
                val username = email.substringBefore("@").replace("[^a-zA-Z0-9]".toRegex(), "")
                
                val customer = withContext(Dispatchers.IO) {
                    repo.createCustomer(
                        email = email,
                        firstName = firstName,
                        lastName = lastName,
                        username = username,
                        password = password
                    )
                }
                
                // Save auth data with customer ID
                AuthManager.login(name, email, phone, customer.id)
                
                // Sync account data from WooCommerce (should be empty for new accounts)
                try {
                    AccountSyncManager.fullSyncFromWoo(this@SignupActivity)
                    Log.d("SignupActivity", "Account synced successfully")
                } catch (syncException: Exception) {
                    Log.w("SignupActivity", "Account sync failed, but signup continues", syncException)
                }
                
                CustomToast.showSuccess(this@SignupActivity, "Account created successfully! Welcome, $name")
                navigateToMain()
                
            } catch (e: Exception) {
                Log.e("SignupActivity", "Signup failed", e)
                signupBtn.isEnabled = true
                signupBtn.text = "SIGN UP"
                
                // Handle specific errors
                val errorMessage = when {
                    e.message?.contains("email", ignoreCase = true) == true -> 
                        "Email already registered. Please login instead."
                    e.message?.contains("username", ignoreCase = true) == true -> 
                        "Username already taken. Please try a different email."
                    else -> "Unable to create account. Please try again."
                }
                
                CustomToast.showError(this@SignupActivity, errorMessage, Toast.LENGTH_LONG)
            }
        }
    }
    
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
