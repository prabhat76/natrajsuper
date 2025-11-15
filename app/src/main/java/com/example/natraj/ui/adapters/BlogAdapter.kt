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
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip

class BlogAdapter(
    private val onBlogClick: (BlogPost) -> Unit
) : ListAdapter<BlogPost, BlogAdapter.BlogViewHolder>(BlogDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_blog_post, parent, false)
        return BlogViewHolder(view, onBlogClick)
    }

    override fun onBindViewHolder(holder: BlogViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class BlogViewHolder(
        itemView: View,
        private val onBlogClick: (BlogPost) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val blogImage: ImageView = itemView.findViewById(R.id.blog_image)
        private val categoryChip: Chip = itemView.findViewById(R.id.category_chip)
        private val blogTitle: TextView = itemView.findViewById(R.id.blog_title)
        private val blogExcerpt: TextView = itemView.findViewById(R.id.blog_excerpt)
        private val blogAuthor: TextView = itemView.findViewById(R.id.blog_author)
        private val blogDate: TextView = itemView.findViewById(R.id.blog_date)
        private val readMoreButton: MaterialButton = itemView.findViewById(R.id.read_more_button)

        fun bind(blogPost: BlogPost) {
            // Load image with Glide
            Glide.with(itemView.context)
                .load(blogPost.imageUrl)
                .placeholder(R.drawable.natraj_logo)
                .error(R.drawable.natraj_logo)
                .centerCrop()
                .into(blogImage)

            categoryChip.text = blogPost.category
            blogTitle.text = blogPost.title
            blogExcerpt.text = blogPost.excerpt
            blogAuthor.text = blogPost.author
            blogDate.text = blogPost.date

            // Click listeners
            itemView.setOnClickListener { onBlogClick(blogPost) }
            readMoreButton.setOnClickListener { onBlogClick(blogPost) }
        }
    }

    class BlogDiffCallback : DiffUtil.ItemCallback<BlogPost>() {
        override fun areItemsTheSame(oldItem: BlogPost, newItem: BlogPost): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: BlogPost, newItem: BlogPost): Boolean {
            return oldItem == newItem
        }
    }
}
