package com.example.natraj

import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout

class BlogActivity : AppCompatActivity() {
    
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var tabLayout: TabLayout
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blog)
        
        setupToolbar()
        initializeViews()
        setupWebView()
        setupTabs()
        
        // Load initial URL (blog or specific post)
        val url = intent.getStringExtra("url") ?: "https://www.natrajsuper.com/blog/"
        webView.loadUrl(url)
    }
    
    private fun setupToolbar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Blog & Articles"
        }
    }
    
    private fun initializeViews() {
        webView = findViewById(R.id.blog_webview)
        progressBar = findViewById(R.id.progress_bar)
        tabLayout = findViewById(R.id.tab_layout)
    }
    
    private fun setupWebView() {
        webView.apply {
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    url?.let { view?.loadUrl(it) }
                    return true
                }
                
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    progressBar.visibility = View.GONE
                }
            }
            
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    progressBar.progress = newProgress
                    if (newProgress == 100) {
                        progressBar.visibility = View.GONE
                    } else {
                        progressBar.visibility = View.VISIBLE
                    }
                }
            }
            
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                builtInZoomControls = false
                displayZoomControls = false
                setSupportZoom(false)
                cacheMode = WebSettings.LOAD_DEFAULT
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
        }
    }
    
    private fun setupTabs() {
        // Add tabs for different sections
        tabLayout.addTab(tabLayout.newTab().setText("🏠 Home"))
        tabLayout.addTab(tabLayout.newTab().setText("📰 Blog"))
        tabLayout.addTab(tabLayout.newTab().setText("🛒 Shop"))
        tabLayout.addTab(tabLayout.newTab().setText("ℹ️ About"))
        tabLayout.addTab(tabLayout.newTab().setText("📞 Contact"))
        
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> webView.loadUrl("https://www.natrajsuper.com/")
                    1 -> webView.loadUrl("https://www.natrajsuper.com/blog/")
                    2 -> webView.loadUrl("https://www.natrajsuper.com/shop/")
                    3 -> webView.loadUrl("https://www.natrajsuper.com/about-us/")
                    4 -> webView.loadUrl("https://www.natrajsuper.com/contact-us/")
                }
                progressBar.visibility = View.VISIBLE
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
        
        // Set blog tab as default
        tabLayout.getTabAt(1)?.select()
    }
    
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
