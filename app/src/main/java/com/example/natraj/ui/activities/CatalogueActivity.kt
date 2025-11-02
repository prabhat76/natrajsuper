package com.example.natraj

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.content.ContextCompat
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import android.provider.MediaStore
import android.content.ContentValues
import android.os.Build
import android.media.MediaScannerConnection
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class CatalogueActivity : AppCompatActivity(), OnPageChangeListener, OnLoadCompleteListener {

    private lateinit var toolbar: Toolbar
    private lateinit var pdfView: PDFView
    private lateinit var shareButton: Button
    private lateinit var downloadButton: Button
    private var pageNumber = 0
    private val REQ_WRITE_STORAGE = 201

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
        try {
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
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to load PDF: ${e.message}", Toast.LENGTH_LONG).show()
            android.util.Log.e("CatalogueActivity", "PDF load error", e)
        }
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
            val fileName = "Natraj_Catalogue.pdf"
            val inputStream = assets.open("natraj_catalogue.pdf")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Use MediaStore to save into public Downloads
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val resolver = contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: throw IllegalStateException("Unable to create download entry")

                resolver.openOutputStream(uri)?.use { out ->
                    inputStream.copyTo(out)
                }

                values.clear()
                values.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(uri, values, null, null)

                Toast.makeText(this, "Saved to Downloads/$fileName", Toast.LENGTH_LONG).show()
            } else {
                // Pre-Android 10: write to public Downloads folder, needs WRITE_EXTERNAL_STORAGE
                val hasPerm = ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED

                if (!hasPerm) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        REQ_WRITE_STORAGE
                    )
                    // Will retry after permission result
                    return
                }

                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val outFile = File(downloadsDir, fileName)
                FileOutputStream(outFile).use { out ->
                    inputStream.copyTo(out)
                }
                // Scan so it appears in file managers immediately
                MediaScannerConnection.scanFile(this, arrayOf(outFile.absolutePath), arrayOf("application/pdf"), null)
                Toast.makeText(this, "Saved to ${outFile.absolutePath}", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
            android.util.Log.e("CatalogueActivity", "Download failed", e)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_WRITE_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Try again now that we have permission
                downloadCatalogue()
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show()
            }
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
