# Kafka Integration & Notification Setup Guide

## Overview
This guide explains how to set up Kafka-based real-time notifications for order status updates in the Natraj app.

## Architecture

```
WooCommerce Store → Kafka Producer (WordPress Plugin) → Kafka Broker → Android App (Consumer) → Push Notifications
```

## Features Implemented

### 1. ✅ Notification System
- **NotificationHelper**: Shows notifications for all order statuses
  - Order Placed 🎉
  - Order Processing 📦
  - Order Shipped 🚚
  - Order Delivered ✅
  - Order Cancelled ❌
  - Order Refunded 💰
  - Order Failed ⚠️

### 2. ✅ Kafka Integration
- **KafkaNotificationService**: Background service that listens to Kafka topics
- **OrderEvent**: Data model for order events
- **KafkaConfig**: Configuration for Kafka broker and topics

### 3. ✅ Wishlist Sync
- **WishlistManager**: Persistent storage with SharedPreferences
- **AccountSyncManager**: Bidirectional sync with WooCommerce
- Wishlist stored in customer `meta_data` field in WooCommerce

### 4. ✅ App Permissions
- `POST_NOTIFICATIONS`: Show push notifications (Android 13+)
- `FOREGROUND_SERVICE`: Run Kafka service in background
- `INTERNET`: Network access for Kafka

## Setup Instructions

### Step 1: Configure Kafka Broker

Update `/app/src/main/java/com/example/natraj/util/kafka/KafkaConfig.kt`:

```kotlin
const val BOOTSTRAP_SERVERS = "your-kafka-server.com:9092" // Your Kafka broker address
var KAFKA_ENABLED = true // Enable Kafka
```

**Kafka Topics Used:**
- `order.placed` - When order is created
- `order.processing` - Order being prepared
- `order.shipped` - Order dispatched
- `order.delivered` - Order completed/delivered
- `order.cancelled` - Order cancelled by user/admin
- `order.refunded` - Refund processed
- `order.failed` - Payment failed
- `order.events` - Combined topic for all events

### Step 2: Install WordPress Plugin (Producer)

Create a custom WordPress plugin to publish order events to Kafka:

```php
<?php
/**
 * Plugin Name: WooCommerce Kafka Producer
 * Description: Publishes order status changes to Kafka for real-time app notifications
 * Version: 1.0
 */

// Install Kafka PHP client: composer require nmred/kafka-php

add_action('woocommerce_order_status_changed', 'publish_order_event_to_kafka', 10, 4);

function publish_order_event_to_kafka($order_id, $old_status, $new_status, $order) {
    try {
        // Kafka configuration
        $broker = new \Kafka\Broker('your-kafka-server.com', '9092');
        $producer = new \Kafka\Producer($broker);
        
        // Prepare order event
        $event = [
            'order_id' => $order_id,
            'order_number' => $order->get_order_number(),
            'customer_id' => $order->get_customer_id(),
            'status' => $new_status,
            'total' => $order->get_total(),
            'timestamp' => time() * 1000,
            'tracking_number' => $order->get_meta('_tracking_number'),
        ];
        
        // Publish to specific topic based on status
        $topic = 'order.' . str_replace('-', '_', $new_status);
        $producer->send($topic, json_encode($event));
        
        // Also publish to combined events topic
        $producer->send('order.events', json_encode($event));
        
        error_log("Order event published to Kafka: $order_id -> $new_status");
    } catch (Exception $e) {
        error_log("Failed to publish order event to Kafka: " . $e->getMessage());
    }
}
```

### Step 3: Enable Notifications in App

The app will automatically:
1. Create notification channels on app start
2. Start Kafka service if configured
3. Listen for order events and show notifications
4. Only show notifications for the logged-in user's orders

### Step 4: Test Notifications

1. **Build and install the app:**
   ```bash
   ./gradlew installDebug
   ```

2. **Grant notification permission:**
   - Android 13+: App will request permission on first launch
   - Settings → Apps → Natraj → Permissions → Notifications → Allow

3. **Place a test order:**
   - Order placed notification will appear immediately
   
4. **Update order status in WooCommerce admin:**
   - Go to: WooCommerce → Orders → Select order
   - Change status: Processing / Completed / Cancelled
   - Notification will appear in the app

