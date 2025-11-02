package com.example.natraj

import java.io.Serializable
import java.util.Date

data class Order(
    val id: String,
    val items: List<CartItem>,
    val totalAmount: Double,
    val orderDate: Date,
    val deliveryAddress: Address,
    val paymentMethod: String,
    val status: OrderStatus = OrderStatus.PLACED
) : Serializable

enum class OrderStatus {
    PLACED, CONFIRMED, SHIPPED, OUT_FOR_DELIVERY, DELIVERED, CANCELLED
}

data class Address(
    val name: String,
    val mobile: String,
    val pincode: String,
    val address: String,
    val locality: String,
    val city: String,
    val state: String,
    val addressType: String = "Home" // Home, Work, Other
) : Serializable
