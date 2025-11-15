package com.example.natraj.util.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.natraj.OrdersActivity
import com.example.natraj.R

/**
 * Helper class for creating and managing app notifications
 * Handles order status notifications (placed, processing, delivered, cancelled)
 */
object NotificationHelper {
    
    private const val CHANNEL_ID_ORDERS = "orders_channel"
    private const val CHANNEL_NAME_ORDERS = "Order Updates"
    private const val CHANNEL_DESC_ORDERS = "Notifications for order status updates"
    
    private const val CHANNEL_ID_PROMOTIONS = "promotions_channel"
    private const val CHANNEL_NAME_PROMOTIONS = "Promotions"
    private const val CHANNEL_DESC_PROMOTIONS = "Special offers and promotions"
    
    /**
     * Initialize notification channels (required for Android O+)
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Orders channel (high priority)
            val ordersChannel = NotificationChannel(
                CHANNEL_ID_ORDERS,
                CHANNEL_NAME_ORDERS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC_ORDERS
                enableLights(true)
                enableVibration(true)
            }
            
            // Promotions channel (low priority)
            val promotionsChannel = NotificationChannel(
                CHANNEL_ID_PROMOTIONS,
                CHANNEL_NAME_PROMOTIONS,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = CHANNEL_DESC_PROMOTIONS
            }
            
            notificationManager.createNotificationChannel(ordersChannel)
            notificationManager.createNotificationChannel(promotionsChannel)
        }
    }
    
    /**
     * Show notification for order placed
     */
    fun showOrderPlacedNotification(context: Context, orderId: String, orderNumber: String) {
        val title = "Order Placed Successfully! 🎉"
        val message = "Your order #$orderNumber has been placed and is being processed."
        showOrderNotification(context, orderId.hashCode(), title, message, orderId)
    }
    
    /**
     * Show notification for order processing
     */
    fun showOrderProcessingNotification(context: Context, orderId: String, orderNumber: String) {
        val title = "Order Processing 📦"
        val message = "Your order #$orderNumber is being prepared for shipping."
        showOrderNotification(context, orderId.hashCode(), title, message, orderId)
    }
    
    /**
     * Show notification for order shipped
     */
    fun showOrderShippedNotification(context: Context, orderId: String, orderNumber: String, trackingNumber: String? = null) {
        val title = "Order Shipped! 🚚"
        val message = if (trackingNumber != null) {
            "Your order #$orderNumber has been shipped. Tracking: $trackingNumber"
        } else {
            "Your order #$orderNumber has been shipped and is on its way!"
        }
        showOrderNotification(context, orderId.hashCode(), title, message, orderId)
    }
    
    /**
     * Show notification for order delivered
     */
    fun showOrderDeliveredNotification(context: Context, orderId: String, orderNumber: String) {
        val title = "Order Delivered! ✅"
        val message = "Your order #$orderNumber has been successfully delivered. Thank you for shopping with us!"
        showOrderNotification(context, orderId.hashCode(), title, message, orderId)
    }
    
    /**
     * Show notification for order cancelled
     */
    fun showOrderCancelledNotification(context: Context, orderId: String, orderNumber: String) {
        val title = "Order Cancelled ❌"
        val message = "Your order #$orderNumber has been cancelled. Refund will be processed within 5-7 business days."
        showOrderNotification(context, orderId.hashCode(), title, message, orderId)
    }
    
    /**
     * Show notification for order refunded
     */
    fun showOrderRefundedNotification(context: Context, orderId: String, orderNumber: String) {
        val title = "Refund Processed 💰"
        val message = "Refund for order #$orderNumber has been processed. Amount will be credited to your account."
        showOrderNotification(context, orderId.hashCode(), title, message, orderId)
    }
    
    /**
     * Show notification for order failed
     */
    fun showOrderFailedNotification(context: Context, orderId: String, orderNumber: String, reason: String? = null) {
        val title = "Order Failed ⚠️"
        val message = if (reason != null) {
            "Order #$orderNumber failed: $reason. Please try again."
        } else {
            "Order #$orderNumber could not be completed. Please contact support."
        }
        showOrderNotification(context, orderId.hashCode(), title, message, orderId)
    }
    
    /**
     * Generic method to show order notification
     */
    private fun showOrderNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String,
        orderId: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Intent to open OrdersActivity when notification is clicked
        val intent = Intent(context, OrdersActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("order_id", orderId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ORDERS)
            .setSmallIcon(R.drawable.ic_notification) // You'll need to add this icon
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        notificationManager.notify(notificationId, notification)
    }
    
    /**
     * Show promotion notification
     */
    fun showPromotionNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val intent = Intent(context, com.example.natraj.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_PROMOTIONS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
