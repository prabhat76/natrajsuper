package com.example.natraj

import java.io.Serializable

data class Product(
    val id: Int,
    val name: String,
    val price: Double,
    val originalPrice: Double = price,
    val imageResId: Int = 0,
    val imageUrl: String = "",
    val images: List<String>? = null,
    val specs: Map<String, String> = emptyMap(),
    val rating: Float = 4.0f,
    val reviewCount: Int = 0,
    val discount: Int = 0,
    val isFeatured: Boolean = false,
    val isAvailable: Boolean = true,
    val category: String = "",
    val description: String = "",
    // Enhanced fields from Excel data
    val supplierName: String? = "Natraj Super",
    val brand: String? = "Natraj Super",
    val articleCode: String? = "",
    val productLink: String? = "",
    val mrp: Double = price,
    val transferPrice: Double = price,
    val hsnCode: String? = "",
    val tax: Int = 12,
    val moq: Int = 1,
    val inventory: Int = 10,
    val leadTime: String? = "",
    val manufacturerInfo: String? = "",
    val dimensions: String? = "",
    val weight: String? = "",
    val countryOfOrigin: String? = "India",
    val imageLinks: List<String>? = null,
    val videoLink: String? = "",
    val packageContents: List<String>? = null,
    val uses: List<String>? = null,
    val warranty: String? = "",
    val features: List<String>? = null
) : Serializable {
    val discountPercent: Int
        get() = if (originalPrice > price) {
            ((originalPrice - price) / originalPrice * 100).toInt()
        } else discount
    
    val effectivePrice: Double
        get() = transferPrice
    
    val savingsAmount: Double
        get() = mrp - effectivePrice
    
    val discountPercentFromMrp: Int
        get() = if (mrp > effectivePrice) {
            ((mrp - effectivePrice) / mrp * 100).toInt()
        } else 0
}
