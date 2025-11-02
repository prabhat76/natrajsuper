package com.example.natraj.data.wp

import android.content.Context
import com.example.natraj.data.woo.WooPrefs
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object WpClient {
    @Volatile private var retrofit: Retrofit? = null

    fun api(context: Context): WpApi {
        val prefs = WooPrefs(context) // reuse same prefs base url
        val baseUrl = prefs.baseUrl ?: throw IllegalStateException("Base URL not configured")

        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        val client = OkHttpClient.Builder().addInterceptor(logging).build()

        val r = retrofit ?: Retrofit.Builder()
            .baseUrl(ensureTrailingSlash(baseUrl))
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .also { retrofit = it }

        return r.create(WpApi::class.java)
    }

    private fun ensureTrailingSlash(url: String): String = if (url.endsWith("/")) url else "$url/"
}
