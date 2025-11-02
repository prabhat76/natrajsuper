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
                    repo.getProducts(com.example.natraj.data.woo.FilterParams(perPage = 5), featured = true)
                }
                
                output += "Products fetched: ${products.size}\n"
                for (prod in products.take(3)) {
                    output += "  - ${prod.name} (${prod.price})\n"
                }
                
                output += "\nSUCCESS: API is working!\n"
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
