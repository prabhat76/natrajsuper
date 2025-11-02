package com.example.natraj

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.natraj.data.woo.WooPrefs

class WordPressSettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wordpress_settings)

        val prefs = WooPrefs(this)
        val baseUrl = findViewById<EditText>(R.id.wp_base_url)
        val ck = findViewById<EditText>(R.id.wp_consumer_key)
        val cs = findViewById<EditText>(R.id.wp_consumer_secret)
        val save = findViewById<Button>(R.id.wp_save_btn)

        baseUrl.setText(prefs.baseUrl ?: "https://www.natrajsuper.com")
        ck.setText(prefs.consumerKey ?: "")
        cs.setText(prefs.consumerSecret ?: "")

        save.setOnClickListener {
            prefs.baseUrl = baseUrl.text.toString().trim()
            prefs.consumerKey = ck.text.toString().trim()
            prefs.consumerSecret = cs.text.toString().trim()
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
