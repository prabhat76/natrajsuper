package com.example.natraj.util.sync

import android.content.Context
import android.util.Log
import com.example.natraj.AuthManager
import com.example.natraj.WishlistManager
import com.example.natraj.data.WooRepository
import com.example.natraj.data.woo.WooBilling
import com.example.natraj.data.woo.WooMetaData
import com.example.natraj.data.woo.WooShipping
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Manages synchronization between app data and WooCommerce website
 * Ensures Orders, Addresses, Account Details, and Wishlist are in sync
 */
object AccountSyncManager {
    
    private const val TAG = "AccountSyncManager"
    private const val WISHLIST_META_KEY = "_app_wishlist"
    
    /**
     * Sync customer account details from WooCommerce to app
     */
    suspend fun syncAccountFromWoo(context: Context): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val customerId = AuthManager.getCustomerId()
                if (customerId == 0) {
                    return@withContext Result.failure(Exception("Not logged in"))
                }
                
                val repo = WooRepository(context)
                val customer = repo.getCustomer(customerId)
                
                // Update local storage with latest data from WooCommerce
                AuthManager.login(
                    name = "${customer.first_name} ${customer.last_name}".trim(),
                    email = customer.email,
                    phone = customer.billing?.phone ?: "",
                    customerId = customer.id
                )
                
                Log.d(TAG, "✓ Account synced from WooCommerce")
                Log.d(TAG, "  Customer ID: ${customer.id}")
                Log.d(TAG, "  Email: ${customer.email}")
                Log.d(TAG, "  Name: ${customer.first_name} ${customer.last_name}")
                
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "✗ Failed to sync account from WooCommerce", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Sync account details (name, addresses) to WooCommerce
     */
    suspend fun syncAccountToWoo(
        context: Context,
        firstName: String?,
        lastName: String?,
        billing: WooBilling?,
        shipping: WooShipping?
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val customerId = AuthManager.getCustomerId()
                if (customerId == 0) {
                    return@withContext Result.failure(Exception("Not logged in"))
                }
                
                val repo = WooRepository(context)
                val customer = repo.updateCustomer(
                    customerId = customerId,
                    firstName = firstName,
                    lastName = lastName,
                    billing = billing,
                    shipping = shipping
                )
                
                // Update local storage
                if (firstName != null || lastName != null) {
                    val name = "${firstName ?: customer.first_name} ${lastName ?: customer.last_name}".trim()
                    AuthManager.login(
                        name = name,
                        email = customer.email,
                        phone = customer.billing?.phone ?: "",
                        customerId = customer.id
                    )
                }
                
                Log.d(TAG, "✓ Account synced to WooCommerce")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "✗ Failed to sync account to WooCommerce", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Sync wishlist to WooCommerce customer meta data
     * This allows wishlist to be accessed from website too
     * 
     * NOTE: Requires WordPress custom endpoint or plugin to save meta_data
     * For example: POST /wp-json/custom/v1/customer/{id}/wishlist
     */
    suspend fun syncWishlistToWoo(context: Context): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val customerId = AuthManager.getCustomerId()
                if (customerId == 0) {
                    return@withContext Result.failure(Exception("Not logged in"))
                }
                
                // Get current wishlist
                val wishlistIds = WishlistManager.getWishlistIds()
                
                if (wishlistIds.isEmpty()) {
                    Log.d(TAG, "Wishlist is empty, nothing to sync")
                    return@withContext Result.success(Unit)
                }
                
                val repo = WooRepository(context)
                
                // Create meta data for wishlist
                val metaData = listOf(
                    WooMetaData(
                        key = "wishlist",
                        value = wishlistIds.joinToString(",")
                    )
                )
                
                // Update customer with wishlist meta data
                repo.updateCustomer(
                    customerId = customerId,
                    metaData = metaData
                )
                
                Log.d(TAG, "✓ Wishlist synced to WooCommerce (${wishlistIds.size} items)")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "✗ Failed to sync wishlist to WooCommerce", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Sync wishlist from WooCommerce customer meta data
     */
    suspend fun syncWishlistFromWoo(context: Context): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val customerId = AuthManager.getCustomerId()
                if (customerId == 0) {
                    return@withContext Result.failure(Exception("Not logged in"))
                }
                
                val repo = WooRepository(context)
                val customer = repo.getCustomer(customerId)
                
                // Find wishlist in meta_data
                val wishlistMeta = customer.meta_data?.find { it.key == "wishlist" }
                
                if (wishlistMeta != null) {
                    val wishlistString = wishlistMeta.value.toString()
                    val wishlistIds = wishlistString.split(",")
                        .mapNotNull { it.trim().toIntOrNull() }
                        .toSet()
                    
                    // Update local wishlist
                    WishlistManager.setWishlist(wishlistIds)
                    
                    Log.d(TAG, "✓ Wishlist synced from WooCommerce (${wishlistIds.size} items)")
                } else {
                    Log.d(TAG, "No wishlist found in WooCommerce meta data")
                }
                
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "✗ Failed to sync wishlist from WooCommerce", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Sync addresses (billing and shipping) to WooCommerce
     */
    suspend fun syncAddressesToWoo(
        context: Context,
        billing: WooBilling?,
        shipping: WooShipping?
    ): Result<Unit> {
        return syncAccountToWoo(context, null, null, billing, shipping)
    }
    
    /**
     * Get all addresses from WooCommerce
     */
    suspend fun getAddressesFromWoo(context: Context): Result<Pair<WooBilling?, WooShipping?>> {
        return withContext(Dispatchers.IO) {
            try {
                val customerId = AuthManager.getCustomerId()
                if (customerId == 0) {
                    return@withContext Result.failure(Exception("Not logged in"))
                }
                
                val repo = WooRepository(context)
                val customer = repo.getCustomer(customerId)
                
                Log.d(TAG, "✓ Addresses retrieved from WooCommerce")
                Result.success(Pair(customer.billing, customer.shipping))
            } catch (e: Exception) {
                Log.e(TAG, "✗ Failed to get addresses from WooCommerce", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Full sync: Pull all data from WooCommerce to app
     */
    suspend fun fullSyncFromWoo(context: Context): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                syncAccountFromWoo(context).getOrThrow()
                syncWishlistFromWoo(context).getOrThrow()
                
                Log.d(TAG, "✓ Full sync completed from WooCommerce")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "✗ Full sync failed", e)
                Result.failure(e)
            }
        }
    }
}
