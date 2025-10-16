package com.example.natraj

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton

class Product360Activity : AppCompatActivity() {
    private var isAutoRotating = false
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var viewpager: ViewPager2
    private lateinit var frameCounter: TextView
    private lateinit var autoRotateBtn: MaterialButton
    
    private val autoRotateRunnable = object : Runnable {
        override fun run() {
            if (isAutoRotating) {
                val nextItem = (viewpager.currentItem + 1) % (viewpager.adapter?.itemCount ?: 1)
                viewpager.setCurrentItem(nextItem, true)
                handler.postDelayed(this, 150) // Auto rotate every 150ms
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_360)

        val images = intent.getStringArrayListExtra("images") ?: arrayListOf()
        viewpager = findViewById(R.id.viewpager_360)
        val close = findViewById<ImageView>(R.id.close_360)
        frameCounter = findViewById(R.id.frame_counter)
        autoRotateBtn = findViewById(R.id.auto_rotate_btn)

        viewpager.adapter = Product360Adapter(images)
        
        // Update frame counter
        viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                frameCounter.text = "${position + 1}/${images.size}"
            }
        })
        
        // Initialize counter
        frameCounter.text = "1/${images.size}"

        close.setOnClickListener { finish() }
        
        autoRotateBtn.setOnClickListener {
            isAutoRotating = !isAutoRotating
            if (isAutoRotating) {
                autoRotateBtn.text = "Stop Auto"
                autoRotateBtn.setIconResource(android.R.drawable.ic_media_pause)
                handler.post(autoRotateRunnable)
            } else {
                autoRotateBtn.text = "Auto Rotate"
                autoRotateBtn.setIconResource(android.R.drawable.ic_media_play)
                handler.removeCallbacks(autoRotateRunnable)
            }
        }
    }
    
    override fun onDestroy() {
        handler.removeCallbacks(autoRotateRunnable)
        super.onDestroy()
    }
}
