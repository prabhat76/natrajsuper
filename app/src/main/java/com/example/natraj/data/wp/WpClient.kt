package com.example.natraj.data.wp

import android.content.Context
import com.example.natraj.data.woo.WooPrefs
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

object WpClient {
    @Volatile private var retrofit: Retrofit? = null
    @Volatile private var cachedClient: OkHttpClient? = null

    fun api(context: Context): WpApi {
        val prefs = WooPrefs(context) // reuse same prefs base url
        val baseUrl = prefs.baseUrl ?: throw IllegalStateException("Base URL not configured")

        // Cache interceptor for GET requests
        val cacheInterceptor = Interceptor { chain ->
            val request = chain.request()
            val response = chain.proceed(request)
            
            if (request.method == "GET") {
                val cacheControl = CacheControl.Builder()
                    .maxAge(5, TimeUnit.MINUTES)
                    .build()
                response.newBuilder()
                    .header("Cache-Control", cacheControl.toString())
                    .removeHeader("Pragma")
                    .build()
            } else {
                response
            }
        }

        val logging = HttpLoggingInterceptor().apply { 
            level = HttpLoggingInterceptor.Level.BASIC 
        }
        
        val cacheDir = File(context.cacheDir, "wp_http_cache")
        val cache = Cache(cacheDir, 10L * 1024 * 1024)
        
        val client = cachedClient ?: OkHttpClient.Builder()
            .cache(cache)
            .addNetworkInterceptor(cacheInterceptor)
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
            .also { cachedClient = it }

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
