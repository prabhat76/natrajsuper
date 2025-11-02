package com.example.natraj

import android.app.Application

class NatrajApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Ensure global initialization of managers used across the app
        AuthManager.initialize(this)
        CartManager.initialize(this)
        ProductManager.initialize(this)
    }
}