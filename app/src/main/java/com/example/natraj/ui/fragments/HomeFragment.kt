package com.example.natraj

import android.content.Intent
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
import com.example.natraj.data.WooRepository
import com.example.natraj.data.woo.FilterParams
import com.example.natraj.data.woo.WooClient
import com.example.natraj.data.woo.WooPrefs
import com.example.natraj.util.CustomToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private lateinit var bannerViewPager: ViewPager2
    private lateinit var categoriesRecycler: RecyclerView
    private lateinit var offersRecycler: RecyclerView
    private lateinit var productsRecycler: RecyclerView
    private lateinit var recommendedProductsRecycler: RecyclerView
    private lateinit var blogRecycler: RecyclerView
    private lateinit var searchBar: EditText
    private lateinit var cartIcon: ImageView
    private lateinit var cartBadge: TextView
    private lateinit var whatsappOrderBtn: Button
    private lateinit var viewCatalogueBtn: Button
    private lateinit var viewAllProductsBtn: TextView
    private lateinit var viewAllOffersBtn: TextView
    private lateinit var viewAllRecommendedBtn: TextView
    private lateinit var viewAllBlogBtn: TextView
    private lateinit var viewAllCategoriesBtn: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        
    initializeViews(view)
    setupBanners()
    setupCategories()
    setupOffers()
    setupProducts()
    setupRecommendedProducts()
    setupBlog()
    setupClickListeners()

        return view
    }

    private fun initializeViews(view: View) {
        bannerViewPager = view.findViewById(R.id.banner_viewpager)
        categoriesRecycler = view.findViewById(R.id.categories_recycler)
        offersRecycler = view.findViewById(R.id.offers_recycler)
        productsRecycler = view.findViewById(R.id.products_recycler)
        recommendedProductsRecycler = view.findViewById(R.id.recommended_products_recycler)
        blogRecycler = view.findViewById(R.id.blog_recycler)
        searchBar = view.findViewById(R.id.search_bar)
        cartIcon = view.findViewById(R.id.cart_icon)
        cartBadge = view.findViewById(R.id.cart_badge)
        whatsappOrderBtn = view.findViewById(R.id.whatsapp_order_btn)
        viewCatalogueBtn = view.findViewById(R.id.view_catalogue_btn)
        viewAllProductsBtn = view.findViewById(R.id.view_all_products)
        viewAllOffersBtn = view.findViewById(R.id.view_all_offers)
        viewAllRecommendedBtn = view.findViewById(R.id.view_all_recommended)
        viewAllBlogBtn = view.findViewById(R.id.view_all_blog)
        viewAllCategoriesBtn = view.findViewById(R.id.view_all_categories)
    }

    private fun setupBanners() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val repo = com.example.natraj.data.WpRepository(requireContext())
                val banners = withContext(Dispatchers.IO) { repo.getBanners() }
                
                val bannersToUse = if (banners.isEmpty()) {
                    listOf(
                        Banner(1, "Festival Sale", "UP TO 50% OFF", "On all agricultural equipment", "", "Shop Now"),
                        Banner(2, "Premium Tools", "FREE DELIVERY", "On orders above ₹2000", "", "Order Now"),
                        Banner(3, "Quality Assured", "BEST PRICES", "Guaranteed authentic products", "", "Explore")
                    )
                } else banners
                
                bannerViewPager.adapter = BannerAdapter(bannersToUse) { banner ->
                    navigateToProducts()
                    CustomToast.showSuccess(requireContext(), "${banner.title} - ${banner.subtitle}")
                }
                
                if (banners.isNotEmpty()) {
                    android.util.Log.d("HomeFragment", "Loaded ${banners.size} banners from WordPress")
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeFragment", "Banner fetch failed: ${e.message}", e)
                val fallbackBanners = listOf(
                    Banner(1, "Festival Sale", "UP TO 50% OFF", "On all agricultural equipment", "", "Shop Now"),
                    Banner(2, "Premium Tools", "FREE DELIVERY", "On orders above ₹2000", "", "Order Now"),
                    Banner(3, "Quality Assured", "BEST PRICES", "Guaranteed authentic products", "", "Explore")
                )
                bannerViewPager.adapter = BannerAdapter(fallbackBanners) { banner ->
                    navigateToProducts()
                    CustomToast.showError(requireContext(), "Failed to load banner data")
                }
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
                        if (category.name == "All") {
                            fetchProductsByWooCategory(null, "All")
                        } else {
                            fetchProductsByWooCategory(category.id, category.name)
                        }
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

    private fun setupProducts() {
        // Horizontal layout
        productsRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        productsRecycler.setItemViewCacheSize(15)

        val prefs = com.example.natraj.data.woo.WooPrefs(requireContext())
        val baseUrl = prefs.baseUrl
        val ck = prefs.consumerKey
        val cs = prefs.consumerSecret
        
        android.util.Log.d("HomeFragment", "setupProducts: baseUrl=$baseUrl, ck=${ck?.take(5)}..., cs=${cs?.take(5)}...")
        val canUseWoo = !baseUrl.isNullOrBlank() && !ck.isNullOrBlank() && !cs.isNullOrBlank()

        if (canUseWoo) {
            android.util.Log.d("HomeFragment", "Fetching featured products...")
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val repo = com.example.natraj.data.WooRepository(requireContext())
                    val products = withContext(Dispatchers.IO) {
                        android.util.Log.d("HomeFragment", "Calling repo.getProducts(featured=true)...")
                        repo.getProducts(com.example.natraj.data.woo.FilterParams(perPage = 20), featured = true)
                    }
                    android.util.Log.d("HomeFragment", "Featured products fetched: ${products.size}")
                    if (products.isEmpty()) return@launch
                    productsRecycler.adapter = HorizontalProductAdapter(products) { product ->
                        val intent = Intent(requireContext(), ProductDetailActivity::class.java)
                        intent.putExtra("product", product)
                        startActivity(intent)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("HomeFragment", "Woo featured failed: ${e.message}", e)
                    showToast("Failed to load featured products: ${e.message}")
                }
            }
        } else {
            android.util.Log.w("HomeFragment", "setupProducts: Credentials missing")
            showToast("Configure WordPress settings to load products")
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
            products,
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
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.url))
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

        viewAllProductsBtn.setOnClickListener {
            android.util.Log.d("HomeFragment", "View All Products clicked!")
            try {
                val intent = Intent(requireContext(), AllProductsActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                android.util.Log.e("HomeFragment", "Error opening AllProductsActivity", e)
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
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
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
            products,
            onProductClick = { product ->
                val intent = Intent(requireContext(), ProductDetailActivity::class.java)
                intent.putExtra("product", product)
                startActivity(intent)
            }
        )
    }
}
