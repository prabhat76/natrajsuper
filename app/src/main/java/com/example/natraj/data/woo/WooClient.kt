package com.example.natraj.data.woo

import android.content.Context
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

object WooClient {
    @Volatile private var retrofit: Retrofit? = null
    @Volatile private var cachedClient: OkHttpClient? = null

    fun api(context: Context): WooApi {
        val prefs = WooPrefs(context)
        val baseUrl = prefs.baseUrl
        require(!baseUrl.isNullOrBlank()) { "Woo base URL not configured" }

        val ck = prefs.consumerKey
        val cs = prefs.consumerSecret
        require(!ck.isNullOrBlank() && !cs.isNullOrBlank()) { "Woo credentials not configured" }

        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val originalUrl = original.url
            val newUrl: HttpUrl = originalUrl.newBuilder()
                .addQueryParameter("consumer_key", ck)
                .addQueryParameter("consumer_secret", cs)
                .build()
            val request: Request = original.newBuilder().url(newUrl).build()
            chain.proceed(request)
        }

        // Cache interceptor - cache GET requests for 5 minutes
        val cacheInterceptor = Interceptor { chain ->
            val request = chain.request()
            val response = chain.proceed(request)
            
            // Cache only GET requests
            if (request.method == "GET") {
                val cacheControl = CacheControl.Builder()
                    .maxAge(5, TimeUnit.MINUTES) // Cache for 5 minutes
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
            level = HttpLoggingInterceptor.Level.BODY // Changed to BODY to see error details
        }

        // Create cache directory (10 MB)
        val cacheDir = File(context.cacheDir, "http_cache")
        val cache = Cache(cacheDir, 10L * 1024 * 1024) // 10 MB

        val client = cachedClient ?: OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(authInterceptor)
            .addNetworkInterceptor(cacheInterceptor)
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS) // Faster timeout
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

        return r.create(WooApi::class.java)
    }

    private fun ensureTrailingSlash(url: String): String = if (url.endsWith("/")) url else "$url/"
}

class WooPrefs(private val context: Context) {
    private val sp = context.getSharedPreferences("woo_settings", Context.MODE_PRIVATE)

    var baseUrl: String?
        get() = sp.getString("base_url", null)
        set(value) = sp.edit().putString("base_url", value).apply()

    var consumerKey: String?
        get() = sp.getString("consumer_key", null)
        set(value) = sp.edit().putString("consumer_key", value).apply()

    var consumerSecret: String?
        get() = sp.getString("consumer_secret", null)
        set(value) = sp.edit().putString("consumer_secret", value).apply()

    // Dynamic theme colors
    var primaryColor: String?
        get() = sp.getString("primary_color", "#1976D2")
        set(value) = sp.edit().putString("primary_color", value).apply()

    var accentColor: String?
        get() = sp.getString("accent_color", "#2196F3")
        set(value) = sp.edit().putString("accent_color", value).apply()
}
