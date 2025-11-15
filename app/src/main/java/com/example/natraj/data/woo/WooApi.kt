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
    @GET("wp-json/wc/v3/orders")
    suspend fun getOrders(
        @Query("per_page") perPage: Int = 20,
        @Query("page") page: Int = 1,
        @Query("customer") customerId: Int? = null,
        @Query("status") status: String? = null,
        @Query("orderby") orderBy: String = "date",
        @Query("order") order: String = "desc"
    ): List<WooOrderResponse>
    
    @POST("wp-json/wc/v3/orders")
    suspend fun createOrder(@Body body: WooCreateOrderRequest): WooOrderResponse
    
    @GET("wp-json/wc/v3/orders/{id}")
    suspend fun getOrder(@Path("id") orderId: Int): WooOrderResponse
    
    @PUT("wp-json/wc/v3/orders/{id}")
    suspend fun updateOrder(@Path("id") orderId: Int, @Body body: WooOrderUpdateRequest): WooOrderResponse
    
    // Payment Gateways
    @GET("wp-json/wc/v3/payment_gateways")
    suspend fun getPaymentGateways(): List<WooPaymentGateway>
    
    @GET("wp-json/wc/v3/payment_gateways/{id}")
    suspend fun getPaymentGateway(@Path("id") gatewayId: String): WooPaymentGateway
    
    // Shipping
    @GET("wp-json/wc/v3/shipping/zones")
    suspend fun getShippingZones(): List<WooShippingZone>
    
    @GET("wp-json/wc/v3/shipping/zones/{id}/methods")
    suspend fun getShippingMethods(@Path("id") zoneId: Int): List<WooShippingMethod>
    
    // Customers
    @POST("wp-json/wc/v3/customers")
    suspend fun createCustomer(@Body body: WooCreateCustomerRequest): WooCustomer
    
    @GET("wp-json/wc/v3/customers/{id}")
    suspend fun getCustomer(@Path("id") customerId: Int): WooCustomer
    
    @GET("wp-json/wc/v3/customers")
    suspend fun getCustomers(
        @Query("email") email: String? = null,
        @Query("per_page") perPage: Int = 10,
        @Query("page") page: Int = 1
    ): List<WooCustomer>
    
    @PUT("wp-json/wc/v3/customers/{id}")
    suspend fun updateCustomer(@Path("id") customerId: Int, @Body body: WooCustomerUpdateRequest): WooCustomer
}
