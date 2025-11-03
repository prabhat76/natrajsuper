package com.example.natraj

import android.app.Application
import com.example.natraj.data.woo.WooPrefs

class NatrajApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize WooCommerce credentials
        initializeWooCommerce()
        
        // Ensure global initialization of managers used across the app
        AuthManager.initialize(this)
        CartManager.initialize(this)
        ProductManager.initialize(this)
    }
    
    private fun initializeWooCommerce() {
        val prefs = WooPrefs(this)
        
        // Set WooCommerce credentials
        prefs.baseUrl = "https://www.natrajsuper.com"
        prefs.consumerKey = "ck_60e3de7255dafa3b78eeb2d96fec395cb0ceb19c"
        prefs.consumerSecret = "cs_2f8926db30ebb4366d135c1150ccbdd9cdb2b211"
        
        android.util.Log.d("NatrajApp", "WooCommerce credentials configured")
        android.util.Log.d("NatrajApp", "Base URL: ${prefs.baseUrl}")
        android.util.Log.d("NatrajApp", "Consumer Key: ${prefs.consumerKey?.take(10)}...")
    }
}
