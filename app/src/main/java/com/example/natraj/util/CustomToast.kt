package com.example.natraj.util

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.natraj.R

object CustomToast {
    
    fun showSuccess(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        showCustomToast(context, "✓ $message", "#4CAF50", "#2E7D32", duration)
    }
    
    fun showError(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        showCustomToast(context, "✕ $message", "#F44336", "#C62828", duration)
    }
    
    fun showWarning(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        showCustomToast(context, "⚠ $message", "#FF9800", "#E65100", duration)
    }
    
    fun showInfo(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        showCustomToast(context, "ℹ $message", "#2196F3", "#1565C0", duration)
    }
    
    private fun showCustomToast(
        context: Context, 
        message: String, 
        bgColor: String, 
        borderColor: String,
        duration: Int
    ) {
        val toast = Toast(context)
        
        // Create container layout
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(48, 32, 48, 32)
            
            // Create gradient background with border
            val drawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(Color.parseColor(bgColor))
                cornerRadius = 24f
                setStroke(4, Color.parseColor(borderColor))
                
                // Add subtle shadow effect
                alpha = 245 // Slightly transparent
            }
            background = drawable
            
            // Add elevation effect
            elevation = 12f
        }
        
        // Create text view
        val textView = TextView(context).apply {
            text = message
            setTextColor(Color.WHITE)
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            
            // Add shadow to text for better readability
            setShadowLayer(4f, 0f, 2f, Color.parseColor("#80000000"))
            
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        
        container.addView(textView)
        
        toast.view = container
        toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 120)
        toast.duration = duration
        toast.show()
    }
}