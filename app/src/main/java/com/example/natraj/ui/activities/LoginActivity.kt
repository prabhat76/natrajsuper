package com.example.natraj

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

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
        // In a real app, this would call an API
        // For now, we'll just store the login state
        
        // Extract name from email (before @)
        val name = email.substringBefore("@").capitalize()
        
        AuthManager.login(name, email, "")
        
        Toast.makeText(this, "Welcome back, $name!", Toast.LENGTH_SHORT).show()
        navigateToMain()
    }
    
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
