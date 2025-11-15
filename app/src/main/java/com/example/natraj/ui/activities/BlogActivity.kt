package com.example.natraj

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.natraj.util.ThemeUtil
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout

class BlogActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var toolbar: MaterialToolbar
    private lateinit var categoryTabs: TabLayout
    private lateinit var emptyState: View
    
    private lateinit var blogAdapter: BlogAdapter
    private var allBlogPosts: List<BlogPost> = emptyList()
    private var currentCategory: String = "All"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blog_native)
        
        initializeViews()
        setupToolbar()
        setupRecyclerView()
        setupSwipeRefresh()
        setupCategoryTabs()
        loadBlogPosts()
    }
    
    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.blog_recycler_view)
        swipeRefresh = findViewById(R.id.swipe_refresh)
        categoryTabs = findViewById(R.id.category_tabs)
        emptyState = findViewById(R.id.empty_state)
    }
    
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Blog & Articles"
        }
        toolbar.setNavigationOnClickListener {
            finish()
        }
        ThemeUtil.applyToolbarColor(toolbar, this)
    }
    
    private fun setupRecyclerView() {
        blogAdapter = BlogAdapter { blogPost ->
            // Open blog detail activity with full content
            val intent = Intent(this, BlogDetailActivity::class.java)
            intent.putExtra("blog_post", blogPost)
            startActivity(intent)
        }
        recyclerView.adapter = blogAdapter
    }
    
    private fun setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener {
            loadBlogPosts()
            swipeRefresh.isRefreshing = false
        }
    }
    
    private fun setupCategoryTabs() {
        // Get unique categories
        val categories = mutableSetOf("All")
        categories.addAll(BlogManager.getAllBlogPosts().map { it.category })
        
        // Add tabs
        categories.forEach { category ->
            categoryTabs.addTab(categoryTabs.newTab().setText(category))
        }
        
        categoryTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentCategory = tab?.text?.toString() ?: "All"
                filterBlogPosts()
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
    
    private fun loadBlogPosts() {
        allBlogPosts = BlogManager.getAllBlogPosts()
        filterBlogPosts()
    }
    
    private fun filterBlogPosts() {
        val filteredPosts = if (currentCategory == "All") {
            allBlogPosts
        } else {
            allBlogPosts.filter { it.category == currentCategory }
        }
        
        if (filteredPosts.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyState.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            blogAdapter.submitList(filteredPosts)
        }
    }
}
