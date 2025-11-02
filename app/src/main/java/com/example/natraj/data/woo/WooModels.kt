package com.example.natraj.data.woo

// Minimal WooCommerce API models (subset)

data class WooImage(
    val id: Int?,
    val src: String?,
    val alt: String?
)

data class WooCategory(
    val id: Int,
    val name: String,
    val image: WooImage? = null,
    val count: Int? = 0
)

data class WooProduct(
    val id: Int,
    val name: String,
    val price: String?,
    val regular_price: String?,
    val sale_price: String?,
    val description: String?,
    val short_description: String?,
    val categories: List<WooCategory>?,
    val images: List<WooImage>?
)

data class WooAttribute(
    val id: Int,
    val name: String,
    val slug: String
)

data class WooAttributeTerm(
    val id: Int,
    val name: String,
    val slug: String
)

// Order payloads
data class WooOrderLineItem(
    val product_id: Int,
    val quantity: Int,
)

data class WooBilling(
    val first_name: String,
    val last_name: String,
    val address_1: String,
    val address_2: String? = null,
    val city: String,
    val state: String? = null,
    val postcode: String? = null,
    val country: String = "IN",
    val email: String,
    val phone: String
)

data class WooShipping(
    val first_name: String,
    val last_name: String,
    val address_1: String,
    val address_2: String? = null,
    val city: String,
    val state: String? = null,
    val postcode: String? = null,
    val country: String = "IN"
)

data class WooCreateOrderRequest(
    val payment_method: String = "cod",
    val payment_method_title: String = "Cash on Delivery",
    val set_paid: Boolean = false,
    val billing: WooBilling,
    val shipping: WooShipping,
    val line_items: List<WooOrderLineItem>
)

data class WooOrderResponse(
    val id: Int,
    val number: String,
    val status: String,
    val total: String
)

// Filter params helper
data class FilterParams(
    val categoryId: Int? = null,
    val minPrice: Int? = null,
    val maxPrice: Int? = null,
    val attribute: Int? = null,
    val attributeTerm: Int? = null,
    val page: Int = 1,
    val perPage: Int = 20
)
