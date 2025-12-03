package com.example.natraj

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView

class SimpleCategoryAdapter(
    private val categories: List<Category>,
    private val onCategoryClick: (Category) -> Unit
) : RecyclerView.Adapter<SimpleCategoryAdapter.CategoryViewHolder>() {

    private var selectedPosition = 0

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView as MaterialCardView
        private val categoryIcon: ImageView = itemView.findViewById(R.id.category_icon)
        private val categoryName: TextView = itemView.findViewById(R.id.category_name)
        private val selectionIndicator: View = itemView.findViewById(R.id.selection_indicator)
        private val offerBadge: View = itemView.findViewById(R.id.offer_badge)

        fun bind(category: Category, position: Int) {
            // Set category name with product count if available
            categoryName.text = if (category.productCount > 0) {
                "${category.name}\n(${category.productCount})"
            } else {
                category.name
            }
            
            // Load category image - prioritize imageResId over URL
            when {
                category.imageResId != 0 -> {
                    categoryIcon.setImageResource(category.imageResId)
                    categoryIcon.setColorFilter(null)
                }
                category.imageUrl.isNotEmpty() -> {
                    Glide.with(itemView.context)
                        .load(category.imageUrl)
                        .placeholder(getCategoryIcon(category.name))
                        .error(getCategoryIcon(category.name))
                        .centerCrop()
                        .into(categoryIcon)
                    categoryIcon.setColorFilter(null)
                }
                else -> {
                    categoryIcon.setImageResource(getCategoryIcon(category.name))
                    categoryIcon.setColorFilter(null)
                }
            }
            
            // Show selection state
            val isSelected = position == selectedPosition
            selectionIndicator.visibility = if (isSelected) View.VISIBLE else View.GONE
            
            // Animate card elevation for selected state
            card.cardElevation = if (isSelected) 6f else 3f
            
            // Show special offer badge and styling
            if (category.hasSpecialOffer) {
                offerBadge.visibility = View.VISIBLE
                card.strokeColor = itemView.context.getColor(R.color.orange_primary)
                card.strokeWidth = 2
            } else {
                offerBadge.visibility = View.GONE
                card.strokeWidth = 0
            }
            
            // Click handler
            itemView.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = adapterPosition
                
                // Notify changes for animation
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                
                // Scale animation
                itemView.animate()
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(100)
                    .withEndAction {
                        itemView.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(100)
                            .start()
                    }
                    .start()
                
                onCategoryClick(category)
            }
        }
        
        private fun getCategoryIcon(categoryName: String): Int {
            return when {
                categoryName.contains("Sprayer", ignoreCase = true) -> R.drawable.ic_spray
                categoryName.contains("Compressor", ignoreCase = true) -> R.drawable.ic_compressor
                categoryName.contains("Chaff", ignoreCase = true) -> R.drawable.ic_chaff_cutter
                categoryName.contains("Auger", ignoreCase = true) -> R.drawable.ic_auger
                categoryName.contains("Pump", ignoreCase = true) -> R.drawable.ic_pump
                categoryName.contains("Welding", ignoreCase = true) -> R.drawable.ic_welding
                categoryName.contains("Washer", ignoreCase = true) -> R.drawable.ic_washer
                categoryName.contains("Engine", ignoreCase = true) -> R.drawable.ic_engine
                categoryName == "All" -> R.drawable.ic_category
                else -> R.drawable.ic_category
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_simple, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position], position)
    }

    override fun getItemCount(): Int = categories.size
    
    fun setSelectedPosition(position: Int) {
        val previousPosition = selectedPosition
        selectedPosition = position
        notifyItemChanged(previousPosition)
        notifyItemChanged(selectedPosition)
    }
}
