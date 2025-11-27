package com.example.natraj.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.natraj.OrderManager
import com.example.natraj.OrdersActivity
import com.example.natraj.R
import com.example.natraj.data.WooRepository
import com.example.natraj.util.CustomToast

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class OrderConfirmationActivity : AppCompatActivity() {

    private val TAG = "OrderConfirmation"
    // private lateinit var trackingManager: DelhiveryTrackingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_confirmation)

        Log.d(TAG, "=== OrderConfirmationActivity Started ===")
        
        // trackingManager = DelhiveryTrackingManager(this)

        val orderId = intent.getStringExtra("order_id") ?: run {
            Log.e(TAG, "No order_id provided, finishing activity")
            finish()
            return
        }
        
        val wooOrderId = intent.getIntExtra("order_woo_id", -1)
        val orderStatus = intent.getStringExtra("order_status") ?: "pending"
        val orderTotal = intent.getStringExtra("order_total") ?: ""
        val isOffline = intent.getBooleanExtra("offline_mode", false)

        Log.d(TAG, "Order ID: $orderId")
        Log.d(TAG, "WooCommerce Order ID: $wooOrderId")
        Log.d(TAG, "Order Status: $orderStatus")
        Log.d(TAG, "Order Total: $orderTotal")
        Log.d(TAG, "Offline Mode: $isOffline")

        val orderIdText = findViewById<TextView>(R.id.confirmation_order_id)
        val dateText = findViewById<TextView>(R.id.confirmation_date)
        val addressText = findViewById<TextView>(R.id.confirmation_address)
        val paymentText = findViewById<TextView>(R.id.confirmation_payment)
        val amountText = findViewById<TextView>(R.id.confirmation_amount)
        val trackingSection = findViewById<LinearLayout>(R.id.confirmation_tracking_section)
        val trackingText = findViewById<TextView>(R.id.confirmation_tracking_text)
        val trackButton = findViewById<Button>(R.id.confirmation_track_btn)
        val refreshStatusButton = findViewById<Button>(R.id.confirmation_refresh_status_btn)
        val viewOrdersButton = findViewById<Button>(R.id.confirmation_view_orders_btn)
        val continueShoppingButton = findViewById<Button>(R.id.confirmation_continue_shopping_btn)
        val cancelOrderButton = findViewById<Button>(R.id.confirmation_cancel_order_btn)

        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        
        orderIdText.text = "Order ID: #$orderId"
        dateText.text = dateFormat.format(System.currentTimeMillis())
        
        // Try to get order from OrderManager for offline orders
        if (isOffline) {
            Log.d(TAG, "Loading offline order details...")
            val order = OrderManager.getOrderById(orderId)
            order?.let {
                addressText.text = "${it.deliveryAddress.address}, ${it.deliveryAddress.city}, ${it.deliveryAddress.state} - ${it.deliveryAddress.pincode}"
                paymentText.text = it.paymentMethod
                amountText.text = "₹${it.totalAmount.toInt()}"
                Log.d(TAG, "Offline order loaded successfully")
            }
            trackingSection.visibility = View.GONE
            Log.d(TAG, "Tracking section hidden for offline order")
        } else {
            // For WooCommerce orders
            Log.d(TAG, "Loading WooCommerce order details...")
            addressText.text = "Order placed successfully"
            
            // Update UI based on order status
            val statusText = orderStatus.replaceFirstChar { it.uppercase() }
            paymentText.text = "Status: $statusText"
            amountText.text = if (orderTotal.isNotBlank()) "₹$orderTotal" else ""
            
            // Show different message for cancelled orders
            if (orderStatus.lowercase() == "cancelled") {
                val successIcon = findViewById<TextView>(R.id.confirmation_success_icon)
                val successTitle = findViewById<TextView>(R.id.confirmation_success_title)
                val successMessage = findViewById<TextView>(R.id.confirmation_success_message)
                
                successIcon?.text = "❌"
                successTitle?.text = "Order Cancelled"
                successMessage?.text = "Your order has been cancelled"
            }
            
            // Load tracking information if available
            if (wooOrderId > 0) {
                Log.d(TAG, "Loading tracking info for order $wooOrderId...")
                loadTrackingInfo(wooOrderId, trackingSection, trackingText, trackButton)
                
                // Show refresh status button for WooCommerce orders
                refreshStatusButton.visibility = View.VISIBLE
                setupRefreshButton(wooOrderId, refreshStatusButton, paymentText)
                
                // Show cancel button only for pending/on-hold/processing orders
                if (orderStatus.lowercase() in listOf("pending", "on-hold", "processing")) {
                    cancelOrderButton.visibility = View.VISIBLE
                    setupCancelButton(wooOrderId, orderStatus, cancelOrderButton, paymentText)
                } else {
                    cancelOrderButton.visibility = View.GONE
                }
            } else {
                Log.w(TAG, "No valid WooCommerce order ID, hiding tracking section")
                trackingSection.visibility = View.GONE
                refreshStatusButton.visibility = View.GONE
                cancelOrderButton.visibility = View.GONE
            }
        }

        viewOrdersButton.setOnClickListener {
            val intent = Intent(this, OrdersActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        continueShoppingButton.setOnClickListener {
            // Add button press animation
            it.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction {
                    it.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(100)
                        .start()
                    
                    // Show promotional message before navigating
                    val messages = arrayOf(
                        "Thanks for shopping with us! 🎉",
                        "Happy shopping! Get 10% off on your next order! 🛒",
                        "Explore more amazing products! ⭐",
                        "Your next favorite item awaits! 💫"
                    )
                    val randomMessage = messages.random()
                    CustomToast.showSuccess(this, randomMessage, Toast.LENGTH_SHORT)
                    
                    // Small delay for better UX
                    it.postDelayed({
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        intent.putExtra("open_home", true) // Flag to open home tab
                        startActivity(intent)
                        finish()
                        
                        // Add smooth transition
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    }, 300)
                }
                .start()
        }
    }
    
    private fun loadTrackingInfo(
        wooOrderId: Int,
        trackingSection: LinearLayout,
        trackingText: TextView,
        trackButton: Button
    ) {
        // Tracking feature temporarily disabled
        Log.d(TAG, "Tracking feature disabled")
        trackingSection.visibility = View.GONE
        trackButton.visibility = View.GONE
    }
    
    private fun setupRefreshButton(wooOrderId: Int, refreshButton: Button, paymentText: TextView) {
        refreshButton.setOnClickListener {
            refreshOrderStatus(wooOrderId, refreshButton, paymentText)
        }
    }
    
    private fun setupCancelButton(wooOrderId: Int, currentStatus: String, cancelButton: Button, paymentText: TextView) {
        Log.d(TAG, "Setting up cancel button for order $wooOrderId with status $currentStatus")
        cancelButton.setOnClickListener {
            Log.d(TAG, "Cancel button clicked for order $wooOrderId")
            AlertDialog.Builder(this)
                .setTitle("Cancel Order")
                .setMessage("Are you sure you want to cancel this order? This action cannot be undone.")
                .setPositiveButton("Yes, Cancel") { _, _ ->
                    Log.d(TAG, "User confirmed order cancellation")
                    cancelOrder(wooOrderId, cancelButton, paymentText)
                }
                .setNegativeButton("No") { _, _ ->
                    Log.d(TAG, "User cancelled the cancellation dialog")
                }
                .show()
        }
    }
    
    private fun refreshOrderStatus(wooOrderId: Int, refreshButton: Button, paymentText: TextView) {
        Log.d(TAG, "Refreshing order status for $wooOrderId...")
        
        refreshButton.isEnabled = false
        val originalText = refreshButton.text
        refreshButton.text = "Refreshing..."
        
        lifecycleScope.launch {
            try {
                val repo = WooRepository(this@OrderConfirmationActivity)
                
                val order = withContext(Dispatchers.IO) {
                    repo.getOrderById(wooOrderId)
                }
                
                if (order != null) {
                    Log.d(TAG, "✓ Order status refreshed: ${order.status}")
                    paymentText.text = "Status: ${order.status.replaceFirstChar { it.uppercase() }}"
                    CustomToast.showSuccess(this@OrderConfirmationActivity, 
                        "Order status updated")
                } else {
                    Log.w(TAG, "Order not found")
                    CustomToast.showError(this@OrderConfirmationActivity, 
                        "Failed to fetch order details")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "✗ Failed to refresh order status", e)
                CustomToast.showError(this@OrderConfirmationActivity, 
                    "Failed to refresh: ${e.message}")
            } finally {
                refreshButton.isEnabled = true
                refreshButton.text = originalText
            }
        }
    }
    
    private fun cancelOrder(wooOrderId: Int, cancelButton: Button, paymentText: TextView) {
        Log.d(TAG, "Cancelling order $wooOrderId...")
        
        cancelButton.isEnabled = false
        cancelButton.text = "Cancelling..."
        
        lifecycleScope.launch {
            try {
                val repo = WooRepository(this@OrderConfirmationActivity)
                
                Log.d(TAG, "Making API call to update order status to cancelled...")
                val response = withContext(Dispatchers.IO) {
                    repo.updateOrder(wooOrderId, status = "cancelled")
                }
                
                Log.d(TAG, "✓ Order cancelled successfully: ${response.status}")
                Log.d(TAG, "Response: id=${response.id}, status=${response.status}")
                
                // Show notification for order cancellation
                com.example.natraj.util.notification.NotificationHelper.showOrderCancelledNotification(
                    this@OrderConfirmationActivity,
                    response.id.toString(),
                    response.number ?: wooOrderId.toString()
                )

                withContext(Dispatchers.Main) {
                    CustomToast.showSuccess(this@OrderConfirmationActivity, 
                        "Order cancelled successfully")
                    
                    // Update UI
                    paymentText.text = "Status: Cancelled"
                    cancelButton.visibility = View.GONE
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "✗ Failed to cancel order", e)
                Log.e(TAG, "Error type: ${e.javaClass.simpleName}")
                Log.e(TAG, "Error message: ${e.message}")
                e.printStackTrace()
                
                withContext(Dispatchers.Main) {
                    CustomToast.showError(this@OrderConfirmationActivity, 
                        "Failed to cancel order: ${e.message}", 
                        Toast.LENGTH_LONG)
                    
                    cancelButton.isEnabled = true
                    cancelButton.text = "CANCEL ORDER"
                }
            }
        }
    }
}
