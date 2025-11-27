package com.example.natraj.ui.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import com.example.natraj.AuthManager
import com.example.natraj.LoginActivity
import com.example.natraj.OrdersActivity
import com.example.natraj.R
import com.example.natraj.ui.activities.AccountDetailsActivity

import com.example.natraj.ui.activities.WishlistActivity
import com.example.natraj.util.CustomToast


class ProfileFragment : Fragment() {

    private lateinit var accountSection: LinearLayout
    private lateinit var ordersSection: LinearLayout
    private lateinit var downloadsSection: LinearLayout
    private lateinit var wishlistSection: LinearLayout
    private lateinit var logoutSection: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize views
        accountSection = view.findViewById(R.id.profile_account_section)
        ordersSection = view.findViewById(R.id.profile_orders_section)
        downloadsSection = view.findViewById(R.id.profile_downloads_section)
        wishlistSection = view.findViewById(R.id.profile_wishlist_section)
        logoutSection = view.findViewById(R.id.profile_logout_section)

        // Set click listeners
        accountSection.setOnClickListener {
            if (AuthManager.isLoggedIn()) {
                val intent = Intent(requireContext(), AccountDetailsActivity::class.java)
                startActivity(intent)
            } else {
                showLoginPrompt()
            }
        }

        ordersSection.setOnClickListener {
            if (AuthManager.isLoggedIn()) {
                val intent = Intent(requireContext(), OrdersActivity::class.java)
                startActivity(intent)
            } else {
                showLoginPrompt()
            }
        }

        downloadsSection.setOnClickListener {
            if (AuthManager.isLoggedIn()) {
                CustomToast.showInfo(requireContext(), "Downloads feature coming soon")
            } else {
                showLoginPrompt()
            }
        }

        wishlistSection.setOnClickListener {
            if (AuthManager.isLoggedIn()) {
                val intent = Intent(requireContext(), WishlistActivity::class.java)
                startActivity(intent)
            } else {
                showLoginPrompt()
            }
        }

        logoutSection.setOnClickListener {
            if (AuthManager.isLoggedIn()) {
                showLogoutConfirmation()
            } else {
                showLoginPrompt()
            }
        }

        return view
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

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                AuthManager.logout()
                CustomToast.showSuccess(requireContext(), "Logged out successfully")
                // Refresh the fragment or navigate to login
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
