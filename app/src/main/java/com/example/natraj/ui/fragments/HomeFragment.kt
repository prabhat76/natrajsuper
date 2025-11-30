package com.example.natraj.ui.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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
import android.widget.ArrayAdapter
import android.widget.ListPopupWindow
import android.widget.BaseAdapter


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

    // Recent searches
    private lateinit var recentSearchesRecycler: RecyclerView
    private lateinit var recentSearchesContainer: View
    private lateinit var prefs: SharedPreferences
    private val PREFS_NAME = "natraj_prefs"
    private val KEY_RECENT_SEARCHES = "recent_searches"

    private var recentSearchesPopup: ListPopupWindow? = null
    private var recentSearchesDropdownAdapter: ArrayAdapter<String>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        initializeViews(view)
        prefs = requireContext().getSharedPreferences(PREFS_NAME, 0)

        // Initialize ProductManager (loads fallback or Woo products)
        ProductManager.initialize(requireContext())

        setupSearchBar()
        setupBanners()
        setupCategories()
        setupOffers()
        setupRecommendedProducts()
        setupBlog()
        setupClickListeners()

        loadRecentSearches()

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
    }

    private fun setupSearchBar() {
        // Search on icon click
        searchIcon.setOnClickListener {
            val query = searchBar.text.toString().trim()
            if (query.isNotBlank()) {
                onSearchQuery(query)
            } else {
                showToast("Please enter search text")
            }
        }

        // Text search on enter key
        searchBar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val query = searchBar.text.toString().trim()
                if (query.isNotBlank()) {
                    onSearchQuery(query)
                }
                true
            } else {
                false
            }
        }

        // Show dropdown of recent searches when tapping the search bar
        searchBar.setOnClickListener {
            // show dropdown instead of toast
            val recent = readRecentSearchList()
            if (recent.isNotEmpty()) {
                showRecentSearchesPopup(recent)
            } else {
                showToast(getString(R.string.opening_search))
            }
        }

        // Filter suggestions as user types
        searchBar.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s?.toString() ?: ""
                // update popup list filter
                recentSearchesDropdownAdapter?.filter?.filter(text)
                if (!text.isNullOrEmpty() && (recentSearchesPopup?.isShowing != true)) {
                    // show popup with filtered results if there are any
                    val recent = readRecentSearchList()
                    if (recent.isNotEmpty()) showRecentSearchesPopup(recent)
                }
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // Hide popup when focus is lost
        searchBar.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                recentSearchesPopup?.dismiss()
            }
        }
    }

    // Helper: read persisted recent searches robustly (same logic as loadRecentSearches)
    private fun readRecentSearchList(): List<String> {
        val raw = try { prefs.all[KEY_RECENT_SEARCHES] } catch (e: Exception) { null }
        return when (raw) {
            null -> emptyList()
            is String -> if (raw.isBlank()) emptyList() else raw.split("||")
            is Set<*> -> raw.filterIsInstance<String>().toList()
            is Collection<*> -> raw.filterIsInstance<String>().toList()
            else -> try { val asString = raw.toString(); if (asString.isBlank()) emptyList() else asString.split("||") } catch (e: Exception) { emptyList() }
        }
    }

    private fun showRecentSearchesPopup(list: List<String>) {
        // Build data with footer marker
        val data = list.toMutableList()
        val FOOTER_MARKER = "__CLEAR_FOOTER__"
        data.add(FOOTER_MARKER)

        // Create custom adapter
        val adapter = object : BaseAdapter() {
            override fun getCount(): Int = data.size
            override fun getItem(position: Int): Any = data[position]
            override fun getItemId(position: Int): Long = position.toLong()

            override fun getViewTypeCount(): Int = 2
            override fun getItemViewType(position: Int): Int = if (data[position] == FOOTER_MARKER) 1 else 0

            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val inflater = LayoutInflater.from(requireContext())
                return if (getItemViewType(position) == 0) {
                    val v = convertView ?: inflater.inflate(R.layout.item_search_suggestion, parent, false)
                    val icon = v.findViewById<ImageView>(R.id.suggestion_icon)
                    val txt = v.findViewById<TextView>(R.id.suggestion_text)
                    txt.text = data[position]
                    v
                } else {
                    val v = convertView ?: inflater.inflate(R.layout.item_search_footer, parent, false)
                    val footer = v.findViewById<TextView>(R.id.footer_text)
                    footer.setOnClickListener {
                        // Clear stored searches
                        prefs.edit().remove(KEY_RECENT_SEARCHES).apply()
                        recentSearchesPopup?.dismiss()
                        loadRecentSearches()
                    }
                    v
                }
            }
        }

        if (recentSearchesPopup == null) {
            recentSearchesPopup = ListPopupWindow(requireContext())
            recentSearchesPopup?.anchorView = searchBar
            recentSearchesPopup?.setAdapter(adapter) // pass adapter directly
            recentSearchesPopup?.width = resources.getDimensionPixelSize(com.example.natraj.R.dimen.search_dropdown_width)
            recentSearchesPopup?.isModal = true
            recentSearchesPopup?.setOnItemClickListener { _, view, position, _ ->
                val item = data[position]
                if (item == FOOTER_MARKER) return@setOnItemClickListener
                searchBar.setText(item)
                searchBar.setSelection(item.length)
                recentSearchesPopup?.dismiss()
                onSearchQuery(item)
            }
            recentSearchesPopup?.setBackgroundDrawable(resources.getDrawable(R.drawable.bg_search_popup, null))
        } else {
            recentSearchesPopup?.setAdapter(adapter)
        }

        try {
            recentSearchesPopup?.show()
        } catch (e: Exception) {
            android.util.Log.w("HomeFragment", "Failed to show recent searches popup", e)
        }
    }

    private fun onSearchQuery(query: String) {
        // Save recent searches (keep last 3)
        try {
            saveRecentSearch(query)
        } catch (e: Exception) {
            android.util.Log.w("HomeFragment", "Failed saving recent search", e)
        }

        // Try local search (best-effort) but don't block navigation on failure
        try {
            ProductManager.searchProducts(query)
        } catch (e: Exception) {
            android.util.Log.w("HomeFragment", "Local search failed (non-fatal)", e)
        }

        if (!isAdded) {
            android.util.Log.e("HomeFragment", "Fragment not added to activity; cannot start AllProductsActivity")
            showToast("Error performing search: fragment not attached")
            return
        }

        try {
            val intent = Intent(requireActivity(), AllProductsActivity::class.java)
            intent.putExtra("extra_search_query", query)

            // Defensive: ensure there's an activity to handle this intent
            val pm = requireActivity().packageManager
            val resolveInfo = intent.resolveActivity(pm)
            if (resolveInfo == null) {
                android.util.Log.e("HomeFragment", "No activity found to handle AllProductsActivity intent: $intent")
                showToast("Cannot perform search: target activity not available")
                return
            }

            startActivity(intent)
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Failed to start AllProductsActivity", e)
            val msg = e.message ?: "unknown"
            showToast("Error performing search: $msg")

            // Show detailed dialog to help debugging on device
            try {
                val full = android.util.Log.getStackTraceString(e)
                val short = if (full.length > 2000) full.substring(0, 2000) + "..." else full
                if (isAdded) {
                    androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Search Error")
                        .setMessage(short)
                        .setPositiveButton("OK", null)
                        .show()
                }
            } catch (ex: Exception) {
                // ignore dialog failures
            }
        }
    }

    private fun saveRecentSearch(query: String) {
        try {
            // Remove any legacy value (could be a Set<String>) before saving as a single string
            prefs.edit().remove(KEY_RECENT_SEARCHES).apply()

            val existing = try { prefs.getString(KEY_RECENT_SEARCHES, "") ?: "" } catch (e: ClassCastException) { "" }
            val list = if (existing.isBlank()) mutableListOf<String>() else existing.split("||").toMutableList()
            list.remove(query)
            list.add(0, query)
            val trimmed = list.take(3)
            prefs.edit().putString(KEY_RECENT_SEARCHES, trimmed.joinToString("||")).apply()

            // Update dropdown contents if visible
            val recent = readRecentSearchList()
            recentSearchesDropdownAdapter?.clear()
            recentSearchesDropdownAdapter?.addAll(recent)
            if (recentSearchesPopup?.isShowing == true && recentSearchesDropdownAdapter?.count ?: 0 <= 0) {
                recentSearchesPopup?.dismiss()
            }

            loadRecentSearches()
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Failed to save recent search", e)
        }
    }

    private fun loadRecentSearches() {
        // Read raw stored value to robustly handle legacy types (String or Set<String>)
        val raw = try {
            prefs.all[KEY_RECENT_SEARCHES]
        } catch (e: Exception) {
            android.util.Log.w("HomeFragment", "Failed to access prefs.all", e)
            null
        }

        val list: List<String> = when (raw) {
            null -> emptyList()
            is String -> if (raw.isBlank()) emptyList() else raw.split("||")
            is Set<*> -> raw.filterIsInstance<String>().toList()
            is Collection<*> -> raw.filterIsInstance<String>().toList()
            else -> {
                // Last resort: attempt to coerce to String then split
                try {
                    val asString = raw.toString()
                    if (asString.isBlank()) emptyList() else asString.split("||")
                } catch (e: Exception) {
                    android.util.Log.w("HomeFragment", "Unexpected recent searches type: ${raw?.javaClass}", e)
                    emptyList()
                }
            }
        }

        if (list.isEmpty()) {
            recentSearchesContainer.visibility = View.GONE
            recentSearchesRecycler.adapter = null
            return
        }

        recentSearchesContainer.visibility = View.VISIBLE
        recentSearchesRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recentSearchesRecycler.adapter = RecentSearchesAdapter(list) { selectedQuery ->
            searchBar.setText(selectedQuery)
            onSearchQuery(selectedQuery)
        }
    }

    // Small adapter for recent searches
    private class RecentSearchesAdapter(
        private val items: List<String>,
        private val onClick: (String) -> Unit
    ) : RecyclerView.Adapter<RecentSearchesAdapter.VH>() {

        inner class VH(val view: View) : RecyclerView.ViewHolder(view) {
            val text: TextView = view.findViewById(android.R.id.text1)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val tv = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
            val lp = tv.layoutParams
            lp.width = ViewGroup.LayoutParams.WRAP_CONTENT
            tv.layoutParams = lp
            (tv as TextView).setPadding(24, 12, 24, 12)
            tv.setBackgroundResource(com.example.natraj.R.drawable.bg_button_outline)
            return VH(tv)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val value = items[position]
            holder.text.text = value
            holder.itemView.setOnClickListener { onClick(value) }
        }

        override fun getItemCount(): Int = items.size
    }

    private fun searchProducts(query: String) {
        // helper used in some flows; keep behaviour consistent with onSearchQuery
        val ctx = context
        if (ctx == null) {
            android.util.Log.e("HomeFragment", "searchProducts: context is null")
            showToast("Error performing search: context unavailable")
            return
        }

        try {
            val intent = Intent(requireActivity(), AllProductsActivity::class.java)
            intent.putExtra("extra_search_query", query)
            startActivity(intent)
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "searchProducts failed to start AllProductsActivity", e)
            val msg = e.message ?: "unknown"
            showToast("Error performing search: $msg")
        }
    }

    private fun setupBanners() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val repo = com.example.natraj.data.WpRepository(requireContext())
                val banners = withContext(Dispatchers.IO) { repo.getBanners() }

                if (banners.isNotEmpty()) {
                    bannerViewPager.adapter = BannerAdapter(banners) { banner ->
                        navigateToProducts()
                    }
                    android.util.Log.d("HomeFragment", "Loaded ${banners.size} banners from WordPress")
                } else {
                    android.util.Log.w("HomeFragment", "No banners found in WordPress")
                    bannerViewPager.visibility = View.GONE
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeFragment", "Banner fetch failed: ${e.message}", e)
                bannerViewPager.visibility = View.GONE
            }
        }
    }

    private fun setupCategories() {
        // Horizontal simple list
        categoriesRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        categoriesRecycler.setItemViewCacheSize(20)

        val prefs = WooPrefs(requireContext())
        val baseUrl = prefs.baseUrl
        val ck = prefs.consumerKey
        val cs = prefs.consumerSecret

        // Debug logging
        android.util.Log.d("HomeFragment", "DEBUG_CREDENTIALS: baseUrl=$baseUrl, ck=${ck?.take(5)}..., cs=${cs?.take(5)}...")

        val canUseWoo = !baseUrl.isNullOrBlank() && !ck.isNullOrBlank() && !cs.isNullOrBlank()

        if (canUseWoo) {
            android.util.Log.d("HomeFragment", "Credentials found, fetching categories from Woo...")
            // Fetch from WooCommerce
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val repo = WooRepository(requireContext())
                    val list = withContext(Dispatchers.IO) {
                        android.util.Log.d("HomeFragment", "Calling repo.getCategories()...")
                        repo.getCategories()
                    }
                    android.util.Log.d("HomeFragment", "Categories fetched: ${list.size} categories")
                    val allCategory = Category(0, "All", imageUrl = "", hasSpecialOffer = false)
                    val categories = listOf(allCategory) + list
                    categoriesRecycler.adapter = SimpleCategoryAdapter(categories) { category ->
                        // Navigate to the dedicated CategoryProductsActivity with proper extras
                        val intent = Intent(requireContext(), CategoryProductsActivity::class.java)
                        intent.putExtra("category_id", category.id)
                        intent.putExtra("category_name", category.name)
                        startActivity(intent)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("HomeFragment", "Woo categories failed: ${e.message}", e)
                    showToast("Failed to load categories: ${e.message}")
                }
            }
        } else {
            android.util.Log.w("HomeFragment", "Credentials NOT found: baseUrl=$baseUrl, ck=$ck, cs=$cs")
            showToast("Configure WordPress settings to load categories")
        }
    }

    private fun setupOffers() {
        offersRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        offersRecycler.setItemViewCacheSize(10)

        // Use local offers
        val offers = OfferManager.getAllOffers()
        if (offers.isEmpty()) {
            // Show coming soon message
            offersRecycler.visibility = View.GONE
            view?.findViewById<TextView>(R.id.offers_coming_soon)?.visibility = View.VISIBLE
            android.util.Log.w("HomeFragment", "No offers available, showing coming soon")
        } else {
            offersRecycler.adapter = OfferAdapter(offers) { offer ->
                val intent = Intent(requireContext(), AllProductsActivity::class.java)
                startActivity(intent)
            }
            android.util.Log.d("HomeFragment", "Loaded ${offers.size} local offers")
        }
    }

    private fun setupRecommendedProducts() {
        val prefs = com.example.natraj.data.woo.WooPrefs(requireContext())
        val canUseWoo = !prefs.baseUrl.isNullOrBlank() && !prefs.consumerKey.isNullOrBlank() && !prefs.consumerSecret.isNullOrBlank()

        if (canUseWoo) {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val repo = com.example.natraj.data.WooRepository(requireContext())
                    val products = withContext(Dispatchers.IO) {
                        repo.getProducts(com.example.natraj.data.woo.FilterParams(perPage = AppConfig.getBlogPostsLimit(requireContext())))
                    }
                    if (products.isEmpty()) return@launch
                    setupRecommendedRecyclerView(products)
                } catch (e: Exception) {
                    android.util.Log.e("HomeFragment", "Woo recommended failed", e)
                }
            }
        } else {
            showToast("Configure WordPress settings to load products")
        }
    }

    private fun setupRecommendedRecyclerView(products: List<Product>) {
        // Use grid layout for recommended products
        recommendedProductsRecycler.layoutManager = GridLayoutManager(requireContext(), 2)

        // Enable smooth scrolling optimizations
        recommendedProductsRecycler.isNestedScrollingEnabled = false

        recommendedProductsRecycler.adapter = GridProductAdapter(
            products.toMutableList(),
            onProductClick = { product ->
                try {
                    val intent = Intent(requireContext(), ProductDetailActivity::class.java)
                    intent.putExtra("product", product)
                    startActivity(intent)
                } catch (e: Exception) {
                    showToast("Error opening product details")
                }
            }
        )

        android.util.Log.d("HomeFragment", "Recommended products adapter set with ${products.size} items")
    }

    private fun setupBlog() {
        blogRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        blogRecycler.setItemViewCacheSize(10)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val repo = com.example.natraj.data.WpRepository(requireContext())
                val posts = withContext(Dispatchers.IO) { repo.getRecentPosts(5) }
                val adapter = BlogAdapter { post ->
                    val intent = Intent(requireContext(), BlogDetailActivity::class.java)
                    intent.putExtra("blog_post", post)
                    startActivity(intent)
                }
                adapter.submitList(posts)
                blogRecycler.adapter = adapter
            } catch (e: Exception) {
                android.util.Log.e("HomeFragment", "Blog fetch failed", e)
            }
        }
    }

    private fun setupClickListeners() {
        whatsappOrderBtn.setOnClickListener { openWhatsApp() }

        cartIcon.setOnClickListener {
            // Navigate to cart fragment
            (activity as? MainActivity)?.let { it.switchFragment(CartFragment()) }
        }

        viewAllCategoriesBtn.setOnClickListener {
            android.util.Log.d("HomeFragment", "View All Categories clicked!")
            try {
                (activity as? MainActivity)?.switchToCategories()
            } catch (e: Exception) {
                android.util.Log.e("HomeFragment", "Error switching to categories", e)
                showToast("Error: ${e.message}")
            }
        }

        viewAllOffersBtn.setOnClickListener {
            try {
                val intent = Intent(requireContext(), AllProductsActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                android.util.Log.e("HomeFragment", "Error opening AllProductsActivity for offers", e)
                showToast("Error: ${e.message}")
            }
        }

        viewAllRecommendedBtn.setOnClickListener {
            android.util.Log.d("HomeFragment", "View All Recommended clicked!")
            try {
                val intent = Intent(requireContext(), AllProductsActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                android.util.Log.e("HomeFragment", "Error opening AllProductsActivity", e)
                showToast("Error: ${e.message}")
            }
        }

        viewAllBlogBtn.setOnClickListener {
            // Open blog page in BlogActivity
            try {
                val intent = Intent(requireContext(), BlogActivity::class.java)
                intent.putExtra("url", "https://www.natrajsuper.com/blog/")
                startActivity(intent)
            } catch (e: Exception) {
                showToast("Could not open blog")
            }
        }

        searchBar.setOnClickListener {
            showToast("Opening search")
        }
    }

    private fun openWhatsApp() {
        try {
            val phoneNumber = AppConfig.getWhatsAppNumber(requireContext()) // Dynamic WhatsApp number
            val message = "Hi! I'm interested in your Diwali offers. Please share more details."
            val encodedMessage = Uri.encode(message)
            val whatsappUri = "https://wa.me/917851979226?text=$encodedMessage"

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUri))
            startActivity(intent)
        } catch (e: Exception) {
            showToast("WhatsApp not installed")
        }
    }

    private fun updateCartBadge() {
        val count = CartManager.getItemCount()
        cartBadge.text = count.toString()
    }

    private fun showToast(message: String) {
        if (isAdded && context != null) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToProducts() {
        try {
            val intent = Intent(requireContext(), AllProductsActivity::class.java)
            startActivity(intent)
        } catch (e: Exception) {
            CustomToast.showError(requireContext(), "Error opening products")
        }
    }

    private fun fetchProductsByWooCategory(categoryId: Int?, label: String) {
        val prefs = com.example.natraj.data.woo.WooPrefs(requireContext())
        if (prefs.baseUrl.isNullOrBlank()) return
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val repo = com.example.natraj.data.WooRepository(requireContext())
                val products = withContext(Dispatchers.IO) {
                    repo.getProducts(com.example.natraj.data.woo.FilterParams(categoryId = categoryId, perPage = AppConfig.getProductsPerPage(requireContext())))
                }
                updateProductsRecycler(products)
                showToast("Showing products in $label")
            } catch (e: Exception) {
                android.util.Log.e("HomeFragment", "Woo products failed", e)
            }
        }
    }

    private fun updateProductsRecycler(products: List<Product>) {
        productsRecycler.adapter = GridProductAdapter(
            products.toMutableList(),
            onProductClick = { product ->
                val intent = Intent(requireContext(), ProductDetailActivity::class.java)
                intent.putExtra("product", product)
                startActivity(intent)
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            recentSearchesPopup?.dismiss()
        } catch (_: Exception) {}
    }
}
