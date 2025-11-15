package com.example.natraj.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.natraj.CartManager
import com.example.natraj.GridProductAdapter
import com.example.natraj.Product
import com.example.natraj.ProductDetailActivity
import com.example.natraj.R
import com.example.natraj.WishlistManager
import com.example.natraj.data.WooRepository
import com.example.natraj.data.woo.WooPrefs
import com.example.natraj.util.CustomToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WishlistActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var wishlistRecycler: RecyclerView
    private lateinit var emptyView: LinearLayout
    private lateinit var emptyText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var browseButton: android.widget.Button
    private lateinit var adapter: GridProductAdapter
    
    private val wishlistProducts = mutableListOf<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wishlist)

        initializeViews()
        setupListeners()
        loadWishlistProducts()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.wishlist_back_button)
        wishlistRecycler = findViewById(R.id.wishlist_recycler)
        emptyView = findViewById(R.id.wishlist_empty_view)
        emptyText = findViewById(R.id.wishlist_empty_text)
        progressBar = findViewById(R.id.wishlist_progress_bar)
        browseButton = findViewById(R.id.browse_products_button)

        // Setup RecyclerView
        wishlistRecycler.layoutManager = GridLayoutManager(this, 2)
    }

    private fun setupListeners() {
        backButton.setOnClickListener { finish() }
        
        browseButton.setOnClickListener {
            finish() // Go back to browse products
        }
        
        // Listen to wishlist changes
        WishlistManager.registerListener {
            loadWishlistProducts()
        }
    }

    private fun loadWishlistProducts() {
        val wishlistIds = WishlistManager.getWishlistIds()
        
        if (wishlistIds.isEmpty()) {
            showEmptyView()
            return
        }

        progressBar.visibility = View.VISIBLE
        
        val prefs = WooPrefs(this)
        val hasWooConfig = !prefs.baseUrl.isNullOrBlank() && 
                          !prefs.consumerKey.isNullOrBlank() && 
                          !prefs.consumerSecret.isNullOrBlank()

        if (!hasWooConfig) {
            progressBar.visibility = View.GONE
            CustomToast.showError(this, "WooCommerce not configured")
            return
        }

        lifecycleScope.launch {
            try {
                val repo = WooRepository(this@WishlistActivity)
                val allProducts = withContext(Dispatchers.IO) {
                    repo.getProducts(params = com.example.natraj.data.woo.FilterParams(perPage = 100))
                }
                
                // Filter products that are in wishlist
                wishlistProducts.clear()
                wishlistProducts.addAll(
                    allProducts.filter { product ->
                        wishlistIds.contains(product.id)
                    }
                )
                
                progressBar.visibility = View.GONE
                
                if (wishlistProducts.isEmpty()) {
                    showEmptyView()
                } else {
                    showWishlistProducts()
                }
                
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                CustomToast.showError(this@WishlistActivity, "Failed to load wishlist: ${e.message}")
            }
        }
    }

    private fun showEmptyView() {
        wishlistRecycler.visibility = View.GONE
        emptyView.visibility = View.VISIBLE
    }

    private fun showWishlistProducts() {
        emptyView.visibility = View.GONE
        wishlistRecycler.visibility = View.VISIBLE
        
        adapter = GridProductAdapter(
            products = wishlistProducts,
            onProductClick = { product ->
                val intent = Intent(this, ProductDetailActivity::class.java)
                intent.putExtra("product", product)
                startActivity(intent)
            },
            onFavoriteClick = { product ->
                WishlistManager.toggle(product.id)
                loadWishlistProducts()
            },
            onAddToCart = { product ->
                CartManager.add(product, 1)
                CustomToast.showSuccess(this, "${product.name} added to cart")
            }
        )
        wishlistRecycler.adapter = adapter
    }

    override fun onDestroy() {
        super.onDestroy()
        WishlistManager.unregisterListener {}
    }
}
