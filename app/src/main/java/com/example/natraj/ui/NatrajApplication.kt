package com.example.natraj

import android.app.Application
import android.content.Intent
import com.example.natraj.data.woo.WooPrefs
import com.example.natraj.util.notification.NotificationHelper
import com.example.natraj.util.kafka.KafkaConfig
import com.example.natraj.util.kafka.KafkaNotificationService
// import com.example.natraj.util.tracking.OrderTrackingScheduler

class NatrajApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize WooCommerce credentials
        initializeWooCommerce()
        
        // Ensure global initialization of managers used across the app
        AuthManager.initialize(this)
        CartManager.initialize(this)
        ProductManager.initialize(this)
        WishlistManager.initialize(this)
        com.example.natraj.util.manager.AddressManager.init(this)
        
        // Initialize notifications
        initializeNotifications()
        
        // Start Kafka notification service if configured
        if (KafkaConfig.isConfigured()) {
            startKafkaService()
        }
        
        // Initialize order tracking scheduler
        // initializeOrderTracking()
    }
    
    private fun initializeWooCommerce() {
        val prefs = WooPrefs(this)
        
        // Set WooCommerce credentials with read/write access
        prefs.baseUrl = "https://www.natrajsuper.com"
        prefs.consumerKey = "ck_4f13aeb31551791a4609bc8a6ba6d0b1df7ac364"
        prefs.consumerSecret = "cs_f959e8710abbc9c8343781916c73cfb5cdf9243a"
        
        android.util.Log.d("NatrajApp", "WooCommerce credentials configured")
        android.util.Log.d("NatrajApp", "Base URL: ${prefs.baseUrl}")
        android.util.Log.d("NatrajApp", "Consumer Key: ${prefs.consumerKey?.take(10)}...")
    }
    
    private fun initializeNotifications() {
        // Create notification channels
        NotificationHelper.createNotificationChannels(this)
        android.util.Log.d("NatrajApp", "Notification channels created")
    }
    
    private fun startKafkaService() {
        if (!KafkaNotificationService.isRunning()) {
            val serviceIntent = Intent(this, KafkaNotificationService::class.java)
            startService(serviceIntent)
            android.util.Log.d("NatrajApp", "Kafka notification service started")
        }
    }
    
    /*
    private fun initializeOrderTracking() {
        val trackingScheduler = OrderTrackingScheduler(this)
        trackingScheduler.startPeriodicTracking()
        android.util.Log.d("NatrajApp", "Order tracking scheduler started")
    }
    */
}
