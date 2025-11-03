package com.example.natraj.util

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.natraj.R

object CustomToast {
    
    fun showSuccess(context: Context, message: String) {
        showCustomToast(context, message, R.color.success_green)
    }
    
    fun showError(context: Context, message: String) {
        showCustomToast(context, message, R.color.error_red)
    }
    
    private fun showCustomToast(context: Context, message: String, colorRes: Int) {
        val toast = Toast(context)
        
        val textView = TextView(context).apply {
            text = message
            setTextColor(ContextCompat.getColor(context, android.R.color.white))
            textSize = 14f
            setPadding(32, 24, 32, 24)
            
            val drawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(ContextCompat.getColor(context, colorRes))
                cornerRadius = 16f
            }
            background = drawable
        }
        
        toast.view = textView
        toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 200)
        toast.duration = Toast.LENGTH_SHORT
        toast.show()
    }
}