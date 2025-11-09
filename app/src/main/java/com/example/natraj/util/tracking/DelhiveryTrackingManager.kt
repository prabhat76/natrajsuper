package com.example.natraj.util.tracking

import android.content.Context
import com.example.natraj.data.WooRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Delhivery Tracking Integration
 * Manages shipment tracking via Delhivery API and WooCommerce order meta
 */
class DelhiveryTrackingManager(private val context: Context) {
    
    private val wooRepository = WooRepository(context)
    
    companion object {
        private const val META_KEY_TRACKING_NUMBER = "_delhivery_tracking_number"
        private const val META_KEY_TRACKING_PROVIDER = "_tracking_provider"
        private const val META_KEY_AWB_NUMBER = "_awb_number"
        private const val TRACKING_PROVIDER = "Delhivery"
    }
    
    /**
     * Add tracking information to a WooCommerce order
     */
    suspend fun addTrackingToOrder(
        orderId: Int,
        trackingNumber: String,
        awbNumber: String? = null
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val updateBody = mapOf(
                    "meta_data" to listOf(
                        mapOf("key" to META_KEY_TRACKING_NUMBER, "value" to trackingNumber),
                        mapOf("key" to META_KEY_TRACKING_PROVIDER, "value" to TRACKING_PROVIDER),
                        mapOf("key" to META_KEY_AWB_NUMBER, "value" to (awbNumber ?: trackingNumber))
                    ),
                    "status" to "processing" // Update status to processing when tracking is added
                )
                
                wooRepository.updateOrder(orderId, updateBody)
                Result.success(Unit)
            } catch (e: Exception) {
                android.util.Log.e("DelhiveryTracking", "Failed to add tracking", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Get tracking information from order
     */
    suspend fun getTrackingInfo(orderId: Int): TrackingInfo? {
        return withContext(Dispatchers.IO) {
            try {
                val order = wooRepository.getOrder(orderId)
                val metaData = order.meta_data ?: emptyList()
                
                val trackingNumber = metaData.find { it.key == META_KEY_TRACKING_NUMBER }?.value
                val provider = metaData.find { it.key == META_KEY_TRACKING_PROVIDER }?.value
                val awbNumber = metaData.find { it.key == META_KEY_AWB_NUMBER }?.value
                
                if (trackingNumber.isNullOrBlank()) {
                    null
                } else {
                    TrackingInfo(
                        trackingNumber = trackingNumber,
                        provider = provider ?: TRACKING_PROVIDER,
                        awbNumber = awbNumber ?: trackingNumber,
                        trackingUrl = generateTrackingUrl(trackingNumber)
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("DelhiveryTracking", "Failed to get tracking", e)
                null
            }
        }
    }
    
    /**
     * Update order status based on tracking status
     */
    suspend fun updateOrderStatusFromTracking(
        orderId: Int,
        trackingStatus: String
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val wooStatus = mapTrackingStatusToWooStatus(trackingStatus)
                val updateBody = mapOf("status" to wooStatus)
                
                wooRepository.updateOrder(orderId, updateBody)
                Result.success(Unit)
            } catch (e: Exception) {
                android.util.Log.e("DelhiveryTracking", "Failed to update order status", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Generate Delhivery tracking URL
     */
    private fun generateTrackingUrl(trackingNumber: String): String {
        return "https://www.delhivery.com/track/package/$trackingNumber"
    }
    
    /**
     * Map Delhivery tracking status to WooCommerce order status
     */
    private fun mapTrackingStatusToWooStatus(trackingStatus: String): String {
        return when (trackingStatus.lowercase()) {
            "dispatched", "in-transit" -> "processing"
            "out-for-delivery" -> "processing"
            "delivered" -> "completed"
            "cancelled", "returned" -> "cancelled"
            else -> "processing"
        }
    }
}

/**
 * Data class for tracking information
 */
data class TrackingInfo(
    val trackingNumber: String,
    val provider: String,
    val awbNumber: String,
    val trackingUrl: String
)
