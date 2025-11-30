package com.example.natraj

import java.util.Date
import java.util.UUID

object OrderManager {
    private val orders = mutableListOf<Order>()
    private val listeners = mutableListOf<() -> Unit>()

    fun placeOrder(items: List<CartItem>, address: Address, paymentMethod: String): Order {
        val total = items.sumOf { (it.product.price * it.quantity).toDouble() }
        val order = Order(
            id = UUID.randomUUID().toString().substring(0, 8).uppercase(),
            items = items.toList(),
            totalAmount = total,
            orderDate = Date(),
            deliveryAddress = address,
            paymentMethod = paymentMethod,
            status = OrderStatus.PLACED
        )
        orders.add(0, order) // Add to beginning
        notifyListeners()
        return order
    }

    fun getOrders(): List<Order> = orders.toList()

    fun getOrderById(id: String): Order? = orders.find { it.id == id }

    fun addListener(listener: () -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: () -> Unit) {
        listeners.remove(listener)
    }

    private fun notifyListeners() {
        listeners.forEach { it() }
    }

    // helper used by product UI to detect if user purchased a product before
    fun hasPurchasedProduct(productId: Int): Boolean {
        return orders.any { order -> order.items.any { it.product.id == productId } }
    }
}
