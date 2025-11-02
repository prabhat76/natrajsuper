package com.example.natraj

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import com.example.natraj.data.WooRepository
import com.example.natraj.data.woo.FilterParams
import com.example.natraj.data.woo.WooPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AllProductsActivity : AppCompatActivity() {

    private lateinit var productsRecycler: RecyclerView
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_products)

        initializeViews()
        setupToolbar()
        setupProductsGrid()
    }

    private fun initializeViews() {
        productsRecycler = findViewById(R.id.all_products_recycler)
        toolbar = findViewById(R.id.toolbar)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "All Products"
        }
    }

    private fun setupProductsGrid() {
        val prefs = WooPrefs(this)
        val canUseWoo = !prefs.baseUrl.isNullOrBlank() && !prefs.consumerKey.isNullOrBlank() && !prefs.consumerSecret.isNullOrBlank()

        // Responsive grid
        val spanCount = try { resources.getInteger(R.integer.product_grid_columns) } catch (_: Exception) { 2 }
        productsRecycler.layoutManager = GridLayoutManager(this, spanCount)

        val catId = intent.getIntExtra("extra_category_id", 0).takeIf { it > 0 }
        val catName = intent.getStringExtra("extra_category_name")
        supportActionBar?.title = catName ?: "All Products"

        if (canUseWoo) {
            lifecycleScope.launch {
                try {
                    val repo = WooRepository(this@AllProductsActivity)
                    val products = withContext(Dispatchers.IO) {
                        repo.getProducts(FilterParams(categoryId = catId, perPage = 40))
                    }
                    if (products.isEmpty()) {
                        Toast.makeText(this@AllProductsActivity, "No products found", Toast.LENGTH_LONG).show()
                        return@launch
                    }
                    productsRecycler.adapter = GridProductAdapter(products) { product ->
                        val intent = Intent(this@AllProductsActivity, ProductDetailActivity::class.java)
                        intent.putExtra("product", product)
                        startActivity(intent)
                    }
                    Toast.makeText(this@AllProductsActivity, "Showing ${products.size} products", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    android.util.Log.e("AllProductsActivity", "Woo fetch failed, fallback", e)
                    loadFromAssetsFallback()
                }
            }
        } else {
            loadFromAssetsFallback()
        }
    }

    private fun loadFromAssetsFallback() {
        val catName = intent.getStringExtra("extra_category_name")
        val products = if (catName.isNullOrBlank()) ProductManager.getAllProducts() else ProductManager.getProductsByCategory(catName)
        if (products.isEmpty()) {
            Toast.makeText(this, "No products found!", Toast.LENGTH_LONG).show()
            return
        }
        productsRecycler.adapter = GridProductAdapter(products) { product ->
            val intent = Intent(this, ProductDetailActivity::class.java)
            intent.putExtra("product", product)
            startActivity(intent)
        }
        Toast.makeText(this, "Showing ${products.size} products", Toast.LENGTH_SHORT).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        finish()
        super.onBackPressed()
    }
}