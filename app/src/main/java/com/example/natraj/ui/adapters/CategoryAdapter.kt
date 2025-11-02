package com.example.natraj

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class CategoryAdapter(
    categories: List<Category>,
    private val onCategoryClick: (Category) -> Unit = {}
) : ListAdapter<Category, CategoryAdapter.CategoryViewHolder>(DIFF) {

    private var selectedPosition: Int = RecyclerView.NO_POSITION

    init {
        submitList(categories)
        setHasStableIds(true)
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
                // Check if it's an asset file (doesn't start with http)
                val imageUri = if (!category.imageUrl.startsWith("http")) {
                    "file:///android_asset/${category.imageUrl}"
                } else {
                    category.imageUrl
                }
                
                Glide.with(itemView.context)
                    .load(imageUri)
                    .placeholder(R.drawable.ic_category)
                    .error(R.drawable.ic_category)
                    .transform(RoundedCorners(20))
                    .into(categoryImage)
            } else {
                val resId = if (category.imageResId != 0) category.imageResId else R.drawable.ic_category
                categoryImage.setImageResource(resId)
            }

            // Enhanced click animation
            itemView.setOnClickListener { 
                val previous = selectedPosition
                selectedPosition = bindingAdapterPosition
                if (previous != RecyclerView.NO_POSITION) notifyItemChanged(previous)
                notifyItemChanged(selectedPosition)

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
    }
}
