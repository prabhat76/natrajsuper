package com.example.natraj

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.natraj.data.WooRepository
import com.example.natraj.util.CustomToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {
    
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginBtn: Button
    private lateinit var signupLink: TextView
    private lateinit var skipLink: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        // Hide action bar
        supportActionBar?.hide()
        
        initializeViews()
        setupClickListeners()
    }
    
    private fun initializeViews() {
        emailInput = findViewById(R.id.login_email_input)
        passwordInput = findViewById(R.id.login_password_input)
        loginBtn = findViewById(R.id.login_btn)
        signupLink = findViewById(R.id.signup_link)
        skipLink = findViewById(R.id.skip_login)
    }
    
    private fun setupClickListeners() {
        loginBtn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            
            if (validateInput(email, password)) {
                performLogin(email)
            }
        }
        
        signupLink.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
        
        skipLink.setOnClickListener {
            // Allow guest mode
            navigateToMain()
        }
    }
    
    private fun validateInput(email: String, password: String): Boolean {
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
        
        return true
    }
    
    private fun performLogin(email: String) {
        // Disable button and show progress
        loginBtn.isEnabled = false
        loginBtn.text = "Logging in..."
        
        lifecycleScope.launch {
            try {
                // Check if customer exists in WooCommerce
                val repo = WooRepository(this@LoginActivity)
                
                val customer = withContext(Dispatchers.IO) {
                    repo.getCustomerByEmail(email)
                }
                
                if (customer != null) {
                    // Customer found - login
                    val fullName = "${customer.first_name} ${customer.last_name}".trim()
                    val displayName = fullName.ifBlank { email.substringBefore("@") }
                    
                    AuthManager.login(displayName, email, "", customer.id)
                    
                    CustomToast.showSuccess(this@LoginActivity, "Welcome back, $displayName!")
                    navigateToMain()
                } else {
                    // Customer not found
                    loginBtn.isEnabled = true
                    loginBtn.text = "LOGIN"
                    CustomToast.showError(
                        this@LoginActivity, 
                        "Account not found. Please sign up first.",
                        Toast.LENGTH_LONG
                    )
                }
                
            } catch (e: Exception) {
                Log.e("LoginActivity", "Login failed", e)
                loginBtn.isEnabled = true
                loginBtn.text = "LOGIN"
                
                // For development: allow local login if WooCommerce is not configured
                val name = email.substringBefore("@")
                AuthManager.login(name, email, "", 0)
                CustomToast.showWarning(this@LoginActivity, "Logged in locally (WooCommerce unavailable)")
                navigateToMain()
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
