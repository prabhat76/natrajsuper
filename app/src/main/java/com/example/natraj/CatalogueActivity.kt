package com.example.natraj

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import java.io.File
import java.io.FileOutputStream

class CatalogueActivity : AppCompatActivity(), OnPageChangeListener, OnLoadCompleteListener {

    private lateinit var toolbar: Toolbar
    private lateinit var pdfView: PDFView
    private lateinit var shareButton: Button
    private lateinit var downloadButton: Button
    private var pageNumber = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalogue)

        initializeViews()
        setupToolbar()
        loadPDF()
        setupButtons()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        pdfView = findViewById(R.id.pdfView)
        shareButton = findViewById(R.id.share_button)
        downloadButton = findViewById(R.id.download_button)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Product Catalogue"
        }
    }

    private fun loadPDF() {
        pdfView.fromAsset("natraj_catalogue.pdf")
            .defaultPage(pageNumber)
            .onPageChange(this)
            .enableAnnotationRendering(true)
            .onLoad(this)
            .scrollHandle(DefaultScrollHandle(this))
            .spacing(10) // in dp
            .enableSwipe(true)
            .swipeHorizontal(false)
            .enableDoubletap(true)
            .pageFitPolicy(com.github.barteksc.pdfviewer.util.FitPolicy.WIDTH)
            .load()
    }

    override fun onPageChanged(page: Int, pageCount: Int) {
        pageNumber = page
        supportActionBar?.title = "Catalogue (${page + 1}/$pageCount)"
    }

    override fun loadComplete(nbPages: Int) {
        Toast.makeText(this, "Catalogue loaded successfully!", Toast.LENGTH_SHORT).show()
    }

    private fun setupButtons() {
        downloadButton.setOnClickListener {
            downloadCatalogue()
        }

        shareButton.setOnClickListener {
            shareCatalogue()
        }
    }

    private fun downloadCatalogue() {
        try {
            val inputStream = assets.open("natraj_catalogue.pdf")
            val downloadsDir = getExternalFilesDir(null)
            val outputFile = File(downloadsDir, "Natraj_Catalogue.pdf")

            FileOutputStream(outputFile).use { output ->
                inputStream.copyTo(output)
            }

            Toast.makeText(
                this,
                "Catalogue saved to: ${outputFile.absolutePath}",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun shareCatalogue() {
        try {
            // Copy PDF to cache for sharing
            val inputStream = assets.open("natraj_catalogue.pdf")
            val cacheFile = File(cacheDir, "Natraj_Catalogue.pdf")

            FileOutputStream(cacheFile).use { output ->
                inputStream.copyTo(output)
            }

            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                cacheFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Natraj Product Catalogue")
                putExtra(Intent.EXTRA_TEXT, "Check out our latest product catalogue!")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, "Share Catalogue via"))
        } catch (e: Exception) {
            Toast.makeText(this, "Share failed: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
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
}
