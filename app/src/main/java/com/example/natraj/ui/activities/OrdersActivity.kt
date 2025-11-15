package com.example.natraj

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.natraj.data.WooRepository
import com.example.natraj.data.woo.WooPrefs
import com.example.natraj.ui.activities.ErrorActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OrdersActivity : AppCompatActivity() {

    private val TAG = "OrdersActivity"
    private var isFirstLoad = true
    private lateinit var backButton: ImageView
    private lateinit var ordersRecycler: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var emptyView: LinearLayout
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders)

        initializeViews()
        
        // Check login status after views are initialized
        if (!AuthManager.isLoggedIn()) {
            // Show login prompt and close activity
            showLoginPromptAndExit()
            return
        }
        
        setupListeners()
        loadOrders()
    }
    
    private fun showLoginPromptAndExit() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Login Required")
            .setMessage("Please login to view your orders")
            .setPositiveButton("Login") { _, _ ->
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.orders_back_button)
        ordersRecycler = findViewById(R.id.orders_recycler)
        swipeRefresh = findViewById(R.id.orders_swipe_refresh)
        emptyView = findViewById(R.id.orders_empty_view)
        progressBar = findViewById<ProgressBar>(R.id.orders_progress_bar).apply {
            visibility = View.VISIBLE
        }
    }

    private fun setupListeners() {
        backButton.setOnClickListener {
            finish()
        }
        
        // Setup home button from empty view
        findViewById<Button>(R.id.go_home_button_orders)?.setOnClickListener {
            finish() // Go back to home
        }
        
        swipeRefresh.setOnRefreshListener {
            Log.d(TAG, "Swipe to refresh triggered")
            loadOrders()
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh orders when activity resumes (but not on first load since onCreate already loads)
        if (!isFirstLoad && AuthManager.isLoggedIn()) {
            Log.d(TAG, "Activity resumed, refreshing orders...")
            // Force refresh with a small delay to ensure backend has processed the order
            Handler(Looper.getMainLooper()).postDelayed({
                loadOrders()
            }, 500)
        }
        isFirstLoad = false
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
        
        // Get customer ID if available
        val customerId = AuthManager.getCustomerId()
        val customerEmail = AuthManager.getUserEmail()
        Log.d(TAG, "Customer ID: $customerId, Email: $customerEmail")
        Log.d(TAG, "Is logged in: ${AuthManager.isLoggedIn()}")
        
        lifecycleScope.launch {
            try {
                val repo = WooRepository(this@OrdersActivity)
                val wooOrders = withContext(Dispatchers.IO) {
                    // Use server-side filtering for better performance - only fetch customer's orders
                    val customerIdParam = if (customerId > 0) customerId else null
                    val orders = repo.getOrders(
                        perPage = 50,  // Reduced from 100 for faster loading
                        page = 1,
                        customerId = customerIdParam  // Server-side filtering
                    )
                    Log.d(TAG, "Orders fetched from API (server-filtered): ${orders.size}")
                    
                    // Log orders for debugging
                    orders.forEachIndexed { index, order ->
                        Log.d(TAG, "Order #${index + 1}: id=${order.id}, number=${order.number}, customer_id=${order.customer_id}, status=${order.status}, total=${order.total}")
                    }
                    
                    orders
                }
                
                progressBar.visibility = View.GONE
                swipeRefresh.isRefreshing = false
                
                Log.d(TAG, "✓ WooCommerce orders fetched: ${wooOrders.size}")
                
                if (wooOrders.isEmpty()) {
                    ordersRecycler.visibility = View.GONE
                    emptyView.visibility = View.VISIBLE
                } else {
                    ordersRecycler.visibility = View.VISIBLE
                    emptyView.visibility = View.GONE
                    
                    // Use WooOrderAdapter for WordPress orders
                    ordersRecycler.layoutManager = LinearLayoutManager(this@OrdersActivity)
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
                        "Showing ${wooOrders.size} order(s) from WordPress site", 
                        Toast.LENGTH_SHORT).show()
                }
                
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                swipeRefresh.isRefreshing = false
                Log.e(TAG, "✗ Failed to fetch WooCommerce orders", e)
                
                // Show error screen if no local orders
                val localOrders = OrderManager.getOrders()
                if (localOrders.isEmpty()) {
                    ErrorActivity.showFromException(
                        this@OrdersActivity,
                        e,
                        showRetry = true
                    )
                } else {
                    Toast.makeText(this@OrdersActivity, 
                        "Showing local orders. Unable to connect to server.", 
                        Toast.LENGTH_LONG).show()
                    showLocalOrdersWithMessage()
                }
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
