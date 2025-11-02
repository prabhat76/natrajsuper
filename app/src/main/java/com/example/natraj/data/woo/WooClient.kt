package com.example.natraj.data.woo

import android.content.Context
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object WooClient {
    @Volatile private var retrofit: Retrofit? = null

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

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()

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
}
