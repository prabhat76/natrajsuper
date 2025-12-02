package com.example.natraj

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.natraj.data.model.Product
import com.example.natraj.ui.activities.MainActivity
import com.example.natraj.ui.activities.QuickCheckoutActivity

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var carousel: ViewPager2
    private lateinit var dotIndicator: LinearLayout
    private val slideshowHandler = Handler(Looper.getMainLooper())
    private var isSlideshowActive = true
    private val slideshowRunnable = object : Runnable {
        override fun run() {
            if (isSlideshowActive && carousel.adapter != null) {
                val itemCount = carousel.adapter?.itemCount ?: 0
                if (itemCount > 1) {
                    val nextItem = (carousel.currentItem + 1) % itemCount
                    carousel.setCurrentItem(nextItem, true)
                    slideshowHandler.postDelayed(this, 3000) // Auto-advance every 3 seconds
                }
            }
        }
    }

    private lateinit var headerCartIcon: ImageView
    private lateinit var headerCartBadge: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        val product = intent.getSerializableExtra("product") as? Product
        if (product == null) {
            finish()
            return
        }

        val carousel = findViewById<ViewPager2>(R.id.detail_image_carousel)
        val dotIndicator = findViewById<LinearLayout>(R.id.detail_dot_indicator)
        val name = findViewById<TextView>(R.id.detail_name)
        val category = findViewById<TextView>(R.id.detail_category)
        val price = findViewById<TextView>(R.id.detail_price)
        val original = findViewById<TextView>(R.id.detail_original_price)
        val description = findViewById<TextView>(R.id.detail_description)
        val add = findViewById<Button>(R.id.detail_add_to_cart)
        val buyNow = findViewById<Button>(R.id.detail_buy_now)
        val wishlistBtn = findViewById<ImageView>(R.id.detail_wishlist_btn)
        val specsContainer = findViewById<LinearLayout>(R.id.detail_specs_container)
        val backButton = findViewById<ImageView>(R.id.back_button)
        val headerSearch = findViewById<ImageView>(R.id.header_search)
        headerCartIcon = findViewById(R.id.header_cart_icon)
        headerCartBadge = findViewById(R.id.header_cart_badge)

        // Store references for slideshow
        this.carousel = carousel
        this.dotIndicator = dotIndicator

        backButton.setOnClickListener { finish() }

        headerSearch.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("open_search", true)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
        }

        headerCartIcon.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("open_tab", "cart")
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
        }

        // Setup wishlist button
        updateWishlistButton(wishlistBtn, product.id)
        
        wishlistBtn.setOnClickListener {
            val added = WishlistManager.toggle(product.id)
            updateWishlistButton(wishlistBtn, product.id)
            val message = if (added) "Added to wishlist" else "Removed from wishlist"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        name.text = product.name ?: "Product"
        category.text = product.category ?: "General"
        
        // Show transfer price instead of regular price for better deals
        val displayPrice = if (product.transferPrice > 0) product.transferPrice else product.price
        price.text = "₹${displayPrice.toInt()}"
        
        // Render HTML description as bullet points instead of paragraphs
        if (!product.description.isNullOrEmpty()) {
            // Convert HTML description to bullet point format
            val processedDescription = processDescriptionToBullets(product.description)
            description.text = processedDescription
        } else {
            description.text = "No description available"
        }

        // Show MRP vs Transfer Price for better discount display
        if (product.mrp > displayPrice && product.mrp > 0) {
            original.visibility = android.view.View.VISIBLE
            original.text = "₹${product.mrp.toInt()}"
            original.paintFlags = original.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else if (product.originalPrice > displayPrice) {
            original.visibility = android.view.View.VISIBLE
            original.text = "₹${product.originalPrice.toInt()}"
            original.paintFlags = original.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        }

        // Setup image carousel with slideshow
        val images = if (!product.images.isNullOrEmpty()) product.images else listOf(product.imageUrl)
        carousel.adapter = ProductImageCarouselAdapter(images)
        
        // Setup dot indicators
        setupDotIndicators(dotIndicator, images.size, 0)
        carousel.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                setupDotIndicators(dotIndicator, images.size, position)
            }
        })

        // Start slideshow if multiple images
        if (images.size > 1) {
            startSlideshow()
        }

        // Pause slideshow on user interaction
        carousel.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                setupDotIndicators(dotIndicator, images.size, position)
                // Reset slideshow timer when user manually changes page
                if (images.size > 1) {
                    restartSlideshow()
                }
            }
        })

        val qty = 1

        add.setOnClickListener {
            CartManager.add(product, qty)
            Toast.makeText(this, "${product.name} added to cart", Toast.LENGTH_SHORT).show()
        }

        buyNow.setOnClickListener {
            // Navigate directly to checkout
            val intent = Intent(this, QuickCheckoutActivity::class.java)
            intent.putExtra("product", product)
            intent.putExtra("quantity", qty)
            startActivity(intent)
        }

        // Render comprehensive specs
        specsContainer.removeAllViews()
        
        // Add basic product specs first
        for ((k, v) in product.specs) {
            addSpecRow(specsContainer, k, v)
        }
        
        // Add additional product information
        if (!product.articleCode.isNullOrEmpty()) {
            addSpecRow(specsContainer, "Article Code", product.articleCode)
        }
        
        if (!product.brand.isNullOrEmpty() && product.brand != "Natraj Super") {
            addSpecRow(specsContainer, "Brand", product.brand)
        }
        
        if (!product.weight.isNullOrEmpty()) {
            addSpecRow(specsContainer, "Weight", product.weight)
        }
        
        if (!product.dimensions.isNullOrEmpty()) {
            addSpecRow(specsContainer, "Dimensions (cm)", product.dimensions)
        }
        
        if (!product.hsnCode.isNullOrEmpty()) {
            addSpecRow(specsContainer, "HSN Code", product.hsnCode)
        }
        
        if (product.tax > 0) {
            addSpecRow(specsContainer, "GST", "${product.tax}%")
        }
        
        if (product.moq > 1) {
            addSpecRow(specsContainer, "Minimum Order Qty", product.moq.toString())
        }
        
        if (!product.warranty.isNullOrEmpty()) {
            addSpecRow(specsContainer, "Warranty", product.warranty)
        }
        
        addSpecRow(specsContainer, "Country of Origin", product.countryOfOrigin ?: "India")
        
        // Add package contents if available
        if (!product.packageContents.isNullOrEmpty()) {
            addSectionTitle(specsContainer, "Package Contents")
            product.packageContents.forEach { item ->
                addBulletPoint(specsContainer, item)
            }
        }
        
        // Add product uses/applications
        if (!product.uses.isNullOrEmpty()) {
            addSectionTitle(specsContainer, "Applications/Uses")
            product.uses.forEach { use ->
                addBulletPoint(specsContainer, use)
            }
        }
        
        // Add features if available
        if (!product.features.isNullOrEmpty()) {
            addSectionTitle(specsContainer, "Key Features")
            product.features.forEach { feature ->
                addBulletPoint(specsContainer, feature)
            }
        }
        
        // Add manufacturer info
        if (!product.manufacturerInfo.isNullOrEmpty()) {
            addSectionTitle(specsContainer, "Manufacturer Information")
            addSpecText(specsContainer, product.manufacturerInfo ?: "")
        }
    }
    
    private fun startSlideshow() {
        isSlideshowActive = true
        slideshowHandler.postDelayed(slideshowRunnable, 3000)
    }
    
    private fun stopSlideshow() {
        isSlideshowActive = false
        slideshowHandler.removeCallbacks(slideshowRunnable)
    }
    
    private fun restartSlideshow() {
        stopSlideshow()
        startSlideshow()
    }
    
    override fun onResume() {
        super.onResume()
        // Resume slideshow when activity becomes visible
        if (carousel.adapter != null && carousel.adapter?.itemCount ?: 0 > 1) {
            startSlideshow()
        }
        updateCartBadge()
    }
    
    override fun onPause() {
        super.onPause()
        // Pause slideshow when activity is not visible
        stopSlideshow()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Clean up slideshow handler
        stopSlideshow()
    }
    
    private fun addSpecRow(container: LinearLayout, label: String, value: String) {
        val row = TextView(this)
        row.text = "$label: $value"
        row.setPadding(0, 8, 0, 8)
        row.setTextColor(resources.getColor(R.color.text_secondary, theme))
        row.textSize = 14f
        container.addView(row)
    }
    
    private fun addSectionTitle(container: LinearLayout, title: String) {
        val titleView = TextView(this)
        titleView.text = title
        titleView.setPadding(0, 16, 0, 8)
        titleView.setTextColor(resources.getColor(R.color.text_primary, theme))
        titleView.textSize = 15f
        titleView.setTypeface(null, android.graphics.Typeface.BOLD)
        container.addView(titleView)
    }
    
    private fun addBulletPoint(container: LinearLayout, text: String) {
        val bulletView = TextView(this)
        bulletView.text = "• $text"
        bulletView.setPadding(16, 4, 0, 4)
        bulletView.setTextColor(resources.getColor(R.color.text_secondary, theme))
        bulletView.textSize = 13f
        container.addView(bulletView)
    }
    
    private fun addSpecText(container: LinearLayout, text: String) {
        val textView = TextView(this)
        textView.text = text
        textView.setPadding(0, 8, 0, 8)
        textView.setTextColor(resources.getColor(R.color.text_secondary, theme))
        textView.textSize = 13f
        container.addView(textView)
    }
    
    private fun processDescriptionToBullets(htmlDescription: String): String {
        // Remove HTML tags and split into sentences/paragraphs
        val cleanText = HtmlCompat.fromHtml(htmlDescription, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
            .replace(Regex("\\s+"), " ") // Normalize whitespace
            .trim()

        // Split by sentences and paragraphs
        val sentences = cleanText.split(Regex("[.!?]+\\s*"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        // Convert each sentence to bullet point
        return sentences.joinToString("\n") { "• $it" }
    }
    
    private fun updateWishlistButton(wishlistBtn: ImageView, productId: Int) {
        if (WishlistManager.isInWishlist(productId)) {
            wishlistBtn.setImageResource(android.R.drawable.btn_star_big_on)
            wishlistBtn.setColorFilter(android.graphics.Color.parseColor("#1976D2"))
        } else {
            wishlistBtn.setImageResource(android.R.drawable.btn_star_big_off)
            wishlistBtn.setColorFilter(android.graphics.Color.parseColor("#BDBDBD"))
        }
    }
    
    private fun setupDotIndicators(container: LinearLayout, count: Int, activeIndex: Int) {
        container.removeAllViews()
        val dots = Array(count) { ImageView(this) }
        
        dots.forEachIndexed { index, imageView ->
            imageView.setImageResource(
                if (index == activeIndex) android.R.drawable.presence_online
                else android.R.drawable.presence_invisible
            )
            
            val params = LinearLayout.LayoutParams(16, 16)
            params.setMargins(4, 0, 4, 0)
            imageView.layoutParams = params
            
            if (index == activeIndex) {
                imageView.setColorFilter(android.graphics.Color.parseColor("#1976D2"))
            } else {
                imageView.setColorFilter(android.graphics.Color.parseColor("#BDBDBD"))
            }
            
            container.addView(imageView)
        }
    }
    
    private fun navigateBackToMain() {
        // Prefer normal finish to preserve back stack if came from list
        finish()
    }

    private fun updateCartBadge() {
        val count = CartManager.getItemCount()
        headerCartBadge.text = count.toString()
        headerCartBadge.isVisible = count > 0
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}
