package com.example.natraj

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken

object OfferManager {
    private var offers: List<Offer> = emptyList()
    
    fun initialize(context: Context) {
        val json = context.assets.open("offers.json").bufferedReader().use { it.readText() }
        val jsonObject = Gson().fromJson(json, JsonObject::class.java)
        
        val offersArray = jsonObject.getAsJsonArray("offers")
        // Use getParameterized to avoid issues with R8/proguard removing generic signatures
        val offerType = TypeToken.getParameterized(java.util.List::class.java, Offer::class.java).type
        offers = Gson().fromJson(offersArray, offerType)
        
        android.util.Log.d("OfferManager", "Loaded ${offers.size} offers")
    }
    
    fun getAllOffers(): List<Offer> = offers
}
