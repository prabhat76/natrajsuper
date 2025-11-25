package com.example.natraj

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.natraj.data.WooRepository
import com.example.natraj.data.woo.WooPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class APIDebugActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_api_debug)
        
        val debugText = findViewById<TextView>(R.id.debug_text)
        
        val prefs = WooPrefs(this)
        
        // Set the new consumer key and secret for read/write access
        prefs.consumerKey = "ck_4f13aeb31551791a4609bc8a6ba6d0b1df7ac364"
        prefs.consumerSecret = "cs_f959e8710abbc9c8343781916c73cfb5cdf9243a"
        
        val baseUrl = prefs.baseUrl
        val ck = prefs.consumerKey
        val cs = prefs.consumerSecret
        
        var output = "=== API DEBUG ===\n\n"
        output += "Base URL: ${baseUrl ?: "NOT SET"}\n"
        output += "Consumer Key: ${if (!ck.isNullOrBlank()) ck.take(10) + "..." else "NOT SET"}\n"
        output += "Consumer Secret: ${if (!cs.isNullOrBlank()) cs.take(10) + "..." else "NOT SET"}\n\n"
        
        debugText.text = output + "Fetching data...\n"
        
        lifecycleScope.launch {
            try {
                if (baseUrl.isNullOrBlank() || ck.isNullOrBlank() || cs.isNullOrBlank()) {
                    output += "ERROR: Credentials not configured!\n"
                    debugText.text = output
                    return@launch
                }
                
                val repo = WooRepository(this@APIDebugActivity)
                output += "Attempting to fetch categories...\n"
                debugText.text = output
                
                val categories = withContext(Dispatchers.IO) {
                    repo.getCategories()
                }
                
                output += "Categories fetched: ${categories.size}\n"
                for (cat in categories.take(3)) {
                    output += "  - ${cat.name}\n"
                }
                
                output += "\nAttempting to fetch featured products...\n"
                debugText.text = output
                
                val products = withContext(Dispatchers.IO) {
                    repo.getProducts(com.example.natraj.data.woo.FilterParams(perPage = 5), featured = true) // Keep small for debugging
                }
                
                output += "Products fetched: ${products.size}\n"
                for (prod in products.take(3)) {
                    output += "  - ${prod.name} (${prod.price})\n"
                }
                
                // Test Customer Creation (Register API)
                output += "\n=== TESTING POST APIS ===\n"
                output += "\n[1] Testing Customer Creation (Register)...\n"
                debugText.text = output
                
                try {
                    val testCustomer = withContext(Dispatchers.IO) {
                        repo.createCustomer(
                            email = "test_${System.currentTimeMillis()}@example.com",
                            firstName = "Test",
                            lastName = "User",
                            username = "testuser_${System.currentTimeMillis()}",
                            password = "Test@123"
                        )
                    }
                    output += "✓ Customer Created Successfully!\n"
                    output += "  Customer ID: ${testCustomer.id}\n"
                    output += "  Email: ${testCustomer.email}\n"
                    output += "  Name: ${testCustomer.first_name} ${testCustomer.last_name}\n"
                } catch (e: Exception) {
                    output += "✗ Customer Creation Failed\n"
                    output += "  Error: ${e.message}\n"
                }
                debugText.text = output
                
                // Test Order Creation (Order API)
                output += "\n[2] Testing Order Creation...\n"
                debugText.text = output
                
                try {
                    // Get first product for test order
                    val testProduct = products.firstOrNull()
                    if (testProduct != null) {
                        val testOrder = withContext(Dispatchers.IO) {
                            repo.placeOrder(
                                billing = com.example.natraj.data.woo.WooBilling(
                                    first_name = "Test",
                                    last_name = "Order",
                                    address_1 = "123 Test Street",
                                    city = "Test City",
                                    state = "Test State",
                                    postcode = "123456",
                                    country = "IN",
                                    email = "testorder@example.com",
                                    phone = "9876543210"
                                ),
                                shipping = com.example.natraj.data.woo.WooShipping(
                                    first_name = "Test",
                                    last_name = "Order",
                                    address_1 = "123 Test Street",
                                    city = "Test City",
                                    state = "Test State",
                                    postcode = "123456",
                                    country = "IN"
                                ),
                                lineItems = listOf(
                                    com.example.natraj.data.woo.WooOrderLineItem(
                                        product_id = testProduct.id,
                                        quantity = 1
                                    )
                                ),
                                paymentMethod = "cod",
                                paymentTitle = "Cash on Delivery",
                                setPaid = false,
                                customerId = 0,
                                customerNote = "API Test Order"
                            )
                        }
                        output += "✓ Order Created Successfully!\n"
                        output += "  Order ID: ${testOrder.id}\n"
                        output += "  Order Number: ${testOrder.number}\n"
                        output += "  Status: ${testOrder.status}\n"
                        output += "  Total: ₹${testOrder.total}\n"
                    } else {
                        output += "✗ No products available for test order\n"
                    }
                } catch (e: Exception) {
                    output += "✗ Order Creation Failed\n"
                    output += "  Error: ${e.message}\n"
                }
                debugText.text = output
                
                output += "\n=== ALL TESTS COMPLETED ===\n"
                output += "READ APIs: ✓ Working\n"
                output += "Check POST results above\n"
                debugText.text = output
                
            } catch (e: Exception) {
                output += "ERROR: ${e.javaClass.simpleName}\n"
                output += "Message: ${e.message}\n"
                output += "Cause: ${e.cause?.message}\n\n"
                output += "Stacktrace:\n"
                output += e.stackTrace.take(5).joinToString("\n") { it.toString() }
                debugText.text = output
                android.util.Log.e("APIDebug", "Full stacktrace", e)
            }
        }
    }
}
