package com.example.natraj

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.natraj.util.CustomToast

class GridProductAdapter(
    private var products: MutableList<Product> = mutableListOf(),
    private val onProductClick: (Product) -> Unit = {},
    private val onFavoriteClick: (Product) -> Unit = {},
    private val onAddToCart: (Product) -> Unit = {}
) : RecyclerView.Adapter<GridProductAdapter.GridProductViewHolder>() {

    inner class GridProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.product_image)
        val productName: TextView = itemView.findViewById(R.id.product_name)
        val currentPrice: TextView = itemView.findViewById(R.id.current_price)
        val originalPrice: TextView = itemView.findViewById(R.id.original_price)
        val discountBadge: TextView = itemView.findViewById(R.id.discount_badge)
        val discountText: TextView = itemView.findViewById(R.id.discount_text)
        val ratingText: TextView = itemView.findViewById(R.id.rating_text)
        val ratingContainer: View = itemView.findViewById(R.id.rating_container)
        val stockStatus: TextView = itemView.findViewById(R.id.stock_status)
        val newBadge: TextView = itemView.findViewById(R.id.new_badge)
        val wishlistIcon: ImageView = itemView.findViewById(R.id.wishlist_icon)
        val addToCartButton: Button = itemView.findViewById(R.id.add_to_cart_button)
        val discountLayout: View = itemView.findViewById(R.id.discount_layout)

        fun bind(product: Product) {
            productName.text = product.name
            
            // Show transfer price if available, otherwise regular price
            val displayPrice = if (product.transferPrice > 0) product.transferPrice else product.price
            currentPrice.text = "₹${String.format("%,d", displayPrice.toInt())}"
            
            // Rating
            if (product.rating > 0) {
                ratingText.text = String.format("%.1f", product.rating)
                ratingContainer.visibility = View.VISIBLE
            } else {
                ratingContainer.visibility = View.GONE
            }

            // Stock status
            stockStatus.text = if (product.inventory > 0) "In stock" else "Out of stock"
            stockStatus.setTextColor(
                if (product.inventory > 0) 
                    itemView.context.getColor(R.color.stock_green)
                else
                    itemView.context.getColor(R.color.discount_badge_red)
            )

            // Handle MRP vs Transfer Price or Original Price
            val comparePrice = if (product.mrp > 0 && product.mrp > displayPrice) {
                product.mrp
            } else if (product.originalPrice > displayPrice) {
                product.originalPrice
            } else {
                0.0
            }
            
            if (comparePrice > displayPrice) {
                discountLayout.visibility = View.VISIBLE
                originalPrice.text = "₹${String.format("%,d", comparePrice.toInt())}"
                originalPrice.paintFlags = originalPrice.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                
                val discount = ((comparePrice - displayPrice) / comparePrice * 100).toInt()
                discountBadge.visibility = View.VISIBLE
                discountBadge.text = "-${discount}%"
                discountText.text = "${discount}% OFF"
            } else {
                discountLayout.visibility = View.GONE
                discountBadge.visibility = View.GONE
            }

            // NEW badge - show for products added in last 30 days (if you have creation date)
            newBadge.visibility = View.GONE // Can be enabled based on product.createdDate
            
            // Wishlist state
            updateWishlistIcon(product.id)

            // Load product image with better quality and caching
            val imageToLoad = when {
                !product.images.isNullOrEmpty() -> product.images.first()
                !product.imageUrl.isNullOrEmpty() -> product.imageUrl
                else -> ""
            }
            val imageUri = if (imageToLoad.isNotEmpty() && !imageToLoad.startsWith("http")) {
                "file:///android_asset/$imageToLoad"
            } else imageToLoad

            Glide.with(itemView.context)
                .load(imageUri)
                .placeholder(R.color.background_light)
                .error(R.color.background_light)
                .fallback(R.color.background_light)
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                .centerCrop()
                .into(productImage)

            // Click listeners
            itemView.setOnClickListener {
                onProductClick(product)
            }

            wishlistIcon.setOnClickListener {
                val added = WishlistManager.toggle(product.id)
                updateWishlistIcon(product.id)
                
                val message = if (added) "Added to wishlist" else "Removed from wishlist"
                CustomToast.showInfo(itemView.context, message)
                onFavoriteClick(product)
            }

            // Add to Cart button
            addToCartButton.setOnClickListener {
                if (product.inventory > 0) {
                    CartManager.add(product, 1)

                    CustomToast.showSuccess(itemView.context, "Added to cart")
                    onAddToCart(product)
                } else {
                    CustomToast.showError(itemView.context, "Product out of stock")
                }
            }
        }
        
        private fun updateWishlistIcon(productId: Int) {
            if (WishlistManager.isInWishlist(productId)) {
                wishlistIcon.setColorFilter(android.graphics.Color.parseColor("#FF5722"))
            } else {
                wishlistIcon.setColorFilter(android.graphics.Color.parseColor("#878787"))
            }
        }
        
        // Removed external mock image fallbacks; rely on product image data or placeholders
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

    // Update entire product list (for initial load or filtering)
    fun update(newProducts: List<Product>) {
        products.clear()
        products.addAll(newProducts)
        notifyDataSetChanged()
    }

    // Append products for pagination
    fun append(newProducts: List<Product>) {
        val startPosition = products.size
        products.addAll(newProducts)
        notifyItemRangeInserted(startPosition, newProducts.size)
    }
}