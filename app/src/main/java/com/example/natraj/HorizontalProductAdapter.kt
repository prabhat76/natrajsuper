package com.example.natraj

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class HorizontalProductAdapter(
    private val products: List<Product>,
    private val onProductClick: (Product) -> Unit
) : RecyclerView.Adapter<HorizontalProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productImage: ImageView = view.findViewById(R.id.product_image)
        val productName: TextView = view.findViewById(R.id.product_name)
        val currentPrice: TextView = view.findViewById(R.id.current_price)
        val originalPrice: TextView = view.findViewById(R.id.original_price)
        val discountBadge: TextView = view.findViewById(R.id.discount_badge)
        val ratingText: TextView = view.findViewById(R.id.rating_text)
        val wishlistIcon: ImageView = view.findViewById(R.id.wishlist_icon)

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

                discountBadge.visibility = View.VISIBLE
                val discount = ((comparePrice - displayPrice) / comparePrice * 100).toInt()
                discountBadge.text = "${discount}% OFF"
            } else {
                originalPrice.visibility = View.GONE
                discountBadge.visibility = View.GONE
            }

            // Wishlist state
            updateWishlistIcon(product.id)

            // Load tractor/agricultural equipment images
            val imageToLoad = getTractorImageUrl(product)
            
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
                .transform(RoundedCorners(16))
                .into(productImage)

            // Click listeners
            itemView.setOnClickListener {
                val intent = android.content.Intent(itemView.context, ProductDetailActivity::class.java)
                intent.putExtra("product", product)
                itemView.context.startActivity(intent)
                onProductClick(product)
            }
            
            wishlistIcon.setOnClickListener {
                val added = WishlistManager.toggle(product.id)
                updateWishlistIcon(product.id)
            }
        }

        private fun getTractorImageUrl(product: Product): String {
            return when {
                !product.images.isNullOrEmpty() -> product.images.first()
                !product.imageUrl.isNullOrEmpty() -> product.imageUrl
                else -> when (product.category?.lowercase()) {
                    "tractors" -> "https://images.unsplash.com/photo-1574906435851-1edc57312bea?ixlib=rb-4.0.3&w=400&q=80"
                    "agriculture sprayers" -> "https://images.unsplash.com/photo-1544737151728-6e4c999de2ac?ixlib=rb-4.0.3&w=400&q=80"
                    "air compressors" -> "https://images.unsplash.com/photo-1579952363873-27d3bfad9c0d?ixlib=rb-4.0.3&w=400&q=80"
                    "chaff cutters" -> "https://images.unsplash.com/photo-1586953208448-b95a79798f07?ixlib=rb-4.0.3&w=400&q=80"
                    "earth augers" -> "https://images.unsplash.com/photo-1621905251189-08b45d6a269e?ixlib=rb-4.0.3&w=400&q=80"
                    "water pumps" -> "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?ixlib=rb-4.0.3&w=400&q=80"
                    "welding machines" -> "https://images.unsplash.com/photo-1504328345606-18bbc8c9d7d1?ixlib=rb-4.0.3&w=400&q=80"
                    "pressure washers" -> "https://images.unsplash.com/photo-1581833971358-2c8b550f87b3?ixlib=rb-4.0.3&w=400&q=80"
                    "electric motors" -> "https://images.unsplash.com/photo-1615462035066-c96b6e5a7ad7?ixlib=rb-4.0.3&w=400&q=80"
                    else -> "https://images.unsplash.com/photo-1574906435851-1edc57312bea?ixlib=rb-4.0.3&w=400&q=80"
                }
            }
        }

        private fun updateWishlistIcon(productId: Int) {
            if (WishlistManager.isInWishlist(productId)) {
                wishlistIcon.setImageResource(R.drawable.ic_favorite)
                wishlistIcon.setColorFilter(android.graphics.Color.RED)
            } else {
                wishlistIcon.setImageResource(R.drawable.ic_favorite_border)
                wishlistIcon.setColorFilter(android.graphics.Color.GRAY)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_horizontal, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount() = products.size
}