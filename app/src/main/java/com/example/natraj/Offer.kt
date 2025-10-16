package com.example.natraj

data class Offer(
    val id: Int,
    val title: String,
    val discount: String,
    val originalPrice: String,
    val salePrice: String,
    val imageUrl: String,
    val productUrl: String
)