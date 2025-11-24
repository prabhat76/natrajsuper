package com.example.natraj

import android.content.Context
import com.example.natraj.data.WooRepository
import com.example.natraj.data.woo.WooPrefs
import kotlinx.coroutines.runBlocking

object ProductManager {
    private var allProducts: List<Product> = emptyList()
    private var isInitialized = false

    fun initialize(context: Context) {
        if (!isInitialized) {
            loadProducts(context)
            isInitialized = true
        }
    }

    private fun loadProducts(context: Context) {
        try {
            val prefs = WooPrefs(context)
            if (prefs.baseUrl.isNullOrBlank()) {
                android.util.Log.w("ProductManager", "WordPress not configured, using fallback")
                loadFromJsonFallback(context)
                return
            }

            // Try to fetch from WooCommerce API first
            runBlocking {
                val repo = WooRepository(context)
                val products = repo.getProducts(com.example.natraj.data.woo.FilterParams(perPage = 100))
                if (products.isNotEmpty()) {
                    allProducts = products
                    android.util.Log.d("ProductManager", "Loaded ${products.size} products from WooCommerce API")
                } else {
                    android.util.Log.w("ProductManager", "No products from WooCommerce, using fallback")
                    loadFromJsonFallback(context)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ProductManager", "Error loading products from WooCommerce", e)
            loadFromJsonFallback(context)
        }
    }

    private fun loadFromJsonFallback(context: Context) {
        try {
            val jsonString = loadJSONFromAsset(context, "products.json")
            android.util.Log.d("ProductManager", "JSON loaded, length: ${jsonString.length}")

            val jsonObject = com.google.gson.Gson().fromJson(jsonString, com.google.gson.JsonObject::class.java)
            val productsArray = jsonObject.getAsJsonArray("products")
            val type = com.google.gson.reflect.TypeToken.getParameterized(java.util.List::class.java, Product::class.java).type
            allProducts = com.google.gson.Gson().fromJson(productsArray, type)

            android.util.Log.d("ProductManager", "Loaded ${allProducts.size} products from JSON fallback")
            android.util.Log.d("ProductManager", "Featured products: ${allProducts.filter { it.isFeatured }.size}")

        } catch (e: Exception) {
            android.util.Log.e("ProductManager", "Error loading products from JSON", e)
            allProducts = emptyList()
        }
    }

    private fun loadJSONFromAsset(context: Context, fileName: String): String {
        return try {
            val inputStream = context.assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)
        } catch (ex: java.io.IOException) {
            ex.printStackTrace()
            ""
        }
    }

    fun getAllProducts(): List<Product> = allProducts

    fun getFeaturedProducts(): List<Product> = allProducts.filter { it.isFeatured }

    fun getProductsByCategory(category: String): List<Product> = 
        allProducts.filter { it.category.equals(category, ignoreCase = true) }

    fun getProductById(id: Int): Product? = allProducts.find { it.id == id }

    fun getCategories(): List<String> = allProducts.map { it.category }.distinct().sorted()

    fun searchProducts(query: String): List<Product> {
        val lowerQuery = query.lowercase()
        return allProducts.filter { product ->
            product.name.lowercase().contains(lowerQuery) ||
            product.description.lowercase().contains(lowerQuery) ||
            product.category.lowercase().contains(lowerQuery) ||
            (product.articleCode?.lowercase()?.contains(lowerQuery) == true)
        }
    }
}
