package com.example.natraj

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.natraj.data.woo.WooPrefs
import com.example.natraj.data.AppConfig
import com.example.natraj.data.WooRepository
import com.example.natraj.data.woo.FilterParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CatalogueActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var productsRecycler: RecyclerView
    private lateinit var adapter: GridProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalogue)

        initializeViews()
        setupToolbar()
        setupProductsGrid()
        loadProducts()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        productsRecycler = findViewById(R.id.products_recycler)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Product Catalogue"
        }
    }

    private fun setupProductsGrid() {
        val spanCount = try { resources.getInteger(R.integer.product_grid_columns) } catch (_: Exception) { 2 }
        productsRecycler.layoutManager = GridLayoutManager(this, spanCount)

        adapter = GridProductAdapter(mutableListOf()) { product ->
            val intent = Intent(this@CatalogueActivity, ProductDetailActivity::class.java)
            intent.putExtra("product", product)
            startActivity(intent)
        }
        productsRecycler.adapter = adapter
    }

    private fun loadProducts() {
        val prefs = WooPrefs(this)
        val canUseWoo = !prefs.baseUrl.isNullOrBlank() && !prefs.consumerKey.isNullOrBlank() && !prefs.consumerSecret.isNullOrBlank()

        if (canUseWoo) {
            lifecycleScope.launch {
                try {
                    val repo = WooRepository(this@CatalogueActivity)
                    val products = withContext(Dispatchers.IO) {
                        repo.getProducts(FilterParams(perPage = AppConfig.getProductsPerPage(this@CatalogueActivity))) // Dynamic perPage for catalogue
                    }

                    if (products.isNotEmpty()) {
                        adapter.update(products)
                        Toast.makeText(this@CatalogueActivity, "Loaded ${products.size} products", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@CatalogueActivity, "No products found", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("CatalogueActivity", "Failed to load products: ${e.message}", e)
                    Toast.makeText(this@CatalogueActivity, "Failed to load products", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Configure WordPress settings to load products", Toast.LENGTH_LONG).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
