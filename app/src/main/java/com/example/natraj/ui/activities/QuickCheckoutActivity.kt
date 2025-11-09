package com.example.natraj

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.natraj.data.WooRepository
import com.example.natraj.data.woo.*
import com.example.natraj.util.CustomToast
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuickCheckoutActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var productNameText: TextView
    private lateinit var productPriceText: TextView
    private lateinit var quantitySpinner: Spinner
    private lateinit var totalPriceText: TextView
    
    // Customer Details
    private lateinit var nameInput: TextInputEditText
    private lateinit var phoneInput: TextInputEditText
    private lateinit var addressInput: TextInputEditText
    private lateinit var pincodeInput: TextInputEditText
    
    // Payment Method
    private lateinit var codRadio: RadioButton
    private lateinit var onlineRadio: RadioButton
    private lateinit var paymentGroup: RadioGroup
    
    // Delivery Options
    private lateinit var standardDelivery: RadioButton
    private lateinit var expressDelivery: RadioButton
    private lateinit var deliveryGroup: RadioGroup
    private lateinit var deliveryChargeText: TextView
    
    private lateinit var placeOrderButton: Button
    private lateinit var progressBar: ProgressBar
    
    private var product: Product? = null
    private var quantity = 1
    private var deliveryCharge = 0.0
    private val TAG = "QuickCheckoutActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quick_checkout)

        product = intent.getSerializableExtra("product") as? Product
        
        if (product == null) {
            CustomToast.showError(this, "Error loading product")
            finish()
            return
        }

        initializeViews()
        setupToolbar()
        setupProductInfo()
        setupListeners()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        productNameText = findViewById(R.id.product_name)
        productPriceText = findViewById(R.id.product_price)
        quantitySpinner = findViewById(R.id.quantity_spinner)
        totalPriceText = findViewById(R.id.total_price)
        
        nameInput = findViewById(R.id.name_input)
        phoneInput = findViewById(R.id.phone_input)
        addressInput = findViewById(R.id.address_input)
        pincodeInput = findViewById(R.id.pincode_input)
        
        paymentGroup = findViewById(R.id.payment_group)
        codRadio = findViewById(R.id.cod_radio)
        onlineRadio = findViewById(R.id.online_radio)
        
        deliveryGroup = findViewById(R.id.delivery_group)
        standardDelivery = findViewById(R.id.standard_delivery)
        expressDelivery = findViewById(R.id.express_delivery)
        deliveryChargeText = findViewById(R.id.delivery_charge)
        
        placeOrderButton = findViewById(R.id.place_order_button)
        progressBar = findViewById(R.id.quick_checkout_progress_bar)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Quick Checkout"
        }
    }

    private fun setupProductInfo() {
        product?.let {
            productNameText.text = it.name
            productPriceText.text = "₹${String.format("%.2f", it.price)}"
            
            // Setup quantity spinner
            val quantities = (1..10).toList()
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, quantities)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            quantitySpinner.adapter = adapter
            
            updateTotalPrice()
        }
    }

    private fun setupListeners() {
        quantitySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                quantity = position + 1
                updateTotalPrice()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        deliveryGroup.setOnCheckedChangeListener { _, checkedId ->
            deliveryCharge = when (checkedId) {
                R.id.standard_delivery -> 0.0
                R.id.express_delivery -> 150.0
                else -> 0.0
            }
            deliveryChargeText.text = if (deliveryCharge > 0) {
                "₹${String.format("%.2f", deliveryCharge)}"
            } else {
                "FREE"
            }
            updateTotalPrice()
        }

        placeOrderButton.setOnClickListener {
            if (validateInputs()) {
                placeOrder()
            }
        }
    }

    private fun updateTotalPrice() {
        product?.let {
            val subtotal = it.price * quantity
            val total = subtotal + deliveryCharge
            totalPriceText.text = "₹${String.format("%.2f", total)}"
        }
    }

    private fun validateInputs(): Boolean {
        if (nameInput.text.toString().trim().isEmpty()) {
            nameInput.error = "Name is required"
            return false
        }

        val phone = phoneInput.text.toString().trim()
        if (phone.isEmpty() || phone.length != 10) {
            phoneInput.error = "Valid 10-digit phone number required"
            return false
        }

        if (addressInput.text.toString().trim().isEmpty()) {
            addressInput.error = "Address is required"
            return false
        }

        val pincode = pincodeInput.text.toString().trim()
        if (pincode.isEmpty() || pincode.length != 6) {
            pincodeInput.error = "Valid 6-digit pincode required"
            return false
        }

        return true
    }

    private fun placeOrder() {
        Log.d(TAG, "=== Quick Checkout Order Placement Started ===")
        
        if (!validateInputs()) {
            return
        }
        
        val paymentMethod = if (codRadio.isChecked) "cod" else "online"
        val paymentTitle = if (codRadio.isChecked) "Cash on Delivery" else "Online Payment"
        
        // Show progress
        progressBar?.visibility = View.VISIBLE
        placeOrderButton.isEnabled = false
        
        val prefs = WooPrefs(this)
        val hasWooConfig = !prefs.baseUrl.isNullOrBlank() && 
                          !prefs.consumerKey.isNullOrBlank() && 
                          !prefs.consumerSecret.isNullOrBlank()

        if (!hasWooConfig) {
            CustomToast.showError(this, "WooCommerce not configured. Please check settings.", Toast.LENGTH_LONG)
            progressBar?.visibility = View.GONE
            placeOrderButton.isEnabled = true
            return
        }

        lifecycleScope.launch {
            try {
                val repo = WooRepository(this@QuickCheckoutActivity)
                
                // Prepare line items
                val lineItems = listOf(
                    WooOrderLineItem(
                        product_id = product!!.id,
                        quantity = quantity
                    )
                )
                
                // Prepare billing info
                val name = nameInput.text.toString().trim()
                val nameParts = name.split(" ", limit = 2)
                val billing = WooBilling(
                    first_name = nameParts.getOrNull(0) ?: name,
                    last_name = nameParts.getOrNull(1) ?: "",
                    address_1 = addressInput.text.toString().trim(),
                    address_2 = "",
                    city = "",
                    state = "",
                    postcode = pincodeInput.text.toString().trim(),
                    email = AuthManager.getUserEmail().ifBlank { "customer@natrajsuper.com" },
                    phone = phoneInput.text.toString().trim(),
                    country = "IN"
                )
                
                // Prepare shipping info
                val shipping = WooShipping(
                    first_name = billing.first_name,
                    last_name = billing.last_name,
                    address_1 = billing.address_1,
                    address_2 = billing.address_2,
                    city = billing.city,
                    state = billing.state,
                    postcode = billing.postcode,
                    country = "IN"
                )
                
                // Add custom meta data
                val deliveryType = if (expressDelivery.isChecked) "Express (1-2 days)" else "Standard (3-5 days)"
                val metaData = listOf(
                    WooMetaData("_app_order", "true"),
                    WooMetaData("_order_source", "Android App - Quick Checkout"),
                    WooMetaData("_delivery_type", deliveryType),
                    WooMetaData("_delivery_charge", deliveryCharge.toString())
                )
                
                Log.d(TAG, "Creating WooCommerce order...")
                
                // Place order
                val response = withContext(Dispatchers.IO) {
                    repo.placeOrder(
                        billing = billing,
                        shipping = shipping,
                        lineItems = lineItems,
                        paymentMethod = paymentMethod,
                        paymentTitle = paymentTitle,
                        setPaid = paymentMethod != "cod",
                        customerNote = "Quick checkout order",
                        metaData = metaData
                    )
                }

                progressBar?.visibility = View.GONE
                placeOrderButton.isEnabled = true
                
                Log.d(TAG, "✓ Order created successfully!")
                Log.d(TAG, "Order ID: ${response.id}")
                Log.d(TAG, "Order Number: ${response.number}")
                
                // Navigate to confirmation with tracking
                val intent = Intent(this@QuickCheckoutActivity, OrderConfirmationActivity::class.java)
                intent.putExtra("order_id", response.number)
                intent.putExtra("order_woo_id", response.id)
                intent.putExtra("order_status", response.status)
                intent.putExtra("order_total", response.total)
                intent.putExtra("offline_mode", false)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
                
            } catch (e: Exception) {
                progressBar?.visibility = View.GONE
                placeOrderButton.isEnabled = true
                
                Log.e(TAG, "✗ Order placement failed", e)
                CustomToast.showError(this@QuickCheckoutActivity, 
                    "Failed to place order: ${e.message}", 
                    Toast.LENGTH_LONG)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
