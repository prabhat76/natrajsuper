package com.example.natraj

import android.content.Context
import com.example.natraj.data.WpRepository
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.runBlocking

object OfferManager {
    private var offers: List<Offer> = emptyList()

    fun initialize(context: Context) {
        try {
            // Try to fetch offers from WordPress API first
            runBlocking {
                val wpRepository = WpRepository(context)
                val offerBanners = wpRepository.getOfferBanners()

                if (offerBanners.isNotEmpty()) {
                    // Convert Banner objects to Offer objects
                    offers = offerBanners.map { banner ->
                        Offer(
                            id = banner.id,
                            title = banner.title,
                            discount = extractDiscount(banner.subtitle),
                            originalPrice = "",
                            salePrice = "",
                            imageUrl = banner.imageUrl,
                            productUrl = "https://www.natrajsuper.com" // Default URL
                        )
                    }
                    android.util.Log.d("OfferManager", "Loaded ${offers.size} offers from WordPress API")
                } else {
                    android.util.Log.w("OfferManager", "No offers from WordPress, using fallback")
                    loadFromJsonFallback(context)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("OfferManager", "Error loading offers from WordPress", e)
            loadFromJsonFallback(context)
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

    private fun loadFromJsonFallback(context: Context) {
        try {
            val json = context.assets.open("offers.json").bufferedReader().use { it.readText() }
            val jsonObject = Gson().fromJson(json, JsonObject::class.java)

            val offersArray = jsonObject.getAsJsonArray("offers")
            // Use getParameterized to avoid issues with R8/proguard removing generic signatures
            val offerType = TypeToken.getParameterized(java.util.List::class.java, Offer::class.java).type
            offers = Gson().fromJson(offersArray, offerType)

            android.util.Log.d("OfferManager", "Loaded ${offers.size} offers from JSON fallback")
        } catch (e: Exception) {
            android.util.Log.e("OfferManager", "Error loading offers from JSON", e)
            offers = emptyList()
        }
    }
    
    fun getAllOffers(): List<Offer> = offers
}
