package com.example.natraj

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

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
                performSignup(name, email, phone)
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
    
    private fun performSignup(name: String, email: String, phone: String) {
        // In a real app, this would call an API
        // For now, we'll just store the user data
        
        AuthManager.login(name, email, phone)
        
        Toast.makeText(this, "Account created successfully! Welcome, $name", Toast.LENGTH_SHORT).show()
        navigateToMain()
    }
    
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
