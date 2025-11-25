package com.example.natraj.data

import android.content.Context

/**
 * Dynamic configuration class for the application
 * All hardcoded values should be moved here for easy configuration
 */
object AppConfig {
    private const val PREFS_NAME = "app_config"

    // Cache configuration
    const val DEFAULT_CACHE_TIMEOUT_MINUTES = 5L
    const val DEFAULT_HTTP_CACHE_SIZE_MB = 10L
    const val DEFAULT_HTTP_CACHE_MAX_AGE_MINUTES = 5L

    // API pagination defaults
    const val DEFAULT_CATEGORIES_PER_PAGE = 100
    const val DEFAULT_PRODUCTS_PER_PAGE = 20
    const val DEFAULT_ORDERS_PER_PAGE = 20
    const val DEFAULT_RECENT_ORDERS_LIMIT = 50
    const val DEFAULT_MEDIA_PER_PAGE = 20
    const val DEFAULT_BLOG_POSTS_LIMIT = 10

    // Payment defaults
    const val DEFAULT_PAYMENT_METHOD = "cod"
    const val DEFAULT_PAYMENT_TITLE = "Cash on Delivery"

    // HTTP timeouts
    const val DEFAULT_CONNECT_TIMEOUT_SECONDS = 15L
    const val DEFAULT_READ_TIMEOUT_SECONDS = 20L
    const val DEFAULT_WRITE_TIMEOUT_SECONDS = 20L

    // Default colors
    const val DEFAULT_PRIMARY_COLOR = "#1976D2"
    const val DEFAULT_ACCENT_COLOR = "#2196F3"

    // Contact information
    const val DEFAULT_WHATSAPP_NUMBER = "+911234567890"

    // Get dynamic values from preferences or use defaults
    fun getCacheTimeoutMinutes(context: Context): Long {
        return getLongPreference(context, "cache_timeout_minutes", DEFAULT_CACHE_TIMEOUT_MINUTES)
    }

    fun getHttpCacheSizeMB(context: Context): Long {
        return getLongPreference(context, "http_cache_size_mb", DEFAULT_HTTP_CACHE_SIZE_MB)
    }

    fun getHttpCacheMaxAgeMinutes(context: Context): Int {
        return getIntPreference(context, "http_cache_max_age_minutes", DEFAULT_HTTP_CACHE_MAX_AGE_MINUTES.toInt())
    }

    fun getCategoriesPerPage(context: Context): Int {
        return getIntPreference(context, "categories_per_page", DEFAULT_CATEGORIES_PER_PAGE)
    }

    fun getProductsPerPage(context: Context): Int {
        return getIntPreference(context, "products_per_page", DEFAULT_PRODUCTS_PER_PAGE)
    }

    fun getOrdersPerPage(context: Context): Int {
        return getIntPreference(context, "orders_per_page", DEFAULT_ORDERS_PER_PAGE)
    }

    fun getRecentOrdersLimit(context: Context): Int {
        return getIntPreference(context, "recent_orders_limit", DEFAULT_RECENT_ORDERS_LIMIT)
    }

    fun getMediaPerPage(context: Context): Int {
        return getIntPreference(context, "media_per_page", DEFAULT_MEDIA_PER_PAGE)
    }

    fun getBlogPostsLimit(context: Context): Int {
        return getIntPreference(context, "blog_posts_limit", DEFAULT_BLOG_POSTS_LIMIT)
    }

    fun getDefaultPaymentMethod(context: Context): String {
        return getStringPreference(context, "default_payment_method", DEFAULT_PAYMENT_METHOD)
    }

    fun getDefaultPaymentTitle(context: Context): String {
        return getStringPreference(context, "default_payment_title", DEFAULT_PAYMENT_TITLE)
    }

    fun getConnectTimeoutSeconds(context: Context): Long {
        return getLongPreference(context, "connect_timeout_seconds", DEFAULT_CONNECT_TIMEOUT_SECONDS)
    }

    fun getReadTimeoutSeconds(context: Context): Long {
        return getLongPreference(context, "read_timeout_seconds", DEFAULT_READ_TIMEOUT_SECONDS)
    }

    fun getWriteTimeoutSeconds(context: Context): Long {
        return getLongPreference(context, "write_timeout_seconds", DEFAULT_WRITE_TIMEOUT_SECONDS)
    }

    fun getPrimaryColor(context: Context): String {
        return getStringPreference(context, "primary_color", DEFAULT_PRIMARY_COLOR)
    }

    fun getAccentColor(context: Context): String {
        return getStringPreference(context, "accent_color", DEFAULT_ACCENT_COLOR)
    }

    fun getWhatsAppNumber(context: Context): String {
        return getStringPreference(context, "whatsapp_number", DEFAULT_WHATSAPP_NUMBER)
    }

    // Helper methods for preferences
    private fun getIntPreference(context: Context, key: String, defaultValue: Int): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(key, defaultValue)
    }

    private fun getLongPreference(context: Context, key: String, defaultValue: Long): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(key, defaultValue)
    }

    private fun getStringPreference(context: Context, key: String, defaultValue: String): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(key, defaultValue) ?: defaultValue
    }

    // Methods to update configuration values
    fun setCacheTimeoutMinutes(context: Context, value: Long) {
        setLongPreference(context, "cache_timeout_minutes", value)
    }

    fun setHttpCacheSizeMB(context: Context, value: Long) {
        setLongPreference(context, "http_cache_size_mb", value)
    }

    fun setHttpCacheMaxAgeMinutes(context: Context, value: Int) {
        setIntPreference(context, "http_cache_max_age_minutes", value)
    }

    fun setCategoriesPerPage(context: Context, value: Int) {
        setIntPreference(context, "categories_per_page", value)
    }

    fun setProductsPerPage(context: Context, value: Int) {
        setIntPreference(context, "products_per_page", value)
    }

    fun setOrdersPerPage(context: Context, value: Int) {
        setIntPreference(context, "orders_per_page", value)
    }

    fun setRecentOrdersLimit(context: Context, value: Int) {
        setIntPreference(context, "recent_orders_limit", value)
    }

    fun setMediaPerPage(context: Context, value: Int) {
        setIntPreference(context, "media_per_page", value)
    }

    fun setBlogPostsLimit(context: Context, value: Int) {
        setIntPreference(context, "blog_posts_limit", value)
    }

    fun setDefaultPaymentMethod(context: Context, value: String) {
        setStringPreference(context, "default_payment_method", value)
    }

    fun setDefaultPaymentTitle(context: Context, value: String) {
        setStringPreference(context, "default_payment_title", value)
    }

    fun setConnectTimeoutSeconds(context: Context, value: Long) {
        setLongPreference(context, "connect_timeout_seconds", value)
    }

    fun setReadTimeoutSeconds(context: Context, value: Long) {
        setLongPreference(context, "read_timeout_seconds", value)
    }

    fun setWriteTimeoutSeconds(context: Context, value: Long) {
        setLongPreference(context, "write_timeout_seconds", value)
    }

    fun setPrimaryColor(context: Context, value: String) {
        setStringPreference(context, "primary_color", value)
    }

    fun setAccentColor(context: Context, value: String) {
        setStringPreference(context, "accent_color", value)
    }

    fun setWhatsAppNumber(context: Context, value: String) {
        setStringPreference(context, "whatsapp_number", value)
    }

    private fun setIntPreference(context: Context, key: String, value: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(key, value).apply()
    }

    private fun setLongPreference(context: Context, key: String, value: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(key, value).apply()
    }

    private fun setStringPreference(context: Context, key: String, value: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(key, value).apply()
    }
}