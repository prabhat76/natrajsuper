package com.example.natraj.data

import android.content.Context
import com.example.natraj.Category
import com.example.natraj.data.model.Product
import com.example.natraj.data.woo.*

class WooRepository(private val context: Context) {
    private val api by lazy { WooClient.api(context) }
    
    // Memory cache for categories and products (dynamic timeout)
    private var categoriesCache: Pair<Long, List<Category>>? = null
    private val productCache = mutableMapOf<String, Pair<Long, List<Product>>>()
    private val cacheTimeout = AppConfig.getCacheTimeoutMinutes(context) * 60 * 1000L // Dynamic cache timeout

    suspend fun getCategories(): List<Category> {
        val prefs = WooPrefs(context)
        if (prefs.baseUrl.isNullOrBlank() || prefs.consumerKey.isNullOrBlank()) {
            return emptyList()
        }

        // Check cache first
        categoriesCache?.let { (timestamp, categories) ->
            if (System.currentTimeMillis() - timestamp < cacheTimeout) {
                return categories
            }
        }
        
        // Fetch from API
        val list = api.getCategories(perPage = AppConfig.getCategoriesPerPage(context), hideEmpty = false)
        val categories = list.map { wc ->
            Category(
                id = wc.id,
                name = wc.name,
                imageUrl = wc.image?.src ?: "",
                hasSpecialOffer = false,
                productCount = wc.count ?: 0
            )
        }.filter { it.id > 0 }

        // Update cache
        categoriesCache = System.currentTimeMillis() to categories
        return categories
    }

    suspend fun getProducts(params: FilterParams, featured: Boolean? = null): List<Product> {
        // Create cache key
        val cacheKey = "${params.categoryId}_${params.page}_${params.perPage}_${params.search}_$featured"

        // Check cache
        productCache[cacheKey]?.let { (timestamp, products) ->
            if (System.currentTimeMillis() - timestamp < cacheTimeout) {
                return products
            }
        }
        
        // Fetch from API
        val items = api.getProducts(
            perPage = params.perPage,
            page = params.page,
            category = params.categoryId,
            minPrice = params.minPrice,
            maxPrice = params.maxPrice,
            attribute = params.attribute,
            attributeTerm = params.attributeTerm,
            search = params.search,
            featured = featured
        )
        val products = items.map { mapProduct(it) }
        
        // Update cache
        productCache[cacheKey] = System.currentTimeMillis() to products
        return products
    }
    
    // Method to clear cache when needed
    fun clearCache() {
        categoriesCache = null
        productCache.clear()
    }

    suspend fun getAttributes(): List<WooAttribute> = api.getAttributes()

    suspend fun getAttributeTerms(attributeId: Int): List<WooAttributeTerm> = api.getAttributeTerms(attributeId)

    suspend fun placeOrder(
        billing: WooBilling,
        shipping: WooShipping,
        lineItems: List<WooOrderLineItem>,
        paymentMethod: String = AppConfig.getDefaultPaymentMethod(context),
        paymentTitle: String = AppConfig.getDefaultPaymentTitle(context),
        setPaid: Boolean = false,
        customerId: Int = 0,
        customerNote: String? = null,
        metaData: List<WooMetaData>? = null
    ): WooOrderResponse {
        val body = WooCreateOrderRequest(
            payment_method = paymentMethod,
            payment_method_title = paymentTitle,
            set_paid = setPaid,
            billing = billing,
            shipping = shipping,
            line_items = lineItems,
            customer_id = customerId,
            customer_note = customerNote,
            meta_data = metaData
        )
        return api.createOrder(body)
    }
    
    suspend fun getOrder(orderId: Int): WooOrderResponse {
        return api.getOrder(orderId)
    }
    
    suspend fun getOrders(
        perPage: Int = AppConfig.getOrdersPerPage(context),
        page: Int = 1,
        customerId: Int? = null,
        status: String? = null
    ): List<WooOrderResponse> {
        return api.getOrders(
            perPage = perPage,
            page = page,
            customerId = customerId,
            status = status
        )
    }
    
    suspend fun getRecentOrders(limit: Int = AppConfig.getRecentOrdersLimit(context)): List<WooOrderResponse> {
        return api.getOrders(perPage = limit, order = "desc")
    }
    
