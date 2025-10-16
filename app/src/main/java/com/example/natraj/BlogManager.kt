package com.example.natraj

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken

object BlogManager {
    private var blogPosts: List<BlogPost> = emptyList()
    
    fun initialize(context: Context) {
        val json = context.assets.open("blog_posts.json").bufferedReader().use { it.readText() }
        val jsonObject = Gson().fromJson(json, JsonObject::class.java)
        
        val postsArray = jsonObject.getAsJsonArray("blogPosts")
        val postType = object : TypeToken<List<BlogPost>>() {}.type
        blogPosts = Gson().fromJson(postsArray, postType)
        
        android.util.Log.d("BlogManager", "Loaded ${blogPosts.size} blog posts")
    }
    
    fun getAllBlogPosts(): List<BlogPost> = blogPosts
    
    fun getRecentPosts(limit: Int = 5): List<BlogPost> = blogPosts.take(limit)
    
    fun getPostsByCategory(category: String): List<BlogPost> = 
        blogPosts.filter { it.category == category }
}
