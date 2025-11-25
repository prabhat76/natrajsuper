package com.example.natraj

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView

class CategoryAdapter(
    categories: List<Category>,
    private val onCategoryClick: (Category) -> Unit = {}
) : ListAdapter<Category, CategoryAdapter.CategoryViewHolder>(DIFF) {

    private var selectedPosition: Int = RecyclerView.NO_POSITION

    init {
        submitList(categories)
        setHasStableIds(true)
    }
    
    fun updateCategories(newCategories: List<Category>) {
        submitList(newCategories)
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView.findViewById(R.id.category_card)
        private val categoryImage: ImageView = itemView.findViewById(R.id.category_image)
        private val categoryName: TextView = itemView.findViewById(R.id.category_name)
        private val specialBadge: TextView = itemView.findViewById(R.id.special_badge)
        private val productCount: TextView? = itemView.findViewById(R.id.product_count)

        fun bind(category: Category, position: Int) {
            categoryName.text = category.name
            itemView.contentDescription = category.name

            // Show special badge if category has special offers
            if (category.hasSpecialOffer) {
                specialBadge.visibility = View.VISIBLE
                specialBadge.text = "SALE"
            } else {
                specialBadge.visibility = View.GONE
            }

            // Product count (optional)
            productCount?.let { countView ->
                if (category.productCount > 0) {
                    countView.visibility = View.VISIBLE
                    countView.text = "${category.productCount} items"
                } else {
                    countView.visibility = View.GONE
                }
            }

            // Load category image with enhanced styling
            if (category.imageUrl.isNotEmpty()) {
                Glide.with(itemView)
                    .load(category.imageUrl)
                    .placeholder(getCategoryImageResId(category.name))
                    .error(getCategoryImageResId(category.name))
                    .centerCrop()
                    .into(categoryImage)
            } else {
                Glide.with(itemView)
                    .load(getCategoryImageResId(category.name))
                    .centerCrop()
                    .into(categoryImage)
            }

            // Click: navigate immediately, run animation purely for visual feedback
            itemView.setOnClickListener {
                val previous = selectedPosition
                selectedPosition = bindingAdapterPosition
                if (previous != RecyclerView.NO_POSITION) notifyItemChanged(previous)
                notifyItemChanged(selectedPosition)

                // Trigger navigation right away
                try {
                    onCategoryClick(category)
                } catch (e: Exception) {
                    android.util.Log.e("CategoryAdapter", "Error on category click", e)
                }
            }

            // Visual selection state
            val isSelected = position == selectedPosition
            card.strokeWidth = if (isSelected) 2 else 0
            card.strokeColor = itemView.resources.getColor(R.color.primary, itemView.context.theme)
            card.cardElevation = if (isSelected) 4f else 2f
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_enhanced, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    override fun getItemId(position: Int): Long = getItem(position).id.toLong()

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Category>() {
            override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean =
                oldItem == newItem
        }

        private fun getCategoryImageResId(categoryName: String): Int {
            val name = categoryName.lowercase()
            return when {
                name.contains("pump") -> R.drawable.ic_pump
                name.contains("engine") -> R.drawable.ic_engine
                name.contains("spray") -> R.drawable.ic_spray
                name.contains("auger") -> R.drawable.ic_auger
                name.contains("chaff") -> R.drawable.ic_chaff_cutter
                name.contains("compressor") -> R.drawable.ic_compressor
                name.contains("welding") -> R.drawable.ic_welding
                name.contains("washer") -> R.drawable.ic_washer
                else -> R.drawable.ic_category
            }
        }
    }
}
