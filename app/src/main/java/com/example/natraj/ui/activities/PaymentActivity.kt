package com.example.natraj

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.natraj.data.WooRepository
import com.example.natraj.data.woo.WooBilling
import com.example.natraj.data.woo.WooMetaData
import com.example.natraj.data.woo.WooOrderLineItem
import com.example.natraj.data.woo.WooPrefs
import com.example.natraj.data.woo.WooShipping
import com.example.natraj.util.CustomToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PaymentActivity : AppCompatActivity() {

    private lateinit var address: Address
    private lateinit var progressBar: ProgressBar
    private lateinit var placeOrderButton: Button
    private val TAG = "PaymentActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)
        
        Log.d(TAG, "=== PaymentActivity Started ===")

        address = intent.getSerializableExtra("address") as? Address ?: run {
            Log.e(TAG, "No address provided, finishing")
            finish()
            return
        }
        
        Log.d(TAG, "Address: ${address.name}, ${address.city}")
        
        val backButton = findViewById<ImageView>(R.id.payment_back_button)
        val addressText = findViewById<TextView>(R.id.payment_address_text)
        val itemsCount = findViewById<TextView>(R.id.payment_items_count)
        val subtotal = findViewById<TextView>(R.id.payment_subtotal)
        val discount = findViewById<TextView>(R.id.payment_discount)
        val delivery = findViewById<TextView>(R.id.payment_delivery)
        val total = findViewById<TextView>(R.id.payment_total)
        val paymentGroup = findViewById<RadioGroup>(R.id.payment_method_group)
        placeOrderButton = findViewById<Button>(R.id.payment_place_order_btn)
        progressBar = findViewById<ProgressBar>(R.id.payment_progress_bar)

        backButton.setOnClickListener {
            finish()
        }

        // Display address
        addressText.text = "${address.name}, ${address.mobile}\n${address.address}, ${address.locality}\n${address.city}, ${address.state} - ${address.pincode}"

        // Calculate amounts
        val cartItems = CartManager.getItems()
        Log.d(TAG, "Cart has ${cartItems.size} items")
        
        val itemCount = cartItems.size
        val subtotalAmount = cartItems.sumOf { it.product.price * it.quantity }
        val discountAmount = subtotalAmount * 0.05 // 5% discount
        val deliveryCharge = if (subtotalAmount > 50000) 0.0 else 500.0
        val totalAmount = subtotalAmount - discountAmount + deliveryCharge

        itemsCount.text = "Price ($itemCount items)"
        subtotal.text = "₹${subtotalAmount.toInt()}"
        discount.text = "− ₹${discountAmount.toInt()}"
        delivery.text = if (deliveryCharge == 0.0) "FREE" else "₹${deliveryCharge.toInt()}"
        total.text = "₹${totalAmount.toInt()}"
        
        Log.d(TAG, "Total amount: ₹${totalAmount.toInt()}")

        // Add payment options statically for reliability
        setupPaymentOptions(paymentGroup)

        placeOrderButton.setOnClickListener {
            Log.d(TAG, "Place Order clicked")
            placeOrder(paymentGroup, cartItems, totalAmount)
        }
    }
    
    private fun setupPaymentOptions(paymentGroup: RadioGroup) {
        Log.d(TAG, "Setting up payment options")
        
        // Clear any existing options
        paymentGroup.removeAllViews()
        
        // Add static reliable payment options
        val options = listOf(
            "cod" to "Cash on Delivery",
            "razorpay" to "Online Payment (UPI/Card/Netbanking)",
            "bacs" to "Direct Bank Transfer"
        )
        
        options.forEachIndexed { index, (id, title) ->
            val radioButton = RadioButton(this).apply {
                this.id = View.generateViewId()
                text = title
                tag = id
                textSize = 15f
                setPadding(24, 24, 24, 24)
                isChecked = (index == 0) // First option checked by default
            }
            paymentGroup.addView(radioButton)
            Log.d(TAG, "Added payment option: $title")
        }
        
        Log.d(TAG, "Payment options setup complete, ${options.size} options added")
    }

    private fun placeOrder(paymentGroup: RadioGroup, cartItems: List<CartItem>, totalAmount: Double) {
        val selectedId = paymentGroup.checkedRadioButtonId
        if (selectedId == -1) {
            CustomToast.showWarning(this, "Please select a payment method")
            Log.w(TAG, "No payment method selected")
            return
        }
        
        val selectedRadio = findViewById<RadioButton>(selectedId)
        val gatewayId = selectedRadio.tag as? String ?: "cod"
        val paymentTitle = selectedRadio.text.toString()
        
        Log.d(TAG, "Selected payment: $paymentTitle (gateway: $gatewayId)")
        
        // Show progress
        progressBar.visibility = View.VISIBLE
        placeOrderButton.isEnabled = false
        
        val prefs = WooPrefs(this)
        val hasWooConfig = !prefs.baseUrl.isNullOrBlank() && 
                          !prefs.consumerKey.isNullOrBlank() && 
                          !prefs.consumerSecret.isNullOrBlank()

        Log.d(TAG, "WooCommerce configured: $hasWooConfig")
        Log.d(TAG, "Base URL: ${prefs.baseUrl}")

        if (!hasWooConfig) {
            Log.w(TAG, "WooCommerce not configured, using offline mode")
            CustomToast.showWarning(this, "WooCommerce not configured. Using offline mode.", Toast.LENGTH_LONG)
            placeOfflineOrder(cartItems, gatewayId, paymentTitle)
            return
        }

        lifecycleScope.launch {
            try {
                Log.d(TAG, "Starting WooCommerce order creation...")
                val repo = WooRepository(this@PaymentActivity)
                
                // Prepare line items
                val lineItems = cartItems.map { 
                    WooOrderLineItem(
                        product_id = it.product.id,
                        quantity = it.quantity
                    ) 
                }
                Log.d(TAG, "Line items: ${lineItems.size} products")
                
                // Prepare billing info
                val nameParts = address.name.split(" ", limit = 2)
                val billing = WooBilling(
                    first_name = nameParts.getOrNull(0) ?: address.name,
                    last_name = nameParts.getOrNull(1) ?: "",
                    address_1 = address.address,
                    address_2 = address.locality,
                    city = address.city,
                    state = address.state,
                    postcode = address.pincode,
                    email = AuthManager.getUserEmail().ifBlank { "customer@natrajsuper.com" },
                    phone = address.mobile,
                    country = "IN"
                )
                
                // Prepare shipping info
                val shipping = WooShipping(
                    first_name = billing.first_name,
                    last_name = billing.last_name,
                    address_1 = billing.address_1,
                    address_2 = billing.address_2,
                    city = billing.city,
                    state = billing.state,
                    postcode = billing.postcode,
                    country = "IN"
                )
                
                // Add custom meta data for tracking support
                val metaData = listOf(
                    WooMetaData("_app_order", "true"),
                    WooMetaData("_order_source", "Android App"),
                    WooMetaData("_customer_name", address.name),
                    WooMetaData("_delivery_tracking_enabled", "true")
                )
                
                Log.d(TAG, "Creating WooCommerce order...")
                
                // Place order
                val response = withContext(Dispatchers.IO) {
                    repo.placeOrder(
                        billing = billing,
                        shipping = shipping,
                        lineItems = lineItems,
                        paymentMethod = gatewayId,
                        paymentTitle = paymentTitle,
                        setPaid = gatewayId != "cod",
                        customerNote = "Order placed via Natraj Super App",
                        metaData = metaData
                    )
                }

                progressBar.visibility = View.GONE
                placeOrderButton.isEnabled = true
                
                Log.d(TAG, "✓ Order created successfully!")
                Log.d(TAG, "Order ID: ${response.id}")
                Log.d(TAG, "Order Number: ${response.number}")
                Log.d(TAG, "Order Status: ${response.status}")
                Log.d(TAG, "Order Total: ${response.total}")
                
                // Clear cart
                CartManager.clear()
                
                // Navigate to confirmation
                val intent = Intent(this@PaymentActivity, OrderConfirmationActivity::class.java)
                intent.putExtra("order_id", response.number)
                intent.putExtra("order_woo_id", response.id)
                intent.putExtra("order_status", response.status)
                intent.putExtra("order_total", response.total)
                intent.putExtra("offline_mode", false)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
                
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                placeOrderButton.isEnabled = true
                
                Log.e(TAG, "✗ Order placement failed", e)
                Log.e(TAG, "Error: ${e.message}")
                e.printStackTrace()
                
                // Show error and fallback
                CustomToast.showError(
                    this@PaymentActivity,
                    "Failed to place order: ${e.message}",
                    Toast.LENGTH_LONG
                )
                
                placeOfflineOrder(cartItems, gatewayId, paymentTitle)
            }
        }
    }
    
    private fun placeOfflineOrder(cartItems: List<CartItem>, gatewayId: String, paymentTitle: String) {
        Log.d(TAG, "Placing offline order...")
        
        val order = OrderManager.placeOrder(cartItems, address, paymentTitle)
        CartManager.clear()
        
        Log.d(TAG, "✓ Offline order created: ${order.id}")
        
        val intent = Intent(this, OrderConfirmationActivity::class.java)
        intent.putExtra("order_id", order.id)
        intent.putExtra("offline_mode", true)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}
