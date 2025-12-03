package com.example.natraj.ui.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.ArrayAdapter
import android.widget.ListPopupWindow
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.natraj.BannerAdapter
import com.example.natraj.BlogActivity
import com.example.natraj.BlogAdapter
import com.example.natraj.BlogDetailActivity
import com.example.natraj.CartFragment
import com.example.natraj.R
import com.example.natraj.data.WooRepository
import com.example.natraj.data.AppConfig
import com.example.natraj.data.model.Product
import com.example.natraj.data.woo.FilterParams
import com.example.natraj.data.woo.WooPrefs
import com.example.natraj.ui.activities.AllProductsActivity
import com.example.natraj.ui.activities.MainActivity
import com.example.natraj.ui.adapters.GridProductAdapter
import com.example.natraj.util.CustomToast
import com.example.natraj.data.repository.WpRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import com.example.natraj.Category
import com.example.natraj.OfferManager
import com.example.natraj.CartManager
import com.example.natraj.OfferAdapter
import com.example.natraj.ProductDetailActivity
import com.example.natraj.SimpleCategoryAdapter
import com.example.natraj.ui.activities.CategoryProductsActivity
import com.example.natraj.ProductManager
import android.util.Log

class HomeFragment : Fragment() {

    private lateinit var bannerViewPager: ViewPager2
    private lateinit var categoriesRecycler: RecyclerView
    private lateinit var offersRecycler: RecyclerView
    private lateinit var productsRecycler: RecyclerView
    private lateinit var recommendedProductsRecycler: RecyclerView
    private lateinit var blogRecycler: RecyclerView
    private lateinit var searchBar: EditText
    private lateinit var searchIcon: ImageView
    private lateinit var cartIcon: ImageView
    private lateinit var cartBadge: TextView
    private lateinit var whatsappOrderBtn: Button
    private lateinit var viewAllProductsBtn: TextView
    private lateinit var viewAllOffersBtn: TextView
    private lateinit var viewAllRecommendedBtn: TextView
    private lateinit var viewAllBlogBtn: TextView
    private lateinit var viewAllCategoriesBtn: TextView

    private lateinit var recentSearchesRecycler: RecyclerView
    private lateinit var recentSearchesContainer: View
    private lateinit var prefs: SharedPreferences
    private val PREFS_NAME = "natraj_prefs"
    private val KEY_RECENT_SEARCHES = "recent_searches"

    // Popup for showing recent searches
    private var recentSearchesPopup: ListPopupWindow? = null

    private lateinit var emptyStateStrip: TextView
    private lateinit var offersComingSoon: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        initializeViews(view)
        prefs = requireContext().getSharedPreferences(PREFS_NAME, 0)

        ProductManager.initialize(requireContext())

        setupSearchBar()
        setupCartIcon()
        setupBanners()
        setupCategories()
        setupOffers()
        setupRecommendedProducts()
        setupBlog()
        setupClickListeners()

