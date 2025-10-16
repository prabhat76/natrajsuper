package com.example.natraj

import java.io.Serializable

data class CartItem(
    val product: Product,
    var quantity: Int = 1
) : Serializable
