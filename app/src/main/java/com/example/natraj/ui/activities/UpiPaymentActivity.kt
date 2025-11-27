package com.example.natraj

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.natraj.data.WooRepository
import com.example.natraj.util.CustomToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.glxn.qrgen.android.QRCode

class UpiPaymentActivity : AppCompatActivity() {

    private lateinit var qrCodeImageView: ImageView
    private lateinit var amountTextView: TextView
    private lateinit var payButton: Button
    private lateinit var cancelButton: Button

    private lateinit var cartItems: List<CartItem>
    private var totalAmount: Double = 0.0
    private lateinit var address: Address

    private val TAG = "UpiPaymentActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upi_payment)

        Log.d(TAG, "UPI Payment Activity started")

        // Get data from intent
        cartItems = intent.getSerializableExtra("cart_items") as? ArrayList<CartItem> ?: emptyList()
        totalAmount = intent.getDoubleExtra("total_amount", 0.0)
        address = intent.getSerializableExtra("address") as? Address ?: run {
            Log.e(TAG, "No address provided")
            finish()
            return
        }

        Log.d(TAG, "Total amount: ₹${totalAmount.toInt()}")

        qrCodeImageView = findViewById(R.id.upi_qr_code)
        amountTextView = findViewById(R.id.upi_amount)
        payButton = findViewById(R.id.upi_pay_button)
        cancelButton = findViewById(R.id.upi_cancel_button)

        amountTextView.text = "₹${totalAmount.toInt()}"

        cancelButton.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        payButton.setOnClickListener {
            // For now, assume payment is successful and proceed
            setResult(RESULT_OK)
            finish()
        }

        // Generate QR code
        generateQRCode()
    }

    private fun generateQRCode() {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Generating QR code...")
                val repo = WooRepository(this@UpiPaymentActivity)
                val upiId = withContext(Dispatchers.IO) { repo.getVyaparUpiId() }
                Log.d(TAG, "Fetched UPI ID: $upiId")

                val merchantName = "Natraj Super"
                val transactionNote = "Payment for Order"
                val upiUrl = "upi://pay?pa=$upiId&pn=$merchantName&am=$totalAmount&cu=INR&tn=$transactionNote"

                Log.d(TAG, "UPI URL: $upiUrl")

                val qrCodeBitmap = withContext(Dispatchers.IO) {
                    QRCode.from(upiUrl).withSize(512, 512).bitmap()
                }

                withContext(Dispatchers.Main) {
                    qrCodeImageView.setImageBitmap(qrCodeBitmap)
                    Log.d(TAG, "QR code generated successfully")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to generate QR code", e)
                CustomToast.showError(this@UpiPaymentActivity, "Failed to generate QR code", android.widget.Toast.LENGTH_LONG)
                finish()
            }
        }
    }
}
