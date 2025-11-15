package com.example.natraj.util.kafka

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.natraj.AuthManager
import com.example.natraj.util.notification.NotificationHelper
import com.google.gson.Gson
import kotlinx.coroutines.*
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import java.time.Duration
import java.util.*

/**
 * Background service that listens to Kafka for order status updates
 * and shows notifications to the user
 */
class KafkaNotificationService : Service() {
    
    companion object {
        private const val TAG = "KafkaNotificationService"
        private var isRunning = false
        
        fun isRunning(): Boolean = isRunning
    }
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var kafkaConsumer: KafkaConsumer<String, String>? = null
    private val gson = Gson()
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        
        if (!KafkaConfig.isConfigured()) {
            Log.w(TAG, "Kafka not configured, service will not start polling")
            return
        }
        
        startKafkaConsumer()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        isRunning = true
        return START_STICKY // Restart service if killed
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        isRunning = false
        stopKafkaConsumer()
        serviceScope.cancel()
    }
    
    /**
     * Initialize and start Kafka consumer
     */
    private fun startKafkaConsumer() {
        serviceScope.launch {
            try {
                // Configure Kafka consumer
                val props = Properties().apply {
                    put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfig.BOOTSTRAP_SERVERS)
                    put(ConsumerConfig.GROUP_ID_CONFIG, KafkaConfig.CONSUMER_GROUP_ID)
                    put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
                    put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
                    put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest") // Only new messages
                    put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
                }
                
                kafkaConsumer = KafkaConsumer<String, String>(props)
                
                // Subscribe to all order event topics
                val topics = listOf(
                    KafkaConfig.TOPIC_ORDER_PLACED,
                    KafkaConfig.TOPIC_ORDER_PROCESSING,
                    KafkaConfig.TOPIC_ORDER_SHIPPED,
                    KafkaConfig.TOPIC_ORDER_DELIVERED,
                    KafkaConfig.TOPIC_ORDER_CANCELLED,
                    KafkaConfig.TOPIC_ORDER_REFUNDED,
                    KafkaConfig.TOPIC_ORDER_FAILED,
                    KafkaConfig.TOPIC_ORDER_EVENTS // Combined topic
                )
                kafkaConsumer?.subscribe(topics)
                
                Log.d(TAG, "Kafka consumer started, subscribed to topics: $topics")
                
                // Start polling for messages
                pollMessages()
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start Kafka consumer", e)
            }
        }
    }
    
    /**
     * Poll Kafka for new messages
     */
    private suspend fun pollMessages() {
        while (serviceScope.isActive) {
            try {
                val records = kafkaConsumer?.poll(Duration.ofMillis(KafkaConfig.POLL_TIMEOUT_MS))
                
                records?.forEach { record ->
                    try {
                        val orderEvent = gson.fromJson(record.value(), OrderEvent::class.java)
                        handleOrderEvent(orderEvent)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse order event: ${record.value()}", e)
                    }
                }
                
                // Wait before next poll
                delay(KafkaConfig.POLL_INTERVAL_MS)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error polling Kafka", e)
                delay(10000) // Wait 10s before retrying
            }
        }
    }
    
    /**
     * Handle order event and show appropriate notification
     */
    private fun handleOrderEvent(event: OrderEvent) {
        // Only show notifications for current user's orders
        val currentCustomerId = AuthManager.getCustomerId()
        if (event.customerId != currentCustomerId) {
            Log.d(TAG, "Ignoring event for different customer: ${event.customerId}")
            return
        }
        
        Log.d(TAG, "Handling order event: ${event.status} for order ${event.orderNumber}")
        
        when (event.status) {
            OrderStatus.PENDING -> {
                // Order placed (pending payment)
                NotificationHelper.showOrderPlacedNotification(
                    this,
                    event.orderId,
                    event.orderNumber
                )
            }
            OrderStatus.PROCESSING -> {
                NotificationHelper.showOrderProcessingNotification(
                    this,
                    event.orderId,
                    event.orderNumber
                )
            }
            OrderStatus.SHIPPED -> {
                NotificationHelper.showOrderShippedNotification(
                    this,
                    event.orderId,
                    event.orderNumber,
                    event.trackingNumber
                )
            }
            OrderStatus.DELIVERED, OrderStatus.COMPLETED -> {
                NotificationHelper.showOrderDeliveredNotification(
                    this,
                    event.orderId,
                    event.orderNumber
                )
            }
            OrderStatus.CANCELLED -> {
                NotificationHelper.showOrderCancelledNotification(
                    this,
                    event.orderId,
                    event.orderNumber
                )
            }
            OrderStatus.REFUNDED -> {
                NotificationHelper.showOrderRefundedNotification(
                    this,
                    event.orderId,
                    event.orderNumber
                )
            }
            OrderStatus.FAILED -> {
                NotificationHelper.showOrderFailedNotification(
                    this,
                    event.orderId,
                    event.orderNumber,
                    event.message
                )
            }
            OrderStatus.ON_HOLD -> {
                // Show generic notification for on-hold status
                NotificationHelper.showOrderProcessingNotification(
                    this,
                    event.orderId,
                    event.orderNumber
                )
            }
        }
    }
    
    /**
     * Stop Kafka consumer
     */
    private fun stopKafkaConsumer() {
        try {
            kafkaConsumer?.close()
            kafkaConsumer = null
            Log.d(TAG, "Kafka consumer stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping Kafka consumer", e)
        }
    }
}