## Kafka Event Format

Example order event JSON:

```json
{
  "order_id": "12345",
  "order_number": "#1234",
  "customer_id": 326,
  "status": "processing",
  "tracking_number": "DHL123456789",
  "total": "₹1,299.00",
  "timestamp": 1700000000000,
  "message": null
}
```

## Wishlist Sync

### How It Works
1. **Local Storage**: Wishlist saved in SharedPreferences (persists across app restarts)
2. **WooCommerce Sync**: 
   - Push: Wishlist saved to customer `meta_data` as comma-separated product IDs
   - Pull: Wishlist loaded from customer `meta_data` on sync

### Sync Methods

```kotlin
// Sync wishlist to WooCommerce
AccountSyncManager.syncWishlistToWoo(context)

// Sync wishlist from WooCommerce
AccountSyncManager.syncWishlistFromWoo(context)

// Full sync (account + wishlist)
AccountSyncManager.fullSyncFromWoo(context)
```

### Usage in Activities

```kotlin
// WishlistActivity - add sync button
syncButton.setOnClickListener {
    lifecycleScope.launch {
        val result = AccountSyncManager.syncWishlistFromWoo(this@WishlistActivity)
        if (result.isSuccess) {
            CustomToast.showSuccess(this@WishlistActivity, "Wishlist synced!")
            loadWishlist() // Reload UI
        } else {
            CustomToast.showError(this@WishlistActivity, "Sync failed")
        }
    }
}
```

## Troubleshooting

### Notifications not appearing?
1. Check notification permission in Android settings
2. Verify Kafka service is running: `adb logcat | grep KafkaNotificationService`
3. Check Kafka broker connectivity
4. Ensure `KAFKA_ENABLED = true` in KafkaConfig

### Kafka connection issues?
1. Verify broker address and port
2. Check firewall/network settings
3. Test Kafka connectivity: `telnet your-kafka-server.com 9092`

### Wishlist not syncing?
1. Ensure user is logged in (`AuthManager.isLoggedIn()`)
2. Check WooCommerce API credentials
3. Verify customer has `customer_id` > 0
4. Check logs: `adb logcat | grep AccountSyncManager`

## Dependencies Added

```kotlin
// build.gradle.kts
implementation("org.apache.kafka:kafka-clients:3.6.0")
```

## Files Created

### Notifications
- `util/notification/NotificationHelper.kt` - Notification manager
- `res/drawable/ic_notification.xml` - Notification icon

### Kafka Integration
- `util/kafka/KafkaConfig.kt` - Kafka configuration
- `util/kafka/OrderEvent.kt` - Order event model
- `util/kafka/KafkaNotificationService.kt` - Background service

### Wishlist Sync
- `util/manager/WishlistManager.kt` - Updated with persistence
- `util/sync/AccountSyncManager.kt` - Updated with wishlist sync

## Alternative: Firebase Cloud Messaging (FCM)

If you prefer FCM over Kafka:

1. **Add FCM to build.gradle.kts:**
   ```kotlin
   implementation("com.google.firebase:firebase-messaging-ktx:23.4.0")
   ```

2. **Create FCM service:**
   ```kotlin
   class MyFirebaseMessagingService : FirebaseMessagingService() {
       override fun onMessageReceived(remoteMessage: RemoteMessage) {
           // Handle FCM notification
           val orderId = remoteMessage.data["order_id"] ?: return
           val status = remoteMessage.data["status"] ?: return
           // Show notification using NotificationHelper
       }
   }
   ```

3. **WordPress plugin sends FCM instead of Kafka:**
   - Use Firebase Admin SDK in PHP
   - Send to device tokens instead of Kafka topics

## Next Steps

1. ✅ Configure Kafka broker details
2. ✅ Install WordPress Kafka producer plugin
3. ✅ Test order placement notification
4. ✅ Test order status update notifications
5. ✅ Add sync button to WishlistActivity UI
6. ✅ Add sync button to OrdersActivity UI
7. ✅ Test wishlist sync workflow

## Support

For issues or questions:
- Check app logs: `adb logcat | grep -E "Kafka|Notification|Wishlist"`
- Verify WooCommerce REST API access
- Test Kafka broker connectivity
- Ensure all permissions granted in Android settings
