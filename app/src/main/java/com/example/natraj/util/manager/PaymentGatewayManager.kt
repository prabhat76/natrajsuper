package com.example.natraj.util.manager

import android.content.Context
import com.example.natraj.data.WooRepository
import com.example.natraj.data.woo.WooPaymentGateway
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PaymentGatewayManager(private val context: Context) {
    
    private val wooRepository = WooRepository(context)
    private var cachedGateways: List<WooPaymentGateway>? = null
    
    suspend fun getAvailablePaymentGateways(): List<WooPaymentGateway> {
        return withContext(Dispatchers.IO) {
            try {
                if (cachedGateways == null) {
                    cachedGateways = wooRepository.getPaymentGateways()
                        .filter { it.enabled }
                }
                cachedGateways ?: getDefaultGateways()
            } catch (e: Exception) {
                android.util.Log.e("PaymentGatewayManager", "Failed to fetch payment gateways", e)
                getDefaultGateways()
            }
        }
    }
    
    private fun getDefaultGateways(): List<WooPaymentGateway> {
        return listOf(
            WooPaymentGateway(
                id = "cod",
                title = "Cash on Delivery",
                description = "Pay with cash upon delivery",
                enabled = true,
                method_title = "Cash on Delivery",
                settings = emptyMap()
            ),
            WooPaymentGateway(
                id = "bacs",
                title = "Direct Bank Transfer",
                description = "Make payment directly into our bank account",
                enabled = true,
                method_title = "Bank Transfer",
                settings = emptyMap()
            ),
            WooPaymentGateway(
                id = "razorpay",
                title = "Razorpay",
                description = "Pay securely via Credit Card, Debit Card, UPI, Netbanking",
                enabled = true,
                method_title = "Razorpay",
                settings = emptyMap()
            ),
            WooPaymentGateway(
                id = "phonepe",
                title = "PhonePe",
                description = "Pay via PhonePe UPI",
                enabled = true,
                method_title = "PhonePe",
                settings = emptyMap()
            ),
            WooPaymentGateway(
                id = "paytm",
                title = "Paytm",
                description = "Pay via Paytm Wallet or UPI",
                enabled = true,
                method_title = "Paytm",
                settings = emptyMap()
            )
        )
    }
    
    fun mapPaymentMethodToGatewayId(paymentMethod: String): String {
        return when (paymentMethod.lowercase()) {
            "cash on delivery", "cod" -> "cod"
            "upi", "phonepe", "google pay", "paytm" -> "razorpay" // Use Razorpay for UPI
            "card", "credit card", "debit card" -> "razorpay"
            "bank transfer", "netbanking" -> "bacs"
            else -> "cod"
        }
    }
    
    fun clearCache() {
        cachedGateways = null
    }
}
