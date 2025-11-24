package com.example.natraj

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private val NOTIFICATION_PERMISSION_CODE = 100
    private var cartBadgeCallback: (() -> Unit)? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Handle window insets for the fragment container
        val fragmentContainer = findViewById<FrameLayout>(R.id.fragment_container)
        ViewCompat.setOnApplyWindowInsetsListener(fragmentContainer) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Only apply top inset to avoid overlapping with bottom navigation
            view.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        // Handle bottom navigation insets
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Apply bottom inset to prevent overlap with system navigation
            view.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        // Request notification permission for Android 13+
        requestNotificationPermission()

        // Initialize AuthManager
        android.util.Log.d("MainActivity", "Initializing AuthManager...")
        AuthManager.initialize(this)
        android.util.Log.d("MainActivity", "AuthManager initialized. Logged in: ${AuthManager.isLoggedIn()}")
        
        // Initialize ProductManager
        android.util.Log.d("MainActivity", "Initializing ProductManager...")
        ProductManager.initialize(this)
        android.util.Log.d("MainActivity", "ProductManager initialized. Products: ${ProductManager.getAllProducts().size}")
        
        // Initialize OfferManager
        android.util.Log.d("MainActivity", "Initializing OfferManager...")
        OfferManager.initialize(this)
        android.util.Log.d("MainActivity", "OfferManager initialized. Offers: ${OfferManager.getAllOffers().size}")
        
        // Initialize BlogManager
        android.util.Log.d("MainActivity", "Initializing BlogManager...")
        BlogManager.initialize(this)
        android.util.Log.d("MainActivity", "BlogManager initialized. Posts: ${BlogManager.getAllBlogPosts().size}")

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> switchFragment(HomeFragment(), "Home")
                R.id.nav_categories -> switchFragment(CategoriesFragment(), "Categories")
                R.id.nav_cart -> switchFragment(CartFragment(), "Cart")
                R.id.nav_profile -> switchFragment(ProfileFragment(), "Profile")
            }
            true
        }
        // Show HomeFragment by default, or open cart if requested
        if (savedInstanceState == null) {
            val openCart = intent.getBooleanExtra("open_cart", false)
            if (openCart) switchFragment(CartFragment(), "Cart") else switchFragment(HomeFragment(), "Home")
        }

        // Setup cart badge listener
        val badge = bottomNav.getOrCreateBadge(R.id.nav_cart)
        cartBadgeCallback = {
            try {
                val count = CartManager.getItemCount()
                if (count > 0) {
                    badge.isVisible = true
                    badge.number = count
                } else {
                    badge.isVisible = false
                }
            } catch (e: Exception) {
                badge.isVisible = false
            }
        }
        CartManager.registerListener(cartBadgeCallback!!)
        cartBadgeCallback?.invoke()
    }

    override fun onDestroy() {
        cartBadgeCallback?.let { CartManager.unregisterListener(it) }
        super.onDestroy()
    }

    fun switchFragment(fragment: Fragment, tag: String = "") {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            .replace(R.id.fragment_container, fragment, tag)
            .commit()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_CODE
                )
            }
        }
    }
}