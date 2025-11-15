package com.example.natraj.data.woo

data class WooCustomer(
    val id: Int = 0,
    val email: String,
    val first_name: String = "",
    val last_name: String = "",
    val username: String = "",
    val billing: WooBilling? = null,
    val shipping: WooShipping? = null,
    val role: String = "customer",
    val date_created: String = "",
    val date_modified: String = "",
    val meta_data: List<WooMetaData>? = null
)

data class WooCreateCustomerRequest(
    val email: String,
    val first_name: String = "",
    val last_name: String = "",
    val username: String = "",
    val password: String = "",
    val billing: WooBilling? = null,
    val shipping: WooShipping? = null
)
