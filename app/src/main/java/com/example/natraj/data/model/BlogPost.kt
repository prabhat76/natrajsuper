package com.example.natraj

import java.io.Serializable

data class BlogPost(
    val id: Int,
    val title: String,
    val excerpt: String,
    val content: String = "", // Full HTML content
    val date: String,
    val category: String,
    val author: String,
    val url: String,
    val imageUrl: String
) : Serializable
