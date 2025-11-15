package com.example.natraj

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * Manages wishlist with persistent storage and WooCommerce sync
 */
object WishlistManager {
    private const val TAG = "WishlistManager"
    private const val PREFS_NAME = "natraj_wishlist"
    private const val KEY_WISHLIST = "wishlist_ids"
    
    private val wishlist = mutableSetOf<Int>() // Store product IDs
    private val listeners = mutableListOf<() -> Unit>()
    private lateinit var prefs: SharedPreferences
    private var initialized = false

    /**
     * Initialize WishlistManager with context
     */
    fun initialize(context: Context) {
        if (initialized) return
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadFromPrefs()
        initialized = true
        Log.d(TAG, "WishlistManager initialized with ${wishlist.size} items")
    }
    
    /**
     * Load wishlist from SharedPreferences
     */
    private fun loadFromPrefs() {
        try {
            val wishlistString = prefs.getString(KEY_WISHLIST, "") ?: ""
            if (wishlistString.isNotEmpty()) {
                wishlist.clear()
                wishlistString.split(",").forEach { idStr ->
                    idStr.toIntOrNull()?.let { wishlist.add(it) }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load wishlist from prefs", e)
        }
    }
    
    /**
     * Save wishlist to SharedPreferences
     */
    private fun saveToPrefs() {
        try {
            val wishlistString = wishlist.joinToString(",")
            prefs.edit().putString(KEY_WISHLIST, wishlistString).apply()
            Log.d(TAG, "Wishlist saved: ${wishlist.size} items")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save wishlist to prefs", e)
        }
    }

    /**
     * Toggle product in wishlist
     * Returns true if added, false if removed
     */
    fun toggle(productId: Int): Boolean {
        val added = if (wishlist.contains(productId)) {
            wishlist.remove(productId)
            false
        } else {
            wishlist.add(productId)
            true
        }
        saveToPrefs()
        notifyListeners()
        return added
    }
    
    /**
     * Add product to wishlist
     */
    fun add(productId: Int) {
        if (wishlist.add(productId)) {
            saveToPrefs()
            notifyListeners()
        }
    }
    
    /**
     * Remove product from wishlist
     */
    fun remove(productId: Int) {
        if (wishlist.remove(productId)) {
            saveToPrefs()
            notifyListeners()
        }
    }
    
    /**
     * Clear entire wishlist
     */
    fun clear() {
        wishlist.clear()
        saveToPrefs()
        notifyListeners()
    }
    
    /**
     * Set wishlist from list (used for sync from WooCommerce)
     */
    fun setWishlist(productIds: Set<Int>) {
        wishlist.clear()
        wishlist.addAll(productIds)
        saveToPrefs()
        notifyListeners()
        Log.d(TAG, "Wishlist updated from sync: ${wishlist.size} items")
    }

    fun isInWishlist(productId: Int): Boolean = wishlist.contains(productId)

    fun getWishlistIds(): Set<Int> = wishlist.toSet()
    
    fun getWishlistCount(): Int = wishlist.size

    fun registerListener(callback: () -> Unit) {
        listeners.add(callback)
    }

    fun unregisterListener(callback: () -> Unit) {
        listeners.remove(callback)
    }

    private fun notifyListeners() {
        listeners.forEach { try { it() } catch (_: Exception) {} }
    }
}
