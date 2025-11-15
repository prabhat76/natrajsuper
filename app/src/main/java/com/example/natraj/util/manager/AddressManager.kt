package com.example.natraj.util.manager

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class SavedAddress(
    val id: String = System.currentTimeMillis().toString(),
    val name: String,
    val phone: String,
    val addressLine: String,
    val pincode: String,
    val isDefault: Boolean = false
)

object AddressManager {
    private const val PREFS_NAME = "address_prefs"
    private const val KEY_ADDRESSES = "addresses"
    private const val KEY_DEFAULT_ADDRESS = "default_address"
    
    private lateinit var prefs: SharedPreferences
    private val gson = Gson()
    
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    fun saveAddress(address: SavedAddress) {
        val addresses = getAllAddresses().toMutableList()
        
        // If this is set as default, remove default flag from others
        if (address.isDefault) {
            addresses.forEachIndexed { index, addr ->
                if (addr.isDefault) {
                    addresses[index] = addr.copy(isDefault = false)
                }
            }
        }
        
        // Check if address already exists and update, otherwise add
        val existingIndex = addresses.indexOfFirst { it.id == address.id }
        if (existingIndex >= 0) {
            addresses[existingIndex] = address
        } else {
            addresses.add(address)
        }
        
        val json = gson.toJson(addresses)
        prefs.edit().putString(KEY_ADDRESSES, json).apply()
        
        // If this is the first address or marked as default, set it as default
        if (addresses.size == 1 || address.isDefault) {
            setDefaultAddress(address.id)
        }
    }
    
    fun getAllAddresses(): List<SavedAddress> {
        val json = prefs.getString(KEY_ADDRESSES, null) ?: return emptyList()
        val type = object : TypeToken<List<SavedAddress>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
    
    fun getDefaultAddress(): SavedAddress? {
        val defaultId = prefs.getString(KEY_DEFAULT_ADDRESS, null)
        return if (defaultId != null) {
            getAllAddresses().find { it.id == defaultId }
        } else {
            getAllAddresses().firstOrNull()
        }
    }
    
    fun setDefaultAddress(addressId: String) {
        prefs.edit().putString(KEY_DEFAULT_ADDRESS, addressId).apply()
        
        // Update the isDefault flag in the addresses list
        val addresses = getAllAddresses().toMutableList()
        addresses.forEachIndexed { index, addr ->
            addresses[index] = addr.copy(isDefault = addr.id == addressId)
        }
        val json = gson.toJson(addresses)
        prefs.edit().putString(KEY_ADDRESSES, json).apply()
    }
    
    fun deleteAddress(addressId: String) {
        val addresses = getAllAddresses().toMutableList()
        addresses.removeIf { it.id == addressId }
        
        val json = gson.toJson(addresses)
        prefs.edit().putString(KEY_ADDRESSES, json).apply()
        
        // If deleted address was default, set first address as default
        val defaultId = prefs.getString(KEY_DEFAULT_ADDRESS, null)
        if (defaultId == addressId && addresses.isNotEmpty()) {
            setDefaultAddress(addresses[0].id)
        }
    }
    
    fun hasAddresses(): Boolean {
        return getAllAddresses().isNotEmpty()
    }
}
