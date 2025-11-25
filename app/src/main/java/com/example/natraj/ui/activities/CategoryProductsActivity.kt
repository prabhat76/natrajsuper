package com.example.natraj.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.natraj.*
import com.example.natraj.data.AppConfig
import com.example.natraj.data.WooRepository
import com.example.natraj.data.woo.FilterParams
import com.example.natraj.data.woo.WooPrefs
import com.example.natraj.util.CustomToast
import com.example.natraj.util.ThemeUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoryProductsActivity : AppCompatActivity() {
    
    private lateinit var productsRecycler: RecyclerView
    private lateinit var toolbar: Toolbar
    private lateinit var adapter: GridProductAdapter
    
    private var categoryId: Int = 0
    private var categoryName: String = ""
    
    // Pagination
    private var currentPage = 1
    private var isLoading = false
    private var hasMore = true
    private val perPage = AppConfig.getProductsPerPage(this)

    // Sorting
    private enum class SortMode { DEFAULT, PRICE_LOW_HIGH, PRICE_HIGH_LOW, NAME_AZ, NEWEST }
    private var currentSort = SortMode.DEFAULT
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_products)
        
        categoryId = intent.getIntExtra("category_id", 0)
        categoryName = intent.getStringExtra("category_name") ?: "Products"
        
        if (categoryId == 0) {
            CustomToast.showError(this, "Invalid category")
            finish()
            return
        }
        
        initViews()
        setupToolbar()
        setupRecyclerView()
        loadProducts(reset = true)
    }
    
    private fun initViews() {
        productsRecycler = findViewById(R.id.category_products_recycler)
        toolbar = findViewById(R.id.toolbar)
    }
    
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = categoryName
        }
        // Apply dynamic theme color
        com.example.natraj.util.ThemeUtil.applyToolbarColor(toolbar, this)
    }
    
    private fun setupRecyclerView() {
        val spanCount = try { resources.getInteger(R.integer.product_grid_columns) } catch (_: Exception) { 2 }
        productsRecycler.layoutManager = GridLayoutManager(this, spanCount)
        adapter = GridProductAdapter(
            onProductClick = { product ->
                val intent = Intent(this, ProductDetailActivity::class.java)
                intent.putExtra("product", product)
                startActivity(intent)
            }
        )
        productsRecycler.adapter = adapter

        // Endless scroll
        productsRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lm = recyclerView.layoutManager as GridLayoutManager
                val visible = lm.childCount
                val total = lm.itemCount
                val firstPos = lm.findFirstVisibleItemPosition()
                if (!isLoading && hasMore && (visible + firstPos) >= total - 5) {
                    loadNextPage()
                }
            }
        })
    }
    
    private fun loadProducts(reset: Boolean = false) {
        val prefs = WooPrefs(this)
        if (prefs.baseUrl.isNullOrBlank() || prefs.consumerKey.isNullOrBlank()) {
            CustomToast.showError(this, "WordPress configuration missing")
            return
        }
        if (reset) {
            currentPage = 1
            hasMore = true
        }

        lifecycleScope.launch {
            try {
                val repo = WooRepository(this@CategoryProductsActivity)
                var products = withContext(Dispatchers.IO) {
                    repo.getProducts(FilterParams(categoryId = categoryId, perPage = perPage, page = currentPage))
                }
                
                if (products.isEmpty()) {
                    CustomToast.showError(this@CategoryProductsActivity, "No products found in this category")
                    hasMore = false
                } else {
                    products = applySorting(products)
                    adapter.update(products)
                    hasMore = products.size >= perPage
                }
            } catch (e: Exception) {
                android.util.Log.e("CategoryProducts", "Failed to load products", e)
                CustomToast.showError(this@CategoryProductsActivity, "Failed to load products")
            }
        }
    }

    private fun loadNextPage() {
        if (isLoading || !hasMore) return
        currentPage++
        lifecycleScope.launch {
            isLoading = true
            try {
                val repo = WooRepository(this@CategoryProductsActivity)
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
                android.util.Log.e("CategoryProducts", "Failed to load next page", e)
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
                loadProducts(reset = true)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_all_products, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { finish(); true }
            R.id.action_sort -> { showSortDialog(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }
}