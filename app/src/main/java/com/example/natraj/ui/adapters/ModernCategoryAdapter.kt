package com.example.natraj

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip

class ModernCategoryAdapter(
    private val categories: List<Category>,
    private val productCounts: Map<String, Int> = emptyMap(),
    private val onCategoryClick: (Category) -> Unit
) : RecyclerView.Adapter<ModernCategoryAdapter.CategoryViewHolder>() {

    private var selectedPosition = 0

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView as MaterialCardView
        private val categoryIcon: ImageView = itemView.findViewById(R.id.category_icon)
        private val categoryName: TextView = itemView.findViewById(R.id.category_name)
        private val categoryCount: Chip = itemView.findViewById(R.id.category_count)
        private val selectionIndicator: View = itemView.findViewById(R.id.selection_indicator)
        private val iconContainer: View = itemView.findViewById(R.id.category_icon_container)

        fun bind(category: Category, position: Int) {
            categoryName.text = category.name
            
            // Set product count
            val count = productCounts[category.name] ?: 0
            categoryCount.text = "$count items"
            
            // Load icon - use placeholder or category-specific icon
            val iconRes = getCategoryIcon(category.name)
            categoryIcon.setImageResource(iconRes)
            
            // Show selection state
            val isSelected = position == selectedPosition
            selectionIndicator.visibility = if (isSelected) View.VISIBLE else View.GONE
            
            // Animate card elevation for selected state
            card.cardElevation = if (isSelected) 8f else 4f
            
            // Scale animation on click
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
            .inflate(R.layout.item_category_modern, parent, false)
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
