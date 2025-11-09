package com.example.natraj

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
import com.example.natraj.data.WooRepository
import com.example.natraj.util.CustomToast
import com.example.natraj.util.tracking.DelhiveryTrackingManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class OrderConfirmationActivity : AppCompatActivity() {

    private val TAG = "OrderConfirmation"
    private lateinit var trackingManager: DelhiveryTrackingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_confirmation)

        Log.d(TAG, "=== OrderConfirmationActivity Started ===")
        
        trackingManager = DelhiveryTrackingManager(this)

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
            paymentText.text = "Status: ${orderStatus.replaceFirstChar { it.uppercase() }}"
            amountText.text = if (orderTotal.isNotBlank()) "₹$orderTotal" else ""
            
            // Load tracking information if available
            if (wooOrderId > 0) {
                Log.d(TAG, "Loading tracking info for order $wooOrderId...")
                loadTrackingInfo(wooOrderId, trackingSection, trackingText, trackButton)
                
                // Show cancel button only for pending/on-hold orders
                if (orderStatus.lowercase() in listOf("pending", "on-hold", "processing")) {
                    cancelOrderButton.visibility = View.VISIBLE
                    setupCancelButton(wooOrderId, orderStatus, cancelOrderButton)
                } else {
                    cancelOrderButton.visibility = View.GONE
                }
            } else {
                Log.w(TAG, "No valid WooCommerce order ID, hiding tracking section")
                trackingSection.visibility = View.GONE
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
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
    
    private fun loadTrackingInfo(
        wooOrderId: Int,
        trackingSection: LinearLayout,
        trackingText: TextView,
        trackButton: Button
    ) {
        Log.d(TAG, "Fetching tracking info for order $wooOrderId...")
        lifecycleScope.launch {
            try {
                val trackingInfo = trackingManager.getTrackingInfo(wooOrderId)
                
                if (trackingInfo != null) {
                    Log.d(TAG, "✓ Tracking info found:")
                    Log.d(TAG, "  Tracking Number: ${trackingInfo.trackingNumber}")
                    Log.d(TAG, "  Provider: ${trackingInfo.provider}")
                    Log.d(TAG, "  AWB: ${trackingInfo.awbNumber}")
                    Log.d(TAG, "  URL: ${trackingInfo.trackingUrl}")
                    
                    trackingSection.visibility = View.VISIBLE
                    trackingText.text = "Tracking Number: ${trackingInfo.trackingNumber}\n" +
                                       "Provider: ${trackingInfo.provider}" +
                                       if (trackingInfo.awbNumber.isNotBlank()) 
                                           "\nAWB: ${trackingInfo.awbNumber}" 
                                       else ""
                    
                    trackButton.visibility = View.VISIBLE
                    trackButton.setOnClickListener {
                        Log.d(TAG, "Opening tracking URL: ${trackingInfo.trackingUrl}")
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(trackingInfo.trackingUrl))
                        startActivity(intent)
                    }
                } else {
                    Log.w(TAG, "No tracking info available for order $wooOrderId")
                    trackingSection.visibility = View.GONE
                }
            } catch (e: Exception) {
                Log.e(TAG, "✗ Failed to load tracking info", e)
                trackingSection.visibility = View.GONE
            }
        }
    }
    
    private fun setupCancelButton(wooOrderId: Int, currentStatus: String, cancelButton: Button) {
        cancelButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Cancel Order")
                .setMessage("Are you sure you want to cancel this order? This action cannot be undone.")
                .setPositiveButton("Yes, Cancel") { _, _ ->
                    cancelOrder(wooOrderId, cancelButton)
                }
                .setNegativeButton("No", null)
                .show()
        }
    }
    
    private fun cancelOrder(wooOrderId: Int, cancelButton: Button) {
        Log.d(TAG, "Cancelling order $wooOrderId...")
        
        cancelButton.isEnabled = false
        cancelButton.text = "Cancelling..."
        
        lifecycleScope.launch {
            try {
                val repo = WooRepository(this@OrderConfirmationActivity)
                
                val response = withContext(Dispatchers.IO) {
                    repo.updateOrder(wooOrderId, mapOf("status" to "cancelled"))
                }
                
                Log.d(TAG, "✓ Order cancelled successfully")
                CustomToast.showSuccess(this@OrderConfirmationActivity, 
                    "Order cancelled successfully")
                
                // Update UI
                val paymentText = findViewById<TextView>(R.id.confirmation_payment)
                paymentText.text = "Status: Cancelled"
                cancelButton.visibility = View.GONE
                
            } catch (e: Exception) {
                Log.e(TAG, "✗ Failed to cancel order", e)
                CustomToast.showError(this@OrderConfirmationActivity, 
                    "Failed to cancel order: ${e.message}", 
                    Toast.LENGTH_LONG)
                
                cancelButton.isEnabled = true
                cancelButton.text = "Cancel Order"
            }
        }
    }
}
