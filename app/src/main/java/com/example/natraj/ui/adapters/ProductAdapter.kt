package com.example.natraj

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.natraj.data.model.Product

class ProductAdapter(
    private val products: List<Product>,
    private val onProductClick: (Product) -> Unit = {},
    private val onAddToCart: (Product) -> Unit = {},
    private val onFavoriteClick: (Product) -> Unit = {}
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.product_image)
        val productName: TextView = itemView.findViewById(R.id.product_name)
        val currentPrice: TextView = itemView.findViewById(R.id.current_price)
        val originalPrice: TextView = itemView.findViewById(R.id.original_price)
        val discountPercent: TextView = itemView.findViewById(R.id.discount_percent)
        val ratingText: TextView = itemView.findViewById(R.id.rating_text)
        val deliveryInfo: TextView = itemView.findViewById(R.id.delivery_info)
        val wishlistIcon: ImageView = itemView.findViewById(R.id.wishlist_icon)

        fun bind(product: Product) {
            productName.text = product.name
            
            // Show transfer price if available, otherwise regular price
            val displayPrice = if (product.transferPrice > 0) product.transferPrice else product.price
            currentPrice.text = "₹${displayPrice.toInt()}"
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
                originalPrice.text = "₹${comparePrice.toInt()}"
                originalPrice.paintFlags = originalPrice.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                
                discountPercent.visibility = View.VISIBLE
                val discount = ((comparePrice - displayPrice) / comparePrice * 100).toInt()
                discountPercent.text = "${discount}% off"
            } else {
                originalPrice.visibility = View.GONE
                discountPercent.visibility = View.GONE
            }

            // Delivery info - Free delivery for orders above ₹2000
            deliveryInfo.text = if (displayPrice >= 2000) "Free delivery" else "Delivery ₹40"
            
            // Wishlist state
            updateWishlistIcon(product.id)

            // Load first image from images list if available
            val imageToLoad = if (!product.images.isNullOrEmpty()) product.images[0] else product.imageUrl
            Glide.with(itemView.context)
                .load(imageToLoad)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .fallback(R.drawable.ic_launcher_background)
                .centerCrop()
                .into(productImage)

            // Flipkart style - entire card is clickable to view details
            itemView.setOnClickListener {
                val ctx = itemView.context
                val intent = Intent(ctx, ProductDetailActivity::class.java)
                intent.putExtra("product", product)
                ctx.startActivity(intent)
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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size
}
