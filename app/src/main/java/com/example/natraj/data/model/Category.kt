package com.example.natraj

data class Category(
    val id: Int,
    val name: String,
    val imageResId: Int = 0,
    val imageUrl: String = "",
    val hasSpecialOffer: Boolean = false,
    val productCount: Int = 0
)
