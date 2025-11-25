package com.example.natraj

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import com.example.natraj.data.AppConfig
import com.example.natraj.data.WooRepository
import com.example.natraj.data.woo.FilterParams
import com.example.natraj.data.woo.WooPrefs
import com.example.natraj.util.ThemeUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AllProductsActivity : AppCompatActivity() {

    private lateinit var productsRecycler: RecyclerView
    private lateinit var toolbar: Toolbar
    private lateinit var adapter: GridProductAdapter
    
    private var currentPage = 1
    private var isLoading = false
    private var hasMore = true
    private var perPage: Int = 0

    private var categoryId: Int? = null
    private var categoryName: String? = null
    
    private enum class SortMode { DEFAULT, PRICE_LOW_HIGH, PRICE_HIGH_LOW, NAME_AZ, NEWEST }
    private var currentSort = SortMode.DEFAULT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_products)

        categoryId = intent.getIntExtra("extra_category_id", 0).takeIf { it > 0 }
        categoryName = intent.getStringExtra("extra_category_name")
        
        perPage = AppConfig.getProductsPerPage(this)

        initializeViews()
        setupToolbar()
        setupProductsGrid()
        loadProducts()
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
            title = categoryName ?: "All Products"
        }
        ThemeUtil.applyToolbarColor(toolbar, this)
    }

    private fun setupProductsGrid() {
        val prefs = WooPrefs(this)
        val spanCount = try { resources.getInteger(R.integer.product_grid_columns) } catch (_: Exception) { 2 }
        productsRecycler.layoutManager = GridLayoutManager(this, spanCount)
        
        adapter = GridProductAdapter(mutableListOf()) { product ->
            val intent = Intent(this@AllProductsActivity, ProductDetailActivity::class.java)
            intent.putExtra("product", product)
            startActivity(intent)
        }
        productsRecycler.adapter = adapter
        
        // Endless scroll listener
        productsRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                
                if (!isLoading && hasMore && 
                    (visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5) {
                    loadNextPage()
                }
            }
        })
    }
    
    private fun loadProducts() {
        currentPage = 1
        hasMore = true
        lifecycleScope.launch {
            isLoading = true
            try {
                val repo = WooRepository(this@AllProductsActivity)
                var products = withContext(Dispatchers.IO) {
                    repo.getProducts(FilterParams(categoryId = categoryId, perPage = perPage, page = currentPage))
                }
                
                if (products.isEmpty()) {
                    Toast.makeText(this@AllProductsActivity, "No products found", Toast.LENGTH_SHORT).show()
                    hasMore = false
                    return@launch
                }
                
                products = applySorting(products)
                adapter.update(products)
                hasMore = products.size >= perPage
                
            } catch (e: Exception) {
                android.util.Log.e("AllProductsActivity", "Failed to load products: ${e.message}", e)
                Toast.makeText(this@AllProductsActivity, "Unable to load products", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }
    
    private fun loadNextPage() {
        if (isLoading || !hasMore) return
        
        currentPage++
        lifecycleScope.launch {
            isLoading = true
            try {
                val repo = WooRepository(this@AllProductsActivity)
                var products = withContext(Dispatchers.IO) {
                    repo.getProducts(FilterParams(categoryId = categoryId, perPage = perPage, page = currentPage))
                }
                
                if (products.isEmpty()) {
                    hasMore = false
                    return@launch
                }
                
                products = applySorting(products)
                adapter.append(products)
                hasMore = products.size >= perPage
                
            } catch (e: Exception) {
                android.util.Log.e("AllProductsActivity", "Failed to load more products: ${e.message}", e)
                currentPage--
            } finally {
                isLoading = false
            }
        }
    }
    
    private fun applySorting(products: List<Product>): List<Product> {
        return when (currentSort) {
            SortMode.PRICE_LOW_HIGH -> products.sortedBy { if (it.transferPrice > 0) it.transferPrice else it.price }
            SortMode.PRICE_HIGH_LOW -> products.sortedByDescending { if (it.transferPrice > 0) it.transferPrice else it.price }
            SortMode.NAME_AZ -> products.sortedBy { it.name }
            SortMode.NEWEST -> products.reversed()
            SortMode.DEFAULT -> products
        }
    }
    
    private fun showSortDialog() {
        val options = arrayOf("Default", "Price: Low to High", "Price: High to Low", "Name: A-Z", "Newest First")
        val currentSelection = currentSort.ordinal
        
        AlertDialog.Builder(this)
            .setTitle("Sort Products")
            .setSingleChoiceItems(options, currentSelection) { dialog, which ->
                currentSort = SortMode.values()[which]
                loadProducts()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_all_products, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_sort -> {
                showSortDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}