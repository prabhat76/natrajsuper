package com.example.natraj

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.natraj.data.WooRepository
import com.example.natraj.data.woo.FilterParams
import com.example.natraj.data.woo.WooClient
import com.example.natraj.data.woo.WooPrefs
import com.example.natraj.util.CustomToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

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
    private lateinit var viewCatalogueBtn: Button
    private lateinit var viewAllProductsBtn: TextView
    private lateinit var viewAllOffersBtn: TextView
    private lateinit var viewAllRecommendedBtn: TextView
    private lateinit var viewAllBlogBtn: TextView
    private lateinit var viewAllCategoriesBtn: TextView
    
    private val voiceSearchLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                val searchText = matches[0]
                searchBar.setText(searchText)
                searchProducts(searchText)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        
        initializeViews(view)
        setupSearchBar()
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
        viewCatalogueBtn = view.findViewById(R.id.view_catalogue_btn)
        viewAllOffersBtn = view.findViewById(R.id.view_all_offers)
        viewAllRecommendedBtn = view.findViewById(R.id.view_all_recommended)
        viewAllBlogBtn = view.findViewById(R.id.view_all_blog)
        viewAllCategoriesBtn = view.findViewById(R.id.view_all_categories)
    }
    
    private fun setupSearchBar() {
        // Voice search on icon click
        searchIcon.setOnClickListener {
            startVoiceSearch()
        }
        
        // Text search
        searchBar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val query = searchBar.text.toString()
                if (query.isNotBlank()) {
                    searchProducts(query)
                }
                true
            } else {
                false
            }
        }
    }
    
    private fun startVoiceSearch() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Say product name or category...")
            }
            voiceSearchLauncher.launch(intent)
        } catch (e: Exception) {
            if (isAdded && context != null) {
                Toast.makeText(requireContext(), "Voice search not available", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun searchProducts(query: String) {
        val intent = Intent(requireContext(), AllProductsActivity::class.java)
        intent.putExtra("extra_search_query", query)
        startActivity(intent)
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
                        val intent = Intent(requireContext(), com.example.natraj.ui.activities.CategoryProductsActivity::class.java)
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
        
        // Fetch offer banners dynamically from WordPress
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val repo = com.example.natraj.data.WpRepository(requireContext())
                val offerBanners = withContext(Dispatchers.IO) { repo.getOfferBanners() }
                
                if (offerBanners.isEmpty()) {
                    // Fallback to OfferManager if no banners found
                    android.util.Log.w("HomeFragment", "No offer banners from WordPress, using fallback")
                    val offers = OfferManager.getAllOffers()
                    offersRecycler.adapter = OfferAdapter(offers) { offer ->
                        showToast("Offer: ${offer.title}")
                    }
                } else {
                    // Use BannerAdapter to display offer banners
                    offersRecycler.adapter = BannerAdapter(offerBanners) { banner ->
                        showToast("${banner.title}: ${banner.subtitle}")
                    }
                    android.util.Log.d("HomeFragment", "Loaded ${offerBanners.size} offer banners from WordPress")
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeFragment", "Offer banners fetch failed: ${e.message}", e)
                // Fallback to local offers
                val offers = OfferManager.getAllOffers()
                offersRecycler.adapter = OfferAdapter(offers) { offer ->
                    showToast("Offer: ${offer.title}")
                }
            }
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
                        repo.getProducts(com.example.natraj.data.woo.FilterParams(perPage = 6))
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
        
        viewCatalogueBtn.setOnClickListener {
            val intent = Intent(requireContext(), CatalogueActivity::class.java)
            startActivity(intent)
        }

        cartIcon.setOnClickListener {
            // Navigate to cart fragment
            (activity as? MainActivity)?.let { it.switchFragment(CartFragment()) }
        }

        viewAllCategoriesBtn.setOnClickListener {
            android.util.Log.d("HomeFragment", "View All Categories clicked!")
            try {
                val intent = Intent(requireContext(), AllProductsActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                android.util.Log.e("HomeFragment", "Error opening AllProductsActivity", e)
                showToast("Error: ${e.message}")
            }
        }

        viewAllOffersBtn.setOnClickListener {
            showToast("Viewing all offers")
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
            val phoneNumber = "+911234567890" // Replace with actual WhatsApp number
            val message = "Hi! I'm interested in your Diwali offers. Please share more details."
            val encodedMessage = Uri.encode(message)
            val whatsappUri = "https://wa.me/$phoneNumber?text=$encodedMessage"
            
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
                    repo.getProducts(com.example.natraj.data.woo.FilterParams(categoryId = categoryId, perPage = 30))
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
}
