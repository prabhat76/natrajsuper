package com.example.natraj

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class AddressActivity : AppCompatActivity() {

    private lateinit var nameInput: EditText
    private lateinit var mobileInput: EditText
    private lateinit var pincodeInput: EditText
    private lateinit var addressInput: EditText
    private lateinit var cityInput: EditText
    private lateinit var stateInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address)

        val backButton = findViewById<ImageView>(R.id.address_back_button)
        nameInput = findViewById(R.id.address_name)
        mobileInput = findViewById(R.id.address_mobile)
        pincodeInput = findViewById(R.id.address_pincode)
        addressInput = findViewById(R.id.address_address)
        val localityInput = findViewById<EditText>(R.id.address_locality)
        cityInput = findViewById(R.id.address_city)
        stateInput = findViewById(R.id.address_state)
        val addressTypeGroup = findViewById<RadioGroup>(R.id.address_type_group)
        val continueButton = findViewById<Button>(R.id.address_continue_btn)

        backButton.setOnClickListener {
            // Navigate back to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        // Add real-time validation
        setupRealTimeValidation()

        continueButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val mobile = mobileInput.text.toString().trim()
            val pincode = pincodeInput.text.toString().trim()
            val address = addressInput.text.toString().trim()
            val locality = localityInput.text.toString().trim()
            val city = cityInput.text.toString().trim()
            val state = stateInput.text.toString().trim()

            // Validate all fields
            if (!validateAllFields(name, mobile, pincode, address, city, state)) {
                return@setOnClickListener
            }

            val selectedTypeId = addressTypeGroup.checkedRadioButtonId
            val addressType = when (selectedTypeId) {
                R.id.address_type_home -> "Home"
                R.id.address_type_work -> "Work"
                else -> "Other"
            }

            val deliveryAddress = Address(
                name = name,
                mobile = mobile,
                pincode = pincode,
                address = address,
                locality = locality,
                city = city,
                state = state,
                addressType = addressType
            )

            // Navigate to payment page
            val intent = Intent(this, PaymentActivity::class.java)
            intent.putExtra("address", deliveryAddress)
            startActivity(intent)
        }
    }

    private fun setupRealTimeValidation() {
        // Mobile validation
        mobileInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val mobile = s.toString()
                if (mobile.isNotEmpty() && mobile.length != 10) {
                    mobileInput.error = "Enter 10-digit mobile number"
                } else if (mobile.isNotEmpty() && !mobile.matches(Regex("^[6-9][0-9]{9}$"))) {
                    mobileInput.error = "Enter valid mobile number"
                } else {
                    mobileInput.error = null
                }
            }
        })

        // Pincode validation
        pincodeInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val pincode = s.toString()
                if (pincode.isNotEmpty() && pincode.length != 6) {
                    pincodeInput.error = "Enter 6-digit pincode"
                } else {
                    pincodeInput.error = null
                }
            }
        })

        // Name validation
        nameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val name = s.toString()
                if (name.isNotEmpty() && name.length < 2) {
                    nameInput.error = "Enter valid name"
                } else {
                    nameInput.error = null
                }
            }
        })
    }

    private fun validateAllFields(
        name: String,
        mobile: String,
        pincode: String,
        address: String,
        city: String,
        state: String
    ): Boolean {
        // Check for empty required fields
        if (name.isEmpty()) {
            nameInput.error = "Name is required"
            nameInput.requestFocus()
            showToast("Please enter your name")
            return false
        }

        if (name.length < 2) {
            nameInput.error = "Enter valid name"
            nameInput.requestFocus()
            showToast("Please enter a valid name")
            return false
        }

        if (mobile.isEmpty()) {
            mobileInput.error = "Mobile number is required"
            mobileInput.requestFocus()
            showToast("Please enter your mobile number")
            return false
        }

        if (mobile.length != 10) {
            mobileInput.error = "Enter 10-digit mobile number"
            mobileInput.requestFocus()
            showToast("Please enter a valid 10-digit mobile number")
            return false
        }

        if (!mobile.matches(Regex("^[6-9][0-9]{9}$"))) {
            mobileInput.error = "Enter valid Indian mobile number"
            mobileInput.requestFocus()
            showToast("Mobile number should start with 6, 7, 8, or 9")
            return false
        }

        if (pincode.isEmpty()) {
            pincodeInput.error = "Pincode is required"
            pincodeInput.requestFocus()
            showToast("Please enter your pincode")
            return false
        }

        if (pincode.length != 6) {
            pincodeInput.error = "Enter 6-digit pincode"
            pincodeInput.requestFocus()
            showToast("Please enter a valid 6-digit pincode")
            return false
        }

        if (address.isEmpty()) {
            addressInput.error = "Address is required"
            addressInput.requestFocus()
            showToast("Please enter your complete address")
            return false
        }

        if (address.length < 10) {
            addressInput.error = "Enter complete address"
            addressInput.requestFocus()
            showToast("Please enter a more detailed address")
            return false
        }

        if (city.isEmpty()) {
            cityInput.error = "City is required"
            cityInput.requestFocus()
            showToast("Please enter your city")
            return false
        }

        if (state.isEmpty()) {
            stateInput.error = "State is required"
            stateInput.requestFocus()
            showToast("Please enter your state")
            return false
        }

        return true
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
