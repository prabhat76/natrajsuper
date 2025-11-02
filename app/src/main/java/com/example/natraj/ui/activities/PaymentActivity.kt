package com.example.natraj

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.natraj.data.WooRepository
import com.example.natraj.data.woo.WooBilling
import com.example.natraj.data.woo.WooOrderLineItem
import com.example.natraj.data.woo.WooPrefs
import com.example.natraj.data.woo.WooShipping
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PaymentActivity : AppCompatActivity() {

    private lateinit var address: Address

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        address = intent.getSerializableExtra("address") as? Address ?: run {
            finish()
            return
        }

        val backButton = findViewById<ImageView>(R.id.payment_back_button)
        val addressText = findViewById<TextView>(R.id.payment_address_text)
        val itemsCount = findViewById<TextView>(R.id.payment_items_count)
        val subtotal = findViewById<TextView>(R.id.payment_subtotal)
        val discount = findViewById<TextView>(R.id.payment_discount)
        val delivery = findViewById<TextView>(R.id.payment_delivery)
        val total = findViewById<TextView>(R.id.payment_total)
        val paymentGroup = findViewById<RadioGroup>(R.id.payment_method_group)
        val placeOrderButton = findViewById<Button>(R.id.payment_place_order_btn)

        backButton.setOnClickListener {
            finish()
        }

        // Display address
        addressText.text = "${address.name}, ${address.mobile}\n${address.address}, ${address.locality}\n${address.city}, ${address.state} - ${address.pincode}"

        // Calculate amounts
        val cartItems = CartManager.getItems()
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

        placeOrderButton.setOnClickListener {
            val selectedPaymentId = paymentGroup.checkedRadioButtonId
            val paymentMethod = when (selectedPaymentId) {
                R.id.payment_cash -> "Cash on Delivery"
                R.id.payment_upi -> "UPI"
                R.id.payment_card -> "Card"
                else -> "Cash on Delivery"
            }

            val prefs = WooPrefs(this)
            val canUseWoo = !prefs.baseUrl.isNullOrBlank() && !prefs.consumerKey.isNullOrBlank() && !prefs.consumerSecret.isNullOrBlank()

            if (canUseWoo) {
                // Try placing order via WooCommerce
                lifecycleScope.launch {
                    try {
                        val repo = WooRepository(this@PaymentActivity)
                        val lineItems = cartItems.map { WooOrderLineItem(product_id = it.product.id, quantity = it.quantity) }
                        val billing = WooBilling(
                            first_name = address.name.substringBefore(" "),
                            last_name = address.name.substringAfter(" ", ""),
                            address_1 = address.address,
                            address_2 = address.locality,
                            city = address.city,
                            state = address.state,
                            postcode = address.pincode,
                            email = "customer@example.com",
                            phone = address.mobile
                        )
                        val shipping = WooShipping(
                            first_name = billing.first_name,
                            last_name = billing.last_name,
                            address_1 = billing.address_1,
                            address_2 = billing.address_2,
                            city = billing.city,
                            state = billing.state,
                            postcode = billing.postcode
                        )

                        val method = when (paymentMethod) {
                            "UPI" -> "bacs" // Map to a generic method if custom gateway not available
                            "Card" -> "bacs"
                            else -> "cod"
                        }

                        val resp = withContext(Dispatchers.IO) {
                            repo.placeOrder(billing, shipping, lineItems, paymentMethod = method, paymentTitle = paymentMethod, setPaid = method != "cod")
                        }

                        // Success: clear cart and navigate
                        CartManager.clear()
                        val intent = Intent(this@PaymentActivity, OrderConfirmationActivity::class.java)
                        intent.putExtra("order_id", resp.number)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    } catch (e: Exception) {
                        // Fallback to local order manager on failure
                        Toast.makeText(this@PaymentActivity, "Online order failed, placing offline", Toast.LENGTH_SHORT).show()
                        val order = OrderManager.placeOrder(cartItems, address, paymentMethod)
                        CartManager.clear()
                        val intent = Intent(this@PaymentActivity, OrderConfirmationActivity::class.java)
                        intent.putExtra("order_id", order.id)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    }
                }
            } else {
                // Offline/local order flow
                val order = OrderManager.placeOrder(cartItems, address, paymentMethod)
                CartManager.clear()
                val intent = Intent(this, OrderConfirmationActivity::class.java)
                intent.putExtra("order_id", order.id)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        }
    }
}
