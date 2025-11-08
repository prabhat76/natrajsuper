package com.example.natraj

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class ProfileFragment : Fragment() {
    
    private lateinit var userNameText: TextView
    private lateinit var userEmailText: TextView
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        
        userNameText = view.findViewById(R.id.profile_user_name)
        userEmailText = view.findViewById(R.id.profile_user_email)
        
        // Update user info
        updateUserInfo()
        
        val ordersSection = view.findViewById<LinearLayout>(R.id.profile_orders_section)
        val wishlistSection = view.findViewById<LinearLayout>(R.id.profile_wishlist_section)
        val addressSection = view.findViewById<LinearLayout>(R.id.profile_address_section)
        val settingsSection = view.findViewById<LinearLayout>(R.id.profile_settings_section)
        val helpSection = view.findViewById<LinearLayout>(R.id.profile_help_section)
        val logoutSection = view.findViewById<LinearLayout>(R.id.profile_logout_section)

        ordersSection.setOnClickListener {
            if (AuthManager.isLoggedIn()) {
                val intent = Intent(requireContext(), OrdersActivity::class.java)
                startActivity(intent)
            } else {
                showLoginPrompt()
            }
        }

        wishlistSection.setOnClickListener {
            if (AuthManager.isLoggedIn()) {
                if (isAdded && context != null) {
                    Toast.makeText(requireContext(), "Wishlist feature coming soon!", Toast.LENGTH_SHORT).show()
                }
            } else {
                showLoginPrompt()
            }
        }

        addressSection.setOnClickListener {
            if (AuthManager.isLoggedIn()) {
                if (isAdded && context != null) {
                    Toast.makeText(requireContext(), "Address management coming soon!", Toast.LENGTH_SHORT).show()
                }
            } else {
                showLoginPrompt()
            }
        }

        settingsSection.setOnClickListener {
            val intent = Intent(requireContext(), WordPressSettingsActivity::class.java)
            startActivity(intent)
        }

        helpSection.setOnClickListener {
            // Launch debug activity for development
            val intent = Intent(requireContext(), APIDebugActivity::class.java)
            startActivity(intent)
        }

        logoutSection.setOnClickListener {
            if (AuthManager.isLoggedIn()) {
                showLogoutDialog()
            } else {
                showLoginPrompt()
            }
        }

        return view
    }
    
    override fun onResume() {
        super.onResume()
        updateUserInfo()
    }
    
    private fun updateUserInfo() {
        if (AuthManager.isLoggedIn()) {
            userNameText.text = AuthManager.getUserName()
            userEmailText.text = AuthManager.getUserEmail()
            userEmailText.visibility = View.VISIBLE
        } else {
            userNameText.text = "Guest User"
            userEmailText.text = "Login to access all features"
            userEmailText.visibility = View.VISIBLE
        }
    }
    
    private fun showLoginPrompt() {
        AlertDialog.Builder(requireContext())
            .setTitle("Login Required")
            .setMessage("Please login to access this feature")
            .setPositiveButton("Login") { _, _ ->
                val intent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun performLogout() {
        AuthManager.logout()
        if (isAdded && context != null) {
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
        }
        
        // Navigate to login screen
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }
}

