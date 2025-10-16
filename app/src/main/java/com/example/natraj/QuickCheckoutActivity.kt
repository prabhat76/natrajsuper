package com.example.natraj

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

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
    
    private var product: Product? = null
    private var quantity = 1
    private var deliveryCharge = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quick_checkout)

        product = intent.getSerializableExtra("product") as? Product
        
        if (product == null) {
            Toast.makeText(this, "Error loading product", Toast.LENGTH_SHORT).show()
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
        val paymentMethod = if (codRadio.isChecked) "Cash on Delivery" else "Online Payment"
        val deliveryType = if (expressDelivery.isChecked) "Express (1-2 days)" else "Standard (3-5 days)"
        
        val orderDetails = """
            Order Confirmed! 🎉
            
            Product: ${product?.name}
            Quantity: $quantity
            Price: ₹${String.format("%.2f", product?.price!! * quantity)}
            Delivery: $deliveryCharge (${if (deliveryCharge > 0) "₹$deliveryCharge" else "FREE"})
            Total: ₹${String.format("%.2f", (product?.price!! * quantity) + deliveryCharge)}
            
            Delivery Type: $deliveryType
            Payment: $paymentMethod
            
            Name: ${nameInput.text}
            Phone: ${phoneInput.text}
            Address: ${addressInput.text}, ${pincodeInput.text}
        """.trimIndent()

        // Show order confirmation
        android.app.AlertDialog.Builder(this)
            .setTitle("✅ Order Placed Successfully!")
            .setMessage(orderDetails)
            .setPositiveButton("Track Order") { _, _ ->
                // Navigate to order tracking
                Toast.makeText(this, "Order tracking feature coming soon!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton("Close") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
