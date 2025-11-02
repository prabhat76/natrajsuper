package com.example.natraj.data.woo

import retrofit2.http.*

interface WooApi {
    // Categories with images
    @GET("wp-json/wc/v3/products/categories")
    suspend fun getCategories(
        @Query("per_page") perPage: Int = 100,
        @Query("page") page: Int = 1,
        @Query("hide_empty") hideEmpty: Boolean = false
    ): List<WooCategory>

    // Products with filters
    @GET("wp-json/wc/v3/products")
    suspend fun getProducts(
        @Query("per_page") perPage: Int = 20,
        @Query("page") page: Int = 1,
        @Query("category") category: Int? = null,
        @Query("min_price") minPrice: Int? = null,
        @Query("max_price") maxPrice: Int? = null,
        @Query("attribute") attribute: Int? = null,
        @Query("attribute_term") attributeTerm: Int? = null,
        @Query("featured") featured: Boolean? = null,
        @Query("orderby") orderBy: String? = null,
        @Query("order") order: String? = null
    ): List<WooProduct>

    // Attributes and terms for filters
    @GET("wp-json/wc/v3/products/attributes")
    suspend fun getAttributes(): List<WooAttribute>

    @GET("wp-json/wc/v3/products/attributes/{id}/terms")
    suspend fun getAttributeTerms(@Path("id") id: Int): List<WooAttributeTerm>

    // Orders
    @POST("wp-json/wc/v3/orders")
    suspend fun createOrder(@Body body: WooCreateOrderRequest): WooOrderResponse
}
