package com.example.natraj

import com.example.natraj.data.model.Product
import java.io.Serializable

data class CartItem(
    val product: Product,
    var quantity: Int = 1
) : Serializable
