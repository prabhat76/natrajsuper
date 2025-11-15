package com.example.natraj

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class OrderFailureActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_failure)
        
        val failureMessage = findViewById<TextView>(R.id.failure_message)
        val failureDetails = findViewById<TextView>(R.id.failure_details)
        val retryButton = findViewById<Button>(R.id.retry_order_button)
        val viewCartButton = findViewById<Button>(R.id.view_cart_button)
        val goHomeButton = findViewById<Button>(R.id.go_home_button_failure)
        
        // Get failure details from intent
        val message = intent.getStringExtra("failure_message") 
            ?: "We couldn't process your order. Please try again."
        val details = intent.getStringExtra("failure_details") 
            ?: "There was a problem connecting to the server."
        val showRetry = intent.getBooleanExtra("show_retry", true)
        
        failureMessage.text = message
        failureDetails.text = details
        
        if (!showRetry) {
            retryButton.visibility = android.view.View.GONE
        }
        
        retryButton.setOnClickListener {
            finish()
        }
        
        viewCartButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("navigate_to", "cart")
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
        
        goHomeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
    
    companion object {
        fun show(
            context: Context,
            message: String,
            details: String,
            showRetry: Boolean = true
        ) {
            val intent = Intent(context, OrderFailureActivity::class.java)
            intent.putExtra("failure_message", message)
            intent.putExtra("failure_details", details)
            intent.putExtra("show_retry", showRetry)
            context.startActivity(intent)
        }
    }
}
