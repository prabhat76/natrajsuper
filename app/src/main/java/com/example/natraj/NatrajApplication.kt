package com.example.natraj

import android.app.Application

class NatrajApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CartManager.initialize(this)
        ProductManager.initialize(this)
    }
}