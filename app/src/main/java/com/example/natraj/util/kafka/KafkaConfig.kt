package com.example.natraj.util.kafka

/**
 * Kafka configuration for order status notifications
 * Configure your Kafka broker details here
 */
object KafkaConfig {
    
    // Kafka broker address (update with your server)
    const val BOOTSTRAP_SERVERS = "your-kafka-server:9092"
    
    // Kafka topics
    const val TOPIC_ORDER_PLACED = "order.placed"
    const val TOPIC_ORDER_PROCESSING = "order.processing"
    const val TOPIC_ORDER_SHIPPED = "order.shipped"
    const val TOPIC_ORDER_DELIVERED = "order.delivered"
    const val TOPIC_ORDER_CANCELLED = "order.cancelled"
    const val TOPIC_ORDER_REFUNDED = "order.refunded"
    const val TOPIC_ORDER_FAILED = "order.failed"
    
    // Combined topic for all order events
    const val TOPIC_ORDER_EVENTS = "order.events"
    
    // Consumer group ID
    const val CONSUMER_GROUP_ID = "natraj-app-consumer"
    
    // Polling configuration
    const val POLL_INTERVAL_MS = 5000L // Poll every 5 seconds
    const val POLL_TIMEOUT_MS = 1000L
    
    // Enable/disable Kafka (useful for development)
    var KAFKA_ENABLED = false // Set to true when Kafka is configured
    
    /**
     * Check if Kafka is properly configured
     */
    fun isConfigured(): Boolean {
        return KAFKA_ENABLED && BOOTSTRAP_SERVERS != "your-kafka-server:9092"
    }
}
