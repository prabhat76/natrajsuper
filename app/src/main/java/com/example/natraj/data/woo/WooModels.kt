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
    val line_items: List<WooOrderLineItem>,
    val customer_note: String? = null,
    val meta_data: List<WooMetaData>? = null
)

data class WooMetaData(
    val key: String,
    val value: String
)

data class WooOrderResponse(
    val id: Int,
    val number: String,
    val status: String,
    val total: String,
    val date_created: String? = null,
    val payment_method: String? = null,
    val payment_method_title: String? = null,
    val tracking_number: String? = null,
    val tracking_provider: String? = null,
    val meta_data: List<WooMetaData>? = null,
    val line_items: List<WooOrderLineItem>? = null
)

// Payment Gateway
data class WooPaymentGateway(
    val id: String,
    val title: String,
    val description: String,
    val enabled: Boolean,
    val method_title: String? = null,
    val settings: Map<String, Any>? = null
)

// Shipping Zone
data class WooShippingZone(
    val id: Int,
    val name: String,
    val order: Int
)

data class WooShippingMethod(
    val id: String,
    val title: String,
    val enabled: Boolean,
    val settings: Map<String, Any>? = null
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
