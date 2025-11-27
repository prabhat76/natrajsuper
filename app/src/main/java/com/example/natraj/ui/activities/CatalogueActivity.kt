package com.example.natraj.ui.activities

import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.natraj.R

class CatalogueActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var toolbar: Toolbar

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
            title = "Catalogue"
        }
    }

    private fun setupWebView() {
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                android.util.Log.d("CatalogueActivity", "WebView page loaded: $url")
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                super.onReceivedError(view, request, error)
                android.util.Log.e("CatalogueActivity", "WebView error: ${error.description}")
                runOnUiThread {
                    android.widget.Toast.makeText(
                        this@CatalogueActivity,
                        "Failed to load catalogue. Please try again later.",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            }
        }

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true

        // Load the catalogue URL
        webView.loadUrl("https://www.natrajsuper.com/e-catalogue/")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
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
