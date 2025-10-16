package com.example.natraj

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private var cartBadgeCallback: (() -> Unit)? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> switchFragment(HomeFragment())
                R.id.nav_categories -> switchFragment(CategoriesFragment())
                R.id.nav_cart -> switchFragment(CartFragment())
                R.id.nav_profile -> switchFragment(ProfileFragment())
            }
            true
        }
        // Show HomeFragment by default, or open cart if requested
        if (savedInstanceState == null) {
            val openCart = intent.getBooleanExtra("open_cart", false)
            if (openCart) switchFragment(CartFragment()) else switchFragment(HomeFragment())
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

    fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}