package com.example.natraj

import android.content.Context
import com.example.natraj.data.WpRepository
import kotlinx.coroutines.runBlocking

object OfferManager {
    private var offers: List<Offer> = emptyList()

    fun initialize(context: Context) {
        try {
            runBlocking {
                val wpRepository = WpRepository(context)
                val offerBanners = wpRepository.getOfferBanners()

                offers = if (offerBanners.isNotEmpty()) {
                    offerBanners.map { banner ->
                        Offer(
                            id = banner.id,
                            title = banner.title,
                            discount = extractDiscount(banner.subtitle),
                            originalPrice = "",
                            salePrice = "",
                            imageUrl = banner.imageUrl,
                            productUrl = "https://www.natrajsuper.com"
                        )
                    }
                } else {
                    // Strictly no fallback — empty list to allow UI to show Coming soon
                    emptyList()
                }
                android.util.Log.d("OfferManager", "Offers loaded: ${offers.size}")
            }
        } catch (e: Exception) {
            android.util.Log.e("OfferManager", "Error loading offers from WordPress", e)
            // Strictly no fallback
            offers = emptyList()
        }
    }

    private fun extractDiscount(subtitle: String): String {
        return when {
            subtitle.contains("50") -> "50%"
            subtitle.contains("40") -> "40%"
            subtitle.contains("30") -> "30%"
            subtitle.contains("29") -> "29%"
            subtitle.contains("26") -> "26%"
            subtitle.contains("23") -> "23%"
            subtitle.contains("52") -> "52%"
            subtitle.contains("47") -> "47%"
            subtitle.contains("33") -> "33%"
            else -> "UP TO 50%"
        }
    }

    fun getAllOffers(): List<Offer> = offers
}
