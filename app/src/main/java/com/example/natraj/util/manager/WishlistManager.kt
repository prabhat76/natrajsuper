package com.example.natraj

object WishlistManager {
    private val wishlist = mutableSetOf<Int>() // Store product IDs
    private val listeners = mutableListOf<() -> Unit>()

    fun toggle(productId: Int): Boolean {
        val added = if (wishlist.contains(productId)) {
            wishlist.remove(productId)
            false
        } else {
            wishlist.add(productId)
            true
        }
        notifyListeners()
        return added
    }

    fun isInWishlist(productId: Int): Boolean = wishlist.contains(productId)

    fun getWishlistIds(): Set<Int> = wishlist.toSet()

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
