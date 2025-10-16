package com.example.natraj

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class CategoryAdapter(
    private val categories: List<Category>,
    private val onCategoryClick: (Category) -> Unit = {}
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryImage: ImageView = itemView.findViewById(R.id.category_image)
        private val categoryName: TextView = itemView.findViewById(R.id.category_name)
        private val specialBadge: TextView = itemView.findViewById(R.id.special_badge)

        fun bind(category: Category) {
            categoryName.text = category.name

            // Show special badge if category has special offers
            if (category.hasSpecialOffer) {
                specialBadge.visibility = View.VISIBLE
                specialBadge.text = "SALE"
            } else {
                specialBadge.visibility = View.GONE
            }

            // Load category image with enhanced styling
            if (category.imageUrl.isNotEmpty()) {
                // Check if it's an asset file (doesn't start with http)
                val imageUri = if (!category.imageUrl.startsWith("http")) {
                    "file:///android_asset/${category.imageUrl}"
                } else {
                    category.imageUrl
                }
                
                Glide.with(itemView.context)
                    .load(imageUri)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .transform(RoundedCorners(20))
                    .into(categoryImage)
            } else {
                categoryImage.setImageResource(category.imageResId)
            }

            // Enhanced click animation
            itemView.setOnClickListener { 
                // Add scale animation
                itemView.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction {
                        itemView.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(100)
                            .start()
                        onCategoryClick(category)
                    }
                    .start()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_enhanced, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size
}
