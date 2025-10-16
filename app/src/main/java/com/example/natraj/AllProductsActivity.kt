package com.example.natraj

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

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
        val allProducts = ProductManager.getAllProducts()
        
        android.util.Log.d("AllProductsActivity", "Products count: ${allProducts.size}")
        
        if (allProducts.isEmpty()) {
            Toast.makeText(this, "No products found! Check products.json", Toast.LENGTH_LONG).show()
            android.util.Log.e("AllProductsActivity", "ProductManager returned empty list!")
            return
        }
        
        // Use responsive grid layout based on screen size
        val spanCount = try {
            resources.getInteger(R.integer.product_grid_columns)
        } catch (e: Exception) {
            2 // Default to 2 columns
        }
        
        productsRecycler.layoutManager = GridLayoutManager(this, spanCount)
        
        productsRecycler.adapter = GridProductAdapter(
            allProducts,
            onProductClick = { product ->
                val intent = Intent(this, ProductDetailActivity::class.java)
                intent.putExtra("product", product)
                startActivity(intent)
            }
        )

        Toast.makeText(this, "Showing ${allProducts.size} products", Toast.LENGTH_SHORT).show()
        android.util.Log.d("AllProductsActivity", "Grid adapter set successfully")
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