package com.example.natraj

import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.natraj.util.ThemeUtil
import com.google.android.material.appbar.MaterialToolbar

class BlogDetailActivity : AppCompatActivity() {
    
    private lateinit var toolbar: MaterialToolbar
    private lateinit var webView: WebView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blog_detail)
        
        val blogPost = intent.getSerializableExtra("blog_post") as? BlogPost
        if (blogPost == null) {
            finish()
            return
        }
        
        initializeViews()
        setupToolbar(blogPost.title)
        setupWebView()
        loadBlogContent(blogPost)
    }
    
    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        webView = findViewById(R.id.webview)
    }
    
    private fun setupToolbar(title: String) {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setTitle(title)
        }
        ThemeUtil.applyToolbarColor(toolbar, this)
    }
    
    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = false
            domStorageEnabled = true
        }
        webView.webViewClient = WebViewClient()
    }
    
    private fun loadBlogContent(blogPost: BlogPost) {
        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        padding: 16px;
                        margin: 0;
                        background: #ffffff;
                        color: #333333;
                        line-height: 1.6;
                    }
                    img {
                        max-width: 100%;
                        height: auto;
                        border-radius: 8px;
                        margin: 16px 0;
                    }
                    .meta {
                        color: #666666;
                        font-size: 14px;
                        margin-bottom: 16px;
                        padding-bottom: 16px;
                        border-bottom: 1px solid #eeeeee;
                    }
                    .category {
                        background: #1976D2;
                        color: white;
                        padding: 4px 12px;
                        border-radius: 4px;
                        font-size: 12px;
                        display: inline-block;
                        margin-right: 8px;
                    }
                    h1, h2, h3, h4 {
                        color: #2874A6;
                        margin-top: 24px;
                    }
                    p {
                        margin: 16px 0;
                    }
                    a {
                        color: #2874A6;
                        text-decoration: none;
                    }
                    pre {
                        background: #f5f5f5;
                        padding: 12px;
                        border-radius: 4px;
                        overflow-x: auto;
                    }
                    blockquote {
                        border-left: 4px solid #1976D2;
                        padding-left: 16px;
                        margin: 16px 0;
                        color: #555555;
                        font-style: italic;
                    }
                </style>
            </head>
            <body>
                <div class="meta">
                    <span class="category">${blogPost.category}</span>
                    <span>By ${blogPost.author}</span> · 
                    <span>${blogPost.date.substringBefore('T')}</span>
                </div>
                ${if (blogPost.imageUrl.isNotEmpty()) """<img src="${blogPost.imageUrl}" alt="Featured Image">""" else ""}
                ${blogPost.content}
            </body>
            </html>
        """.trimIndent()
        
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
