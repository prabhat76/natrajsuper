package com.example.natraj.util

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.widget.Toolbar
import com.example.natraj.data.woo.WooPrefs
import com.google.android.material.appbar.MaterialToolbar

object ThemeUtil {
    
    fun getPrimaryColor(context: Context): Int {
        val prefs = WooPrefs(context)
        return try {
            Color.parseColor(prefs.primaryColor)
        } catch (e: Exception) {
            Color.parseColor("#2874A6") // fallback
        }
    }
    
    fun getAccentColor(context: Context): Int {
        val prefs = WooPrefs(context)
        return try {
            Color.parseColor(prefs.accentColor)
        } catch (e: Exception) {
            Color.parseColor("#2196F3") // fallback
        }
    }
    
    fun applyToolbarColor(toolbar: Toolbar, context: Context) {
        toolbar.setBackgroundColor(getPrimaryColor(context))
    }
    
    fun applyToolbarColor(toolbar: MaterialToolbar, context: Context) {
        toolbar.setBackgroundColor(getPrimaryColor(context))
    }
    
    fun updateColors(context: Context, primaryColor: String?, accentColor: String?) {
        val prefs = WooPrefs(context)
        primaryColor?.let { prefs.primaryColor = it }
        accentColor?.let { prefs.accentColor = it }
    }
}