    suspend fun getOrderById(orderId: Int): WooOrderResponse? {
        return try {
            api.getOrder(orderId)
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun updateOrder(orderId: Int, status: String? = null, customerNote: String? = null, metaData: List<WooMetaData>? = null): WooOrderResponse {
        val body = WooOrderUpdateRequest(
            status = status,
            customer_note = customerNote,
            meta_data = metaData
        )
        return api.updateOrder(orderId, body)
    }
    
    suspend fun getPaymentGateways(): List<WooPaymentGateway> {
        return api.getPaymentGateways()
    }
    
    // Customer Management
    suspend fun createCustomer(
        email: String,
        firstName: String,
        lastName: String,
        username: String,
        password: String,
        billing: WooBilling? = null,
        shipping: WooShipping? = null
    ): WooCustomer {
        val request = WooCreateCustomerRequest(
            email = email,
            first_name = firstName,
            last_name = lastName,
            username = username,
            password = password,
            billing = billing,
            shipping = shipping
        )
        return api.createCustomer(request)
    }
    
    suspend fun getCustomer(customerId: Int): WooCustomer {
        return api.getCustomer(customerId)
    }
    
    suspend fun getCustomerByEmail(email: String): WooCustomer? {
        val customers = api.getCustomers(email = email, perPage = 1)
        return customers.firstOrNull()
    }
    
    suspend fun updateCustomer(
        customerId: Int,
        email: String? = null,
        firstName: String? = null,
        lastName: String? = null,
        billing: WooBilling? = null,
        shipping: WooShipping? = null,
        metaData: List<WooMetaData>? = null
    ): WooCustomer {
        val body = WooCustomerUpdateRequest(
            email = email,
            first_name = firstName,
            last_name = lastName,
            billing = billing,
            shipping = shipping,
            meta_data = metaData
        )
        return api.updateCustomer(customerId, body)
    }

    suspend fun getVyaparUpiId(): String {
        // Try to get from WooCommerce settings or options
        // For now, return a default or fetch from a custom endpoint
        return "vyapar@upi" // Replace with actual fetching logic
    }

    suspend fun getBanners(): List<com.example.natraj.Banner> {
        return listOf(
            com.example.natraj.Banner(1, "Natraj Super", "Agricultural Equipment", "Quality farming solutions", "https://www.natrajsuper.com/wp-content/uploads/2024/01/banner-1.jpg"),
            com.example.natraj.Banner(2, "Best Prices", "Guaranteed Quality", "Trusted by farmers", "https://www.natrajsuper.com/wp-content/uploads/2024/01/banner-2.jpg"),
            com.example.natraj.Banner(3, "Fast Delivery", "Pan India Service", "Quick and reliable", "https://www.natrajsuper.com/wp-content/uploads/2024/01/banner-3.jpg"),
            com.example.natraj.Banner(4, "Premium Quality", "Certified Products", "ISO certified equipment", "https://www.natrajsuper.com/wp-content/uploads/2024/01/banner-4.jpg"),
            com.example.natraj.Banner(5, "Expert Support", "24/7 Customer Care", "Always here to help", "https://www.natrajsuper.com/wp-content/uploads/2024/01/banner-5.jpg")
        )
    }

    private fun mapProduct(p: WooProduct): Product {
        // Choose price precedence: sale -> regular -> price
        val priceStr = p.sale_price?.takeIf { it.isNotBlank() }
            ?: p.regular_price?.takeIf { it.isNotBlank() }
            ?: p.price
        val price = priceStr?.toDoubleOrNull() ?: 0.0
        val mrp = p.regular_price?.toDoubleOrNull() ?: price
        val images = (p.images ?: emptyList()).mapNotNull { it.src }
        val firstImage = images.firstOrNull() ?: ""
        val categoryName = p.categories?.firstOrNull()?.name ?: ""

        return Product(
            id = p.id,
            name = p.name,
            price = price,
            originalPrice = mrp,
            imageUrl = firstImage,
            images = images,
            description = p.description ?: p.short_description.orEmpty(),
            category = categoryName,
            isFeatured = false
        )
    }
}
