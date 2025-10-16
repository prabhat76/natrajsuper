package com.example.natraj

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class BlogAdapter(
    private val blogPosts: List<BlogPost>,
    private val onBlogClick: (BlogPost) -> Unit = {}
) : RecyclerView.Adapter<BlogAdapter.BlogViewHolder>() {

    inner class BlogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val blogImage: ImageView = itemView.findViewById(R.id.blog_image)
        private val blogTitle: TextView = itemView.findViewById(R.id.blog_title)
        private val blogExcerpt: TextView = itemView.findViewById(R.id.blog_excerpt)
        private val blogCategory: TextView = itemView.findViewById(R.id.blog_category)
        private val blogDate: TextView = itemView.findViewById(R.id.blog_date)

        fun bind(post: BlogPost) {
            blogTitle.text = post.title
            blogExcerpt.text = post.excerpt
            blogCategory.text = post.category
            blogDate.text = post.date
            
            // Load image with Glide
            Glide.with(itemView.context)
                .load(post.imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .into(blogImage)

            itemView.setOnClickListener { 
                // Open in BlogActivity instead of external browser
                val intent = Intent(itemView.context, BlogActivity::class.java)
                intent.putExtra("url", post.url)
                itemView.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_blog, parent, false)
        return BlogViewHolder(view)
    }

    override fun onBindViewHolder(holder: BlogViewHolder, position: Int) {
        holder.bind(blogPosts[position])
    }

    override fun getItemCount(): Int = blogPosts.size
}
