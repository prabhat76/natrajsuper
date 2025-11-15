package com.example.natraj.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.natraj.AuthManager
import com.example.natraj.R
import com.example.natraj.data.WooRepository
import com.example.natraj.data.woo.WooBilling
import com.example.natraj.data.woo.WooShipping
import com.example.natraj.util.CustomToast
import com.example.natraj.util.sync.AccountSyncManager
import kotlinx.coroutines.launch

/**
 * Account Details Activity - Syncs with WooCommerce website
 * Allows editing of:
 * - First Name & Last Name
 * - Billing Address
 * - Shipping Address
 * All changes sync to WooCommerce automatically
 */
class AccountDetailsActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var scrollView: ScrollView
    private lateinit var syncButton: Button
    private lateinit var saveButton: Button
    
    // Account fields
    private lateinit var firstNameInput: EditText
    private lateinit var lastNameInput: EditText
    private lateinit var emailText: TextView
    
    // Billing fields
    private lateinit var billingAddress1: EditText
    private lateinit var billingAddress2: EditText
    private lateinit var billingCity: EditText
    private lateinit var billingState: EditText
    private lateinit var billingPostcode: EditText
    private lateinit var billingPhone: EditText
    
    // Shipping fields
    private lateinit var shippingAddress1: EditText
    private lateinit var shippingAddress2: EditText
    private lateinit var shippingCity: EditText
    private lateinit var shippingState: EditText
    private lateinit var shippingPostcode: EditText
    private lateinit var copyBillingCheckbox: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_details)

        initializeViews()
        setupListeners()
        loadAccountData()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.account_back_button)
        progressBar = findViewById(R.id.account_progress_bar)
        scrollView = findViewById(R.id.account_scroll_view)
        syncButton = findViewById(R.id.sync_from_web_button)
        saveButton = findViewById(R.id.save_account_button)
        
        firstNameInput = findViewById(R.id.first_name_input)
        lastNameInput = findViewById(R.id.last_name_input)
        emailText = findViewById(R.id.email_text)
        
        billingAddress1 = findViewById(R.id.billing_address_1)
        billingAddress2 = findViewById(R.id.billing_address_2)
        billingCity = findViewById(R.id.billing_city)
        billingState = findViewById(R.id.billing_state)
        billingPostcode = findViewById(R.id.billing_postcode)
        billingPhone = findViewById(R.id.billing_phone)
        
        shippingAddress1 = findViewById(R.id.shipping_address_1)
        shippingAddress2 = findViewById(R.id.shipping_address_2)
        shippingCity = findViewById(R.id.shipping_city)
        shippingState = findViewById(R.id.shipping_state)
        shippingPostcode = findViewById(R.id.shipping_postcode)
        copyBillingCheckbox = findViewById(R.id.copy_billing_checkbox)
    }

    private fun setupListeners() {
        backButton.setOnClickListener { finish() }
        
        syncButton.setOnClickListener {
            syncFromWeb()
        }
        
        saveButton.setOnClickListener {
            saveAccountDetails()
        }
        
        copyBillingCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                copyBillingToShipping()
            }
        }
    }

    private fun loadAccountData() {
        progressBar.visibility = View.VISIBLE
        scrollView.visibility = View.GONE
        
        lifecycleScope.launch {
            try {
                // Load from WooCommerce
                val customerId = AuthManager.getCustomerId()
                if (customerId == 0) {
                    CustomToast.showError(this@AccountDetailsActivity, "Please login first")
                    finish()
                    return@launch
                }
                
                val repo = WooRepository(this@AccountDetailsActivity)
                val customer = repo.getCustomer(customerId)
                
                // Populate fields
                firstNameInput.setText(customer.first_name)
                lastNameInput.setText(customer.last_name)
                emailText.text = customer.email
                
                // Billing address
                customer.billing?.let { billing ->
                    billingAddress1.setText(billing.address_1)
                    billingAddress2.setText(billing.address_2 ?: "")
                    billingCity.setText(billing.city)
                    billingState.setText(billing.state ?: "")
                    billingPostcode.setText(billing.postcode ?: "")
                    billingPhone.setText(billing.phone)
                }
                
                // Shipping address
                customer.shipping?.let { shipping ->
                    shippingAddress1.setText(shipping.address_1)
                    shippingAddress2.setText(shipping.address_2 ?: "")
                    shippingCity.setText(shipping.city)
                    shippingState.setText(shipping.state ?: "")
                    shippingPostcode.setText(shipping.postcode ?: "")
                }
                
                progressBar.visibility = View.GONE
                scrollView.visibility = View.VISIBLE
                
                CustomToast.showSuccess(this@AccountDetailsActivity, "Account data loaded from website")
                
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                CustomToast.showError(this@AccountDetailsActivity, "Failed to load account: ${e.message}")
            }
        }
    }

    private fun syncFromWeb() {
        progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                AccountSyncManager.syncAccountFromWoo(this@AccountDetailsActivity).getOrThrow()
                loadAccountData()
                CustomToast.showSuccess(this@AccountDetailsActivity, "Synced from website")
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                CustomToast.showError(this@AccountDetailsActivity, "Sync failed: ${e.message}")
            }
        }
    }

    private fun saveAccountDetails() {
        val firstName = firstNameInput.text.toString().trim()
        val lastName = lastNameInput.text.toString().trim()
        
        if (firstName.isEmpty()) {
            firstNameInput.error = "First name required"
            return
        }
        
        progressBar.visibility = View.VISIBLE
        saveButton.isEnabled = false
        
        lifecycleScope.launch {
            try {
                val email = emailText.text.toString()
                
                val billing = WooBilling(
                    first_name = firstName,
                    last_name = lastName,
                    address_1 = billingAddress1.text.toString(),
                    address_2 = billingAddress2.text.toString(),
                    city = billingCity.text.toString(),
                    state = billingState.text.toString(),
                    postcode = billingPostcode.text.toString(),
                    email = email,
                    phone = billingPhone.text.toString()
                )
                
                val shipping = WooShipping(
                    first_name = firstName,
                    last_name = lastName,
                    address_1 = shippingAddress1.text.toString(),
                    address_2 = shippingAddress2.text.toString(),
                    city = shippingCity.text.toString(),
                    state = shippingState.text.toString(),
                    postcode = shippingPostcode.text.toString()
                )
                
                // Sync to WooCommerce
                AccountSyncManager.syncAccountToWoo(
                    this@AccountDetailsActivity,
                    firstName,
                    lastName,
                    billing,
                    shipping
                ).getOrThrow()
                
                progressBar.visibility = View.GONE
                saveButton.isEnabled = true
                
                CustomToast.showSuccess(this@AccountDetailsActivity, "✓ Saved to website")
                finish()
                
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                saveButton.isEnabled = true
                CustomToast.showError(this@AccountDetailsActivity, "Failed to save: ${e.message}")
            }
        }
    }

    private fun copyBillingToShipping() {
        shippingAddress1.setText(billingAddress1.text)
        shippingAddress2.setText(billingAddress2.text)
        shippingCity.setText(billingCity.text)
        shippingState.setText(billingState.text)
        shippingPostcode.setText(billingPostcode.text)
    }
}
