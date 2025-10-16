package com.example.natraj

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object CartManager {
    private val items = mutableListOf<CartItem>()
    private val listeners = mutableListOf<() -> Unit>()
    private var sharedPrefs: SharedPreferences? = null
    private val gson = Gson()
    
    fun initialize(context: Context) {
        sharedPrefs = context.getSharedPreferences("cart_prefs", Context.MODE_PRIVATE)
        loadCartFromStorage()
    }
    
    private fun loadCartFromStorage() {
        sharedPrefs?.let { prefs ->
            val cartJson = prefs.getString("cart_items", "[]")
            try {
                val type = object : TypeToken<List<CartItem>>() {}.type
                val savedItems: List<CartItem> = gson.fromJson(cartJson, type) ?: emptyList()
                items.clear()
                items.addAll(savedItems)
            } catch (e: Exception) {
                items.clear() // Clear if parsing fails
            }
        }
    }
    
    private fun saveCartToStorage() {
        sharedPrefs?.let { prefs ->
            try {
                val cartJson = gson.toJson(items)
                prefs.edit().putString("cart_items", cartJson).apply()
            } catch (e: Exception) {
                // Handle save error silently
            }
        }
    }

    fun add(product: Product, qty: Int = 1) {
        val existing = items.find { it.product.id == product.id }
        if (existing != null) {
            existing.quantity += qty
        } else {
            items.add(CartItem(product, qty))
        }
        saveCartToStorage()
        notifyListeners()
    }

    fun remove(product: Product) {
        items.removeAll { it.product.id == product.id }
        saveCartToStorage()
        notifyListeners()
    }

    fun updateQuantity(product: Product, qty: Int) {
        val item = items.find { it.product.id == product.id }
        if (item != null) {
            if (qty <= 0) {
                remove(product)
            } else {
                item.quantity = qty
                saveCartToStorage()
                notifyListeners()
            }
        }
    }

    fun clear() {
        items.clear()
        saveCartToStorage()
        notifyListeners()
    }

    fun getItems(): List<CartItem> = items.toList()

    fun registerListener(callback: () -> Unit) {
        listeners.add(callback)
    }

    fun unregisterListener(callback: () -> Unit) {
        listeners.remove(callback)
    }

    private fun notifyListeners() {
        listeners.forEach { try { it() } catch (_: Exception) {} }
    }

    fun getItemCount(): Int = items.sumOf { it.quantity }

    fun getProductCount(): Int = items.size

    fun subtotal(): Double = items.sumOf { it.product.price * it.quantity }

    fun discountAmount(): Double = items.sumOf { ((it.product.originalPrice - it.product.price).coerceAtLeast(0.0)) * it.quantity }

    fun total(): Double = (subtotal() - discountAmount())
}
