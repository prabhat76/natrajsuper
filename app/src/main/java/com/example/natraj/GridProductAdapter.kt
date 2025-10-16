package com.example.natraj

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class GridProductAdapter(
    private val products: List<Product>,
    private val onProductClick: (Product) -> Unit = {},
    private val onFavoriteClick: (Product) -> Unit = {}
) : RecyclerView.Adapter<GridProductAdapter.GridProductViewHolder>() {

    inner class GridProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.product_image)
        val productName: TextView = itemView.findViewById(R.id.product_name)
        val currentPrice: TextView = itemView.findViewById(R.id.current_price)
        val originalPrice: TextView = itemView.findViewById(R.id.original_price)
        val discountPercent: TextView = itemView.findViewById(R.id.discount_percent)
        val discountText: TextView = itemView.findViewById(R.id.discount_text)
        val ratingText: TextView = itemView.findViewById(R.id.rating_text)
        val deliveryInfo: TextView = itemView.findViewById(R.id.delivery_info)
        val wishlistIcon: ImageView = itemView.findViewById(R.id.wishlist_icon)

        fun bind(product: Product) {
            productName.text = product.name
            
            // Show transfer price if available, otherwise regular price
            val displayPrice = if (product.transferPrice > 0) product.transferPrice else product.price
            currentPrice.text = "₹${String.format("%,d", displayPrice.toInt())}"
            ratingText.text = String.format("%.1f", product.rating)

            // Handle MRP vs Transfer Price or Original Price
            val comparePrice = if (product.mrp > 0 && product.mrp > displayPrice) {
                product.mrp
            } else if (product.originalPrice > displayPrice) {
                product.originalPrice
            } else {
                0.0
            }
            
            if (comparePrice > displayPrice) {
                originalPrice.visibility = View.VISIBLE
                originalPrice.text = "₹${String.format("%,d", comparePrice.toInt())}"
                originalPrice.paintFlags = originalPrice.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                
                discountPercent.visibility = View.VISIBLE
                discountText.visibility = View.VISIBLE
                val discount = ((comparePrice - displayPrice) / comparePrice * 100).toInt()
                discountPercent.text = "${discount}% off"
                discountText.text = "${discount}% off"
            } else {
                originalPrice.visibility = View.GONE
                discountPercent.visibility = View.GONE
                discountText.visibility = View.GONE
            }

            // Delivery info - Free delivery for orders above ₹2000
            deliveryInfo.text = if (displayPrice >= 2000) "Free delivery" else "Delivery ₹40"
            
            // Wishlist state
            updateWishlistIcon(product.id)

            // Load product image from URL (prioritize imageUrl from product data)
            val imageToLoad = if (!product.imageUrl.isNullOrEmpty()) {
                product.imageUrl
            } else if (!product.images.isNullOrEmpty() && product.images.isNotEmpty()) {
                product.images[0]
            } else {
                getTractorImageUrl(product)
            }
            
            // Check if it's an asset file (doesn't start with http)
            val imageUri = if (!imageToLoad.startsWith("http")) {
                "file:///android_asset/$imageToLoad"
            } else {
                imageToLoad
            }
            
            Glide.with(itemView.context)
                .load(imageUri)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .fallback(R.drawable.ic_launcher_background)
                .centerCrop()
                .into(productImage)

            // Click listeners
            itemView.setOnClickListener {
                val intent = Intent(itemView.context, ProductDetailActivity::class.java)
                intent.putExtra("product", product)
                itemView.context.startActivity(intent)
                onProductClick(product)
            }

            wishlistIcon.setOnClickListener {
                val added = WishlistManager.toggle(product.id)
                updateWishlistIcon(product.id)
                
                // Animate the icon
                wishlistIcon.animate()
                    .scaleX(1.3f)
                    .scaleY(1.3f)
                    .setDuration(150)
                    .withEndAction {
                        wishlistIcon.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(150)
                            .start()
                    }
                    .start()
                
                val message = if (added) "Added to wishlist" else "Removed from wishlist"
                Toast.makeText(itemView.context, message, Toast.LENGTH_SHORT).show()
                onFavoriteClick(product)
            }
        }
        
        private fun updateWishlistIcon(productId: Int) {
            if (WishlistManager.isInWishlist(productId)) {
                wishlistIcon.setColorFilter(android.graphics.Color.parseColor("#FF5722"))
            } else {
                wishlistIcon.setColorFilter(android.graphics.Color.parseColor("#878787"))
            }
        }
        
        private fun getTractorImageUrl(product: Product): String {
            // Use high-quality tractor and agricultural equipment images
            return when {
                product.category.contains("Agriculture Sprayers", ignoreCase = true) || 
                product.name.contains("sprayer", ignoreCase = true) -> 
                    "https://images.unsplash.com/photo-1544737151728-6e4c999de2ac?ixlib=rb-4.0.3&w=400&q=80"
                
                product.category.contains("Air Compressor", ignoreCase = true) || 
                product.name.contains("compressor", ignoreCase = true) -> 
                    "https://images.unsplash.com/photo-1579952363873-27d3bfad9c0d?ixlib=rb-4.0.3&w=400&q=80"
                
                product.category.contains("Chaff Cutter", ignoreCase = true) || 
                product.name.contains("chaff", ignoreCase = true) -> 
                    "https://images.unsplash.com/photo-1586953208448-b95a79798f07?ixlib=rb-4.0.3&w=400&q=80"
                
                product.category.contains("Earth Auger", ignoreCase = true) || 
                product.name.contains("auger", ignoreCase = true) -> 
                    "https://images.unsplash.com/photo-1621905251189-08b45d6a269e?ixlib=rb-4.0.3&w=400&q=80"
                
                product.category.contains("Water Pump", ignoreCase = true) || 
                product.name.contains("pump", ignoreCase = true) -> 
                    "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?ixlib=rb-4.0.3&w=400&q=80"
                
                product.category.contains("Welding", ignoreCase = true) || 
                product.name.contains("welding", ignoreCase = true) -> 
                    "https://images.unsplash.com/photo-1504328345606-18bbc8c9d7d1?ixlib=rb-4.0.3&w=400&q=80"
                
                product.category.contains("Pressure Washer", ignoreCase = true) || 
                product.name.contains("washer", ignoreCase = true) -> 
                    "https://images.unsplash.com/photo-1581833971358-2c8b550f87b3?ixlib=rb-4.0.3&w=400&q=80"
                
                product.category.contains("Electric Motor", ignoreCase = true) || 
                product.name.contains("motor", ignoreCase = true) -> 
                    "https://images.unsplash.com/photo-1615462035066-c96b6e5a7ad7?ixlib=rb-4.0.3&w=400&q=80"
                
                else -> "https://images.unsplash.com/photo-1574906435851-1edc57312bea?ixlib=rb-4.0.3&w=400&q=80" // Default tractor image
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_grid, parent, false)
        return GridProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: GridProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size
}