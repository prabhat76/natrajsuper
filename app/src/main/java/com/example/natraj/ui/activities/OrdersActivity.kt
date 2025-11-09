package com.example.natraj

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.natraj.data.WooRepository
import com.example.natraj.data.woo.WooPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OrdersActivity : AppCompatActivity() {

    private val TAG = "OrdersActivity"
    private lateinit var backButton: ImageView
    private lateinit var ordersRecycler: RecyclerView
    private lateinit var emptyView: LinearLayout
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders)

        initializeViews()
        setupListeners()
        loadOrders()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.orders_back_button)
        ordersRecycler = findViewById(R.id.orders_recycler)
        emptyView = findViewById(R.id.orders_empty_view)
        progressBar = findViewById<ProgressBar>(R.id.orders_progress_bar).apply {
            visibility = View.VISIBLE
        }
    }

    private fun setupListeners() {
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun loadOrders() {
        Log.d(TAG, "=== Loading Orders ===")
        
        val prefs = WooPrefs(this)
        val canUseWoo = !prefs.baseUrl.isNullOrBlank() && 
                       !prefs.consumerKey.isNullOrBlank() && 
                       !prefs.consumerSecret.isNullOrBlank()

        Log.d(TAG, "WooCommerce configured: $canUseWoo")

        if (canUseWoo) {
            loadWooCommerceOrders()
        } else {
            loadLocalOrders()
        }
    }

    private fun loadWooCommerceOrders() {
        Log.d(TAG, "Fetching orders from WooCommerce...")
        
        lifecycleScope.launch {
            try {
                val repo = WooRepository(this@OrdersActivity)
                val wooOrders = withContext(Dispatchers.IO) {
                    // Fetch all orders from WooCommerce
                    repo.getOrders(perPage = 50, page = 1)
                }
                
                progressBar.visibility = View.GONE
                
                Log.d(TAG, "✓ WooCommerce orders fetched: ${wooOrders.size}")
                
                if (wooOrders.isEmpty()) {
                    ordersRecycler.visibility = View.GONE
                    emptyView.visibility = View.VISIBLE
                } else {
                    ordersRecycler.visibility = View.VISIBLE
                    emptyView.visibility = View.GONE
                    
                    // Use WooOrderAdapter for WordPress orders
                    ordersRecycler.adapter = WooOrderAdapter(wooOrders) { order ->
                        Log.d(TAG, "WooCommerce order clicked: ${order.id}")
                        
                        // Navigate to order details
                        val intent = Intent(this@OrdersActivity, OrderConfirmationActivity::class.java)
                        intent.putExtra("order_id", order.number ?: order.id.toString())
                        intent.putExtra("order_woo_id", order.id)
                        intent.putExtra("order_status", order.status)
                        intent.putExtra("order_total", order.total)
                        intent.putExtra("offline_mode", false)
                        startActivity(intent)
                    }
                    
                    Toast.makeText(this@OrdersActivity, 
                        "${wooOrders.size} order(s) from WordPress", 
                        Toast.LENGTH_SHORT).show()
                }
                
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Log.e(TAG, "✗ Failed to fetch WooCommerce orders", e)
                Log.e(TAG, "Error: ${e.message}")
                
                Toast.makeText(this@OrdersActivity, 
                    "Failed to load orders from WordPress: ${e.message}", 
                    Toast.LENGTH_LONG).show()
                
                // Fallback to local orders
                showLocalOrdersWithMessage()
            }
        }
    }

    private fun loadLocalOrders() {
        Log.d(TAG, "Loading local orders...")
        progressBar.visibility = View.GONE
        
        val orders = OrderManager.getOrders()
        Log.d(TAG, "Local orders count: ${orders.size}")

        if (orders.isEmpty()) {
            ordersRecycler.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        } else {
            ordersRecycler.visibility = View.VISIBLE
            emptyView.visibility = View.GONE

            ordersRecycler.layoutManager = LinearLayoutManager(this)
            ordersRecycler.adapter = OrderAdapter(orders) { order ->
                // TODO: Navigate to order details
                Log.d(TAG, "Order clicked: ${order.id}")
            }
        }
    }

    private fun showLocalOrdersWithMessage() {
        val orders = OrderManager.getOrders()
        
        if (orders.isEmpty()) {
            ordersRecycler.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        } else {
            ordersRecycler.visibility = View.VISIBLE
            emptyView.visibility = View.GONE

            ordersRecycler.layoutManager = LinearLayoutManager(this)
            ordersRecycler.adapter = OrderAdapter(orders) { order ->
                Log.d(TAG, "Order clicked: ${order.id}")
            }
        }
    }
}
