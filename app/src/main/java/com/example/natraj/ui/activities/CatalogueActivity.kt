package com.example.natraj

import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceError
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class CatalogueActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalogue)

        initializeViews()
        setupToolbar()
        setupWebView()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        webView = findViewById(R.id.catalogue_webview)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "E-Catalogue"
        }
    }

    private fun setupWebView() {
        try {
            webView.settings.javaScriptEnabled = true
            webView.settings.domStorageEnabled = true
            webView.settings.loadWithOverviewMode = true
            webView.settings.useWideViewPort = true
            webView.settings.builtInZoomControls = true
            webView.settings.displayZoomControls = false

            webView.webViewClient = object : WebViewClient() {
                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                    super.onReceivedError(view, request, error)
                    // Handle error, perhaps show a message
                    android.util.Log.e("CatalogueActivity", "WebView error: ${error?.description}")
                }
            }
            webView.loadUrl("https://www.natrajsuper.com/e-catalogue/")
        } catch (e: Exception) {
            android.util.Log.e("CatalogueActivity", "Error setting up WebView", e)
            // Fallback or show error
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    finish()
                }
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
