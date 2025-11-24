package com.example.natraj.util

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import com.example.natraj.data.woo.WooPrefs
import com.google.android.material.appbar.MaterialToolbar

object ThemeUtil {

    // Initialize theme based on device settings
    fun initializeTheme(context: Context) {
        val prefs = WooPrefs(context)
        val savedTheme = prefs.themeMode

        val mode = when (savedTheme) {
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            "system" -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM // Default to system
        }

        AppCompatDelegate.setDefaultNightMode(mode)
    }

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

    fun setThemeMode(context: Context, mode: String) {
        val prefs = WooPrefs(context)
        prefs.themeMode = mode

        val delegateMode = when (mode) {
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            "system" -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }

        AppCompatDelegate.setDefaultNightMode(delegateMode)
    }

    fun getCurrentThemeMode(context: Context): String {
        val prefs = WooPrefs(context)
        return prefs.themeMode ?: "system"
    }

    fun isDarkTheme(context: Context): Boolean {
        return context.resources.configuration.uiMode and
               android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
               android.content.res.Configuration.UI_MODE_NIGHT_YES
    }
}
