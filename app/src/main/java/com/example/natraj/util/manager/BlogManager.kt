package com.example.natraj

import android.content.Context
import com.example.natraj.data.repository.WpRepository
import com.example.natraj.data.AppConfig
import kotlinx.coroutines.runBlocking

object BlogManager {
    private var blogPosts: List<BlogPost> = emptyList()
    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return

        try {
            // Try to fetch from WordPress API first
            runBlocking {
                val wpRepository = WpRepository(context)
                blogPosts = wpRepository.getRecentPosts(limit = AppConfig.getBlogPostsLimit(context))
                isInitialized = true
                android.util.Log.d("BlogManager", "Loaded ${blogPosts.size} blog posts from WordPress")
            }
        } catch (e: Exception) {
            android.util.Log.w("BlogManager", "Failed to load from WordPress, using fallback", e)
            // Fallback to JSON if WordPress fails
            loadFromJsonFallback(context)
        }
    }

    private fun loadFromJsonFallback(context: Context) {
        try {
            val json = context.assets.open("blog_posts.json").bufferedReader().use { it.readText() }
            val jsonObject = com.google.gson.Gson().fromJson(json, com.google.gson.JsonObject::class.java)

            val postsArray = jsonObject.getAsJsonArray("blogPosts")
            val postType = com.google.gson.reflect.TypeToken.getParameterized(java.util.List::class.java, BlogPost::class.java).type
            blogPosts = com.google.gson.Gson().fromJson(postsArray, postType)

            isInitialized = true
            android.util.Log.d("BlogManager", "Loaded ${blogPosts.size} blog posts from JSON fallback")
        } catch (e: Exception) {
            android.util.Log.e("BlogManager", "Failed to load blog posts from JSON", e)
            blogPosts = emptyList()
        }
    }

    fun getAllBlogPosts(): List<BlogPost> = blogPosts

    fun getRecentPosts(limit: Int = AppConfig.DEFAULT_BLOG_POSTS_LIMIT): List<BlogPost> = blogPosts.take(limit)

    fun getPostsByCategory(category: String): List<BlogPost> =
        blogPosts.filter { it.category.equals(category, ignoreCase = true) }

    fun refreshPosts(context: Context) {
        isInitialized = false
        initialize(context)
    }
}
