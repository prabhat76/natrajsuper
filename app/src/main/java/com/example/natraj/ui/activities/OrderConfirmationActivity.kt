package com.example.natraj

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Locale

class OrderConfirmationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_confirmation)

        val orderId = intent.getStringExtra("order_id") ?: run {
            finish()
            return
        }

        val order = OrderManager.getOrderById(orderId) ?: run {
            finish()
            return
        }

        val orderIdText = findViewById<TextView>(R.id.confirmation_order_id)
        val dateText = findViewById<TextView>(R.id.confirmation_date)
        val addressText = findViewById<TextView>(R.id.confirmation_address)
        val paymentText = findViewById<TextView>(R.id.confirmation_payment)
        val amountText = findViewById<TextView>(R.id.confirmation_amount)
        val viewOrdersButton = findViewById<Button>(R.id.confirmation_view_orders_btn)
        val continueShoppingButton = findViewById<Button>(R.id.confirmation_continue_shopping_btn)

        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        
        orderIdText.text = "Order ID: #${order.id}"
        dateText.text = dateFormat.format(order.orderDate)
        addressText.text = "${order.deliveryAddress.address}, ${order.deliveryAddress.city}, ${order.deliveryAddress.state} - ${order.deliveryAddress.pincode}"
        paymentText.text = order.paymentMethod
        amountText.text = "₹${order.totalAmount.toInt()}"

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
}
