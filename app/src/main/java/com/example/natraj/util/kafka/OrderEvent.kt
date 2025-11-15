package com.example.natraj.util.kafka

import com.google.gson.annotations.SerializedName

/**
 * Data class representing an order event from Kafka
 */
data class OrderEvent(
    @SerializedName("order_id")
    val orderId: String,
    
    @SerializedName("order_number")
    val orderNumber: String,
    
    @SerializedName("customer_id")
    val customerId: Int,
    
    @SerializedName("status")
    val status: OrderStatus,
    
    @SerializedName("tracking_number")
    val trackingNumber: String? = null,
    
    @SerializedName("total")
    val total: String? = null,
    
    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis(),
    
    @SerializedName("message")
    val message: String? = null
)

/**
 * Order status enum matching WooCommerce statuses
 */
enum class OrderStatus(val value: String) {
    @SerializedName("pending")
    PENDING("pending"),
    
    @SerializedName("processing")
    PROCESSING("processing"),
    
    @SerializedName("on-hold")
    ON_HOLD("on-hold"),
    
    @SerializedName("completed")
    COMPLETED("completed"),
    
    @SerializedName("cancelled")
    CANCELLED("cancelled"),
    
    @SerializedName("refunded")
    REFUNDED("refunded"),
    
    @SerializedName("failed")
    FAILED("failed"),
    
    @SerializedName("shipped")
    SHIPPED("shipped"),
    
    @SerializedName("delivered")
    DELIVERED("delivered");
    
    companion object {
        fun fromString(value: String): OrderStatus {
            return values().find { it.value == value } ?: PENDING
        }
    }
}
