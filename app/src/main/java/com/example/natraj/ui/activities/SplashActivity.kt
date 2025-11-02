package com.example.natraj

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class SplashActivity : AppCompatActivity() {
    
    private val splashDelay = 3000L // 3 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Hide the action bar if present
        supportActionBar?.hide()
        
        // Initialize AuthManager
        AuthManager.initialize(this)
        
        // Start animations
        startAnimations()

        // Navigate to appropriate screen after delay
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = if (AuthManager.isLoggedIn()) {
                Intent(this, MainActivity::class.java)
            } else {
                Intent(this, LoginActivity::class.java)
            }
            startActivity(intent)
            finish()
        }, splashDelay)
    }
    
    private fun startAnimations() {
        try {
            // Logo card animation - scale and fade in
            val logoCard = findViewById<CardView>(R.id.logo_card)
            val scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_fade_in)
            logoCard.startAnimation(scaleAnimation)
            
            // Brand name animation - fade in with delay
            val brandName = findViewById<TextView>(R.id.brand_name)
            val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
            fadeInAnimation.startOffset = 300
            brandName.startAnimation(fadeInAnimation)
            
            // Subtitle animation - fade in with delay
            val subtitle = findViewById<TextView>(R.id.subtitle_text)
            val subtitleAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
            subtitleAnimation.startOffset = 500
            subtitle.startAnimation(subtitleAnimation)
            
            // Loading container animation - slide up and fade in
            val loadingContainer = findViewById<android.view.View>(R.id.loading_container)
            val slideUpAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in)
            slideUpAnimation.startOffset = 700
            loadingContainer.startAnimation(slideUpAnimation)
            
        } catch (e: Exception) {
            android.util.Log.e("SplashActivity", "Error starting animations", e)
        }
    }
}