        return view
    }

    private fun initializeViews(view: View) {
        bannerViewPager = view.findViewById(R.id.banner_viewpager)
        categoriesRecycler = view.findViewById(R.id.categories_recycler)
        offersRecycler = view.findViewById(R.id.offers_recycler)
        recommendedProductsRecycler = view.findViewById(R.id.recommended_products_recycler)
        blogRecycler = view.findViewById(R.id.blog_recycler)
        searchBar = view.findViewById(R.id.search_bar)
        searchIcon = view.findViewById(R.id.search_icon)
        cartIcon = view.findViewById(R.id.cart_icon)
        cartBadge = view.findViewById(R.id.cart_badge)
        whatsappOrderBtn = view.findViewById(R.id.whatsapp_order_btn)
        viewAllOffersBtn = view.findViewById(R.id.view_all_offers)
        viewAllRecommendedBtn = view.findViewById(R.id.view_all_recommended)
        viewAllBlogBtn = view.findViewById(R.id.view_all_blog)
        viewAllCategoriesBtn = view.findViewById(R.id.view_all_categories)

        recentSearchesRecycler = view.findViewById(R.id.recent_searches_recycler)
        recentSearchesContainer = view.findViewById(R.id.recent_searches_container)
        emptyStateStrip = view.findViewById(R.id.empty_state_strip)
        offersComingSoon = view.findViewById(R.id.offers_coming_soon)
    }

    private fun setupSearchBar() {
        // Show dropdown when search gains focus
        searchBar.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) showRecentSearchesDropdown() else dismissRecentSearchesDropdown()
        }

        // Also show when user taps the field
        searchBar.setOnClickListener { showRecentSearchesDropdown() }

        searchIcon.setOnClickListener {
            val query = searchBar.text.toString().trim()
            if (query.isNotBlank()) {
                saveRecentSearch(query)
                onSearchQuery(query)
            } else {
                showToast("Please enter search text")
            }
        }

        searchBar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = searchBar.text.toString().trim()
                if (query.isNotBlank()) {
                    saveRecentSearch(query)
                    onSearchQuery(query)
                }
                true
            } else {
                false
            }
        }
    }

    private fun showRecentSearchesDropdown() {
        val recent = getRecentSearches()
        if (recent.isEmpty()) {
            dismissRecentSearchesDropdown()
            return
        }
        val popup = recentSearchesPopup ?: ListPopupWindow(requireContext()).also { recentSearchesPopup = it }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, recent)
        popup.setAdapter(adapter)
        popup.anchorView = searchBar
        popup.isModal = true
        popup.setOnItemClickListener { _, _, position, _ ->
            val selected = recent[position]
            searchBar.setText(selected)
            searchBar.setSelection(selected.length)
            dismissRecentSearchesDropdown()
            saveRecentSearch(selected)
            onSearchQuery(selected)
        }
        popup.show()
    }

    private fun dismissRecentSearchesDropdown() {
        recentSearchesPopup?.dismiss()
    }

    private fun saveRecentSearch(query: String) {
        // Use StringSet to avoid ClassCastException
        val set = prefs.getStringSet(KEY_RECENT_SEARCHES, emptySet())?.toMutableSet() ?: mutableSetOf()
        // Maintain uniqueness and most-recent-first with max size 3
        // Remove existing to re-add on top
        set.remove(query)
        set.add(query)
        // If over 3, trim by oldest — convert to list to control order
        val list = set.toMutableList()
        // We can't rely on set order; rebuild from prefs stored list when available
        val current = getRecentSearches().toMutableList()
        current.remove(query)
        current.add(0, query)
        val trimmed = current.take(3)
        prefs.edit().putStringSet(KEY_RECENT_SEARCHES, trimmed.toSet()).apply()
    }

    private fun getRecentSearches(): List<String> {
        // Safely read as StringSet; never cast to String
        val set = prefs.getStringSet(KEY_RECENT_SEARCHES, emptySet())
        // Show most recent first; we stored in order via saveRecentSearch
        return set?.toList() ?: emptyList()
    }

    private fun setupCartIcon() {
        updateCartBadge()

        cartIcon.setOnClickListener {
            // Navigate to cart fragment within the same MainActivity
            (activity as? MainActivity)?.let { mainActivity ->
                mainActivity.supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, CartFragment())
                    .addToBackStack(null)
                    .commit()

                // Update bottom navigation to show cart tab as selected
                mainActivity.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
                    ?.selectedItemId = R.id.nav_cart
            }
        }

        CartManager.registerListener { updateCartBadge() }
    }

    private fun updateCartBadge() {
        val count = CartManager.getItemCount()
        if (count > 0) {
            cartBadge.visibility = View.VISIBLE
            cartBadge.text = count.toString() // Show exact count, not hardcoded "3"
        } else {
            cartBadge.visibility = View.GONE
        }
    }

    private fun onSearchQuery(query: String) {
        val intent = Intent(requireContext(), AllProductsActivity::class.java)
        intent.putExtra("search_query", query)
        try {
            startActivity(intent)
        } catch (e: Exception) {
            // Avoid crash if activity missing; show feedback
            showToast("Search results screen is not available")
        }
    }

    private fun setupBanners() {
        lifecycleScope.launch {
            try {
                val repository = WpRepository(requireContext())
                val banners = withContext(Dispatchers.IO) {
                    repository.getBanners()
                }

                if (banners.isNotEmpty()) {
                    bannerViewPager.adapter = BannerAdapter(banners) { banner ->
                        // Handle banner click - navigate to appropriate section
                        when {
                            banner.title.contains("pump", ignoreCase = true) -> {
                                // Navigate to pumps category
                                val intent = Intent(requireContext(), CategoryProductsActivity::class.java)
                                intent.putExtra("category_id", 1)
                                intent.putExtra("category_name", "Pumps")
                                startActivity(intent)
                            }
                            banner.title.contains("sprayer", ignoreCase = true) -> {
                                // Navigate to sprayers category
                                val intent = Intent(requireContext(), CategoryProductsActivity::class.java)
                                intent.putExtra("category_id", 3)
                                intent.putExtra("category_name", "Sprayers")
                                startActivity(intent)
                            }
                            banner.title.contains("delivery", ignoreCase = true) -> {
                                // Show delivery info or navigate to products
                                showToast("Free delivery on orders above ₹2000!")
                            }
                            else -> {
                                // Navigate to all products
                                val intent = Intent(requireContext(), AllProductsActivity::class.java)
                                startActivity(intent)
                            }
                        }
                    }
                    emptyStateStrip.visibility = View.GONE

                    // Auto-scroll banners every 4 seconds
                    setupBannerAutoScroll(banners.size)
                } else {
                    bannerViewPager.adapter = BannerAdapter(emptyList())
                    emptyStateStrip.visibility = View.VISIBLE
                    emptyStateStrip.text = "Loading premium agricultural equipment offers..."
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error loading banners: ${e.message}")
                bannerViewPager.adapter = BannerAdapter(emptyList())
                emptyStateStrip.visibility = View.VISIBLE
                emptyStateStrip.text = "Natraj Super - Premium Agricultural Equipment"
            }
        }
    }

    private fun setupBannerAutoScroll(bannerCount: Int) {
        if (bannerCount > 1) {
            val handler = android.os.Handler(android.os.Looper.getMainLooper())
            val scrollRunnable = object : Runnable {
                override fun run() {
                    val currentItem = bannerViewPager.currentItem
                    val nextItem = if (currentItem == bannerCount - 1) 0 else currentItem + 1
                    bannerViewPager.setCurrentItem(nextItem, true)
                    handler.postDelayed(this, 4000) // 4 seconds
                }
            }
            handler.postDelayed(scrollRunnable, 4000)
        }
    }

    private fun setupCategories() {
        categoriesRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        
        // Load categories from WooCommerce
        lifecycleScope.launch {
            try {
                val wooRepository = WooRepository(requireContext())
                val wooCategories = withContext(Dispatchers.IO) { wooRepository.getCategories() }

                // The repository already maps to app Category model; prepend an "All" item
                val categories = mutableListOf<Category>().apply {
                    add(Category(0, "All", R.drawable.ic_category, "", hasSpecialOffer = false, productCount = 0))
                    addAll(wooCategories)
                }

                categoriesRecycler.adapter = com.example.natraj.SimpleCategoryAdapter(categories) { category ->
                    val intent = Intent(requireContext(), com.example.natraj.ui.activities.CategoryProductsActivity::class.java)
                    intent.putExtra("category_id", category.id)
                    intent.putExtra("category_name", category.name)
                    startActivity(intent)
                }

            } catch (e: Exception) {
                // Fallback to local categories if API fails
                val fallbackCategories = listOf(
                    Category(0, "All", R.drawable.ic_category, "", hasSpecialOffer = false, productCount = 0),
                    Category(1, "Sprayers", R.drawable.ic_spray, "", hasSpecialOffer = true, productCount = 15),
                    Category(2, "Compressors", R.drawable.ic_compressor, "", hasSpecialOffer = false, productCount = 8),
                    Category(3, "Pumps", R.drawable.ic_pump, "", hasSpecialOffer = false, productCount = 10)
                )
                categoriesRecycler.adapter = com.example.natraj.SimpleCategoryAdapter(fallbackCategories) { category ->
                    val intent = Intent(requireContext(), com.example.natraj.ui.activities.CategoryProductsActivity::class.java)
                    intent.putExtra("category_id", category.id)
                    intent.putExtra("category_name", category.name)
                    startActivity(intent)
                }
            }
        }
    }

    private fun setupOffers() {
        offersRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val offers = OfferManager.getAllOffers()
        offersRecycler.adapter = OfferAdapter(offers) { offer ->
            val intent = Intent(requireContext(), AllProductsActivity::class.java)
            intent.putExtra("offer_id", offer.id)
            try {
                startActivity(intent)
            } catch (e: Exception) {
                showToast("Offers screen is not available")
            }
        }
        offersComingSoon.visibility = if (offers.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun setupRecommendedProducts() {
        recommendedProductsRecycler.layoutManager = GridLayoutManager(requireContext(), 2)
        val products = ProductManager.getAllProducts().take(6)
        recommendedProductsRecycler.adapter = GridProductAdapter(products.toMutableList(),
            onProductClick = { product ->
                val intent = Intent(requireContext(), ProductDetailActivity::class.java)
                intent.putExtra("product", product)
                startActivity(intent)
            },
            onAddToCart = { product ->
                CartManager.add(product, 1)
            }
        )
    }

    private fun setupBlog() {
        blogRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        blogRecycler.adapter = BlogAdapter { blog ->
            val intent = Intent(requireContext(), BlogDetailActivity::class.java)
            intent.putExtra("blog_id", blog.id)
            startActivity(intent)
        }
    }

    private fun setupClickListeners() {
        whatsappOrderBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://wa.me/919876543210?text=Hello, I want to place an order")
            startActivity(intent)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        updateCartBadge()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        CartManager.unregisterListener { updateCartBadge() }
        dismissRecentSearchesDropdown()
    }
}