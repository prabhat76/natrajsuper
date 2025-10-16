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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

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
    }

    private fun setupBanners() {
        val banners = listOf(
            Banner(1, "🎆 Diwali Special Sale", "UP TO 50% OFF", "On all agricultural equipment", "", "Shop Now"),
            Banner(2, "🚜 Premium Tools", "FREE DELIVERY", "On orders above ₹2000", "", "Order Now"),
            Banner(3, "⚙️ Quality Assured", "BEST PRICES", "Guaranteed authentic products", "", "Explore")
        )
        bannerViewPager.adapter = BannerAdapter(banners) { banner ->
            showToast("Clicked: ${banner.title}")
        }
    }

    private fun setupCategories() {
        val categoryNames = ProductManager.getCategories()
        
        // Add "All" category at the beginning
        val allCategory = Category(
            id = 0,
            name = "All",
            imageUrl = "https://www.natrajsuper.com/wp-content/uploads/2024/05/1212.png",
            hasSpecialOffer = false
        )
        
        val categories = mutableListOf(allCategory)
        categories.addAll(categoryNames.mapIndexed { index, name ->
            Category(
                id = index + 1,
                name = name,
                imageUrl = getCategoryImageUrl(name),
                hasSpecialOffer = index < 3 // First 3 categories have special offers
            )
        })
        
        categoriesRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        
        // Enable smooth scrolling optimizations
        categoriesRecycler.setHasFixedSize(true)
        categoriesRecycler.setItemViewCacheSize(20)
        
        categoriesRecycler.adapter = CategoryAdapter(categories) { category ->
            // Filter products by selected category or show all
            if (category.name == "All") {
                showAllProducts()
            } else {
                filterProductsByCategory(category.name)
            }
        }
    }

    private fun getCategoryImageUrl(categoryName: String): String {
        return when (categoryName) {
            "High Pressure Agriculture Sprayer HTP PUMP" -> "https://www.natrajsuper.com/wp-content/uploads/2024/05/1212.png"
            "Air compressor" -> "https://www.natrajsuper.com/wp-content/uploads/2025/01/Untitled-design-53.png"
            "Chaff Cutters" -> "https://www.natrajsuper.com/wp-content/uploads/2024/12/IMG_1986-scaled.jpg"
            "Earth Augers Machine" -> "https://www.natrajsuper.com/wp-content/uploads/2024/11/Untitled-design-2-5.png"
            "Electric HTP Pump" -> "https://www.natrajsuper.com/wp-content/uploads/2024/05/1212.png"
            "Gasoline Water Pump" -> "https://www.natrajsuper.com/wp-content/uploads/2025/07/AS-515WP.png"
            "Welding Machine" -> "https://www.natrajsuper.com/wp-content/uploads/2024/05/1212.png"
            "High Pressure Washers" -> "https://www.natrajsuper.com/wp-content/uploads/2025/01/Untitled-design-53.png"
            "Gasoline/Diesel Engines" -> "https://www.natrajsuper.com/wp-content/uploads/2025/07/IMG_8767-scaled.jpg"
            "Gasoline Engine Sprayer" -> "https://www.natrajsuper.com/wp-content/uploads/2024/12/photo_2024-12-19_13-11-50-2.jpg"
            else -> "https://www.natrajsuper.com/wp-content/uploads/2024/05/1212.png"
        }
    }

    private fun setupOffers() {
        val offers = OfferManager.getAllOffers()
        offersRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        offersRecycler.setHasFixedSize(true)
        offersRecycler.setItemViewCacheSize(10)
        offersRecycler.adapter = OfferAdapter(offers) { offer ->
            // Open product detail or web page
            showToast("Offer: ${offer.title}")
        }
    }

    private fun setupProducts() {
        val products = ProductManager.getFeaturedProducts()
        
        android.util.Log.d("HomeFragment", "Featured products count: ${products.size}")
        android.util.Log.d("HomeFragment", "All products count: ${ProductManager.getAllProducts().size}")

        if (products.isEmpty()) {
            android.util.Log.w("HomeFragment", "No featured products found!")
            Toast.makeText(requireContext(), "No products available", Toast.LENGTH_LONG).show()
            return
        }

        // Use horizontal layout for better scrolling in home
        productsRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        
        // Enable smooth scrolling optimizations
        productsRecycler.setHasFixedSize(true)
        productsRecycler.setItemViewCacheSize(15)
        
        productsRecycler.adapter = HorizontalProductAdapter(
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
        
        android.util.Log.d("HomeFragment", "Products adapter set with ${products.size} items")
    }

    private fun setupRecommendedProducts() {
        // Get products not in featured list (or all products for more variety)
        val allProducts = ProductManager.getAllProducts()
        val featuredProducts = ProductManager.getFeaturedProducts()
        
        // Get products that are not featured, or show remaining products
        val recommendedProducts = allProducts.filterNot { product ->
            featuredProducts.any { it.id == product.id }
        }.take(6) // Show 6 products in grid (2 rows x 3 columns)
        
        android.util.Log.d("HomeFragment", "Recommended products count: ${recommendedProducts.size}")

        if (recommendedProducts.isEmpty()) {
            // If no non-featured products, show some featured ones
            val fallbackProducts = allProducts.take(6)
            setupRecommendedRecyclerView(fallbackProducts)
        } else {
            setupRecommendedRecyclerView(recommendedProducts)
        }
    }

    private fun setupRecommendedRecyclerView(products: List<Product>) {
        // Use grid layout for recommended products
        recommendedProductsRecycler.layoutManager = GridLayoutManager(requireContext(), 2)
        
        // Enable smooth scrolling optimizations
        recommendedProductsRecycler.setHasFixedSize(true)
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
        val blogPosts = BlogManager.getRecentPosts(5)
        
        blogRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        blogRecycler.setHasFixedSize(true)
        blogRecycler.setItemViewCacheSize(10)
        
        blogRecycler.adapter = BlogAdapter(blogPosts) { post ->
            // Open blog post in BlogActivity (handled in adapter)
        }
        
        android.util.Log.d("HomeFragment", "Blog posts loaded: ${blogPosts.size}")
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
    
    private fun filterProductsByCategory(categoryName: String) {
        val filteredProducts = ProductManager.getProductsByCategory(categoryName)
        updateProductsRecycler(filteredProducts)
        showToast("Showing products in $categoryName")
    }
    
    private fun showAllProducts() {
        val allProducts = ProductManager.getAllProducts()
        updateProductsRecycler(allProducts)
        showToast("Showing all products")
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
