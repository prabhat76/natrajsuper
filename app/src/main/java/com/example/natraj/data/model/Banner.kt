package com.example.natraj

data class Banner(
    val id: Int,
    val title: String,
    val subtitle: String,
    val description: String,
    val imageUrl: String,
    val ctaText: String = "Shop Now"
)