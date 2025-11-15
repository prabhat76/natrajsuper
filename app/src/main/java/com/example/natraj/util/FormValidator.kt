package com.example.natraj.util

import android.text.TextUtils
import android.util.Patterns
import com.google.android.material.textfield.TextInputLayout

object FormValidator {
    
    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String = ""
    )
    
    fun validateEmail(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult(false, "Email is required")
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> 
                ValidationResult(false, "Please enter a valid email address")
            else -> ValidationResult(true)
        }
    }
    
    fun validatePhone(phone: String): ValidationResult {
        return when {
            phone.isBlank() -> ValidationResult(false, "Phone number is required")
            phone.length < 10 -> ValidationResult(false, "Phone number must be at least 10 digits")
            !phone.all { it.isDigit() || it == '+' || it == '-' || it == ' ' } -> 
                ValidationResult(false, "Please enter a valid phone number")
            else -> ValidationResult(true)
        }
    }
    
    fun validateName(name: String, fieldName: String = "Name"): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult(false, "$fieldName is required")
            name.length < 2 -> ValidationResult(false, "$fieldName must be at least 2 characters")
            !name.matches(Regex("^[a-zA-Z\\s.]+$")) -> 
                ValidationResult(false, "$fieldName can only contain letters and spaces")
            else -> ValidationResult(true)
        }
    }
    
    fun validateAddress(address: String): ValidationResult {
        return when {
            address.isBlank() -> ValidationResult(false, "Address is required")
            address.length < 10 -> ValidationResult(false, "Please enter a complete address")
            else -> ValidationResult(true)
        }
    }
    
    fun validatePincode(pincode: String): ValidationResult {
        return when {
            pincode.isBlank() -> ValidationResult(false, "Pincode is required")
            pincode.length != 6 -> ValidationResult(false, "Pincode must be 6 digits")
            !pincode.all { it.isDigit() } -> ValidationResult(false, "Pincode must contain only digits")
            else -> ValidationResult(true)
        }
    }
    
    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult(false, "Password is required")
            password.length < 6 -> ValidationResult(false, "Password must be at least 6 characters")
            else -> ValidationResult(true)
        }
    }
    
    fun validateConfirmPassword(password: String, confirmPassword: String): ValidationResult {
        return when {
            confirmPassword.isBlank() -> ValidationResult(false, "Please confirm your password")
            password != confirmPassword -> ValidationResult(false, "Passwords do not match")
            else -> ValidationResult(true)
        }
    }
    
    fun validateRequired(value: String, fieldName: String): ValidationResult {
        return when {
            value.isBlank() -> ValidationResult(false, "$fieldName is required")
            else -> ValidationResult(true)
        }
    }
    
    fun validateQuantity(quantity: Int): ValidationResult {
        return when {
            quantity < 1 -> ValidationResult(false, "Quantity must be at least 1")
            else -> ValidationResult(true)
        }
    }
    
    // Helper function to set error on TextInputLayout
    fun setError(textInputLayout: TextInputLayout, validationResult: ValidationResult) {
        if (validationResult.isValid) {
            textInputLayout.error = null
            textInputLayout.isErrorEnabled = false
        } else {
            textInputLayout.error = validationResult.errorMessage
            textInputLayout.isErrorEnabled = true
        }
    }
    
    // Helper to validate multiple fields
    fun validateAllFields(vararg validations: ValidationResult): Boolean {
        return validations.all { it.isValid }
    }
}
