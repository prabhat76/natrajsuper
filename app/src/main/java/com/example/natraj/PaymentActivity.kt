package com.example.natraj

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

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

            // Place order
            val order = OrderManager.placeOrder(cartItems, address, paymentMethod)
            
            // Clear cart
            CartManager.clear()

            // Navigate to order confirmation
            val intent = Intent(this, OrderConfirmationActivity::class.java)
            intent.putExtra("order_id", order.id)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}
