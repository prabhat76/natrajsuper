package com.example.natraj

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast

class ProfileFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        
        val ordersSection = view.findViewById<LinearLayout>(R.id.profile_orders_section)
        val wishlistSection = view.findViewById<LinearLayout>(R.id.profile_wishlist_section)
        val addressSection = view.findViewById<LinearLayout>(R.id.profile_address_section)
        val settingsSection = view.findViewById<LinearLayout>(R.id.profile_settings_section)
        val helpSection = view.findViewById<LinearLayout>(R.id.profile_help_section)
        val logoutSection = view.findViewById<LinearLayout>(R.id.profile_logout_section)

        ordersSection.setOnClickListener {
            val intent = Intent(requireContext(), OrdersActivity::class.java)
            startActivity(intent)
        }

        wishlistSection.setOnClickListener {
            Toast.makeText(requireContext(), "Wishlist feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        addressSection.setOnClickListener {
            Toast.makeText(requireContext(), "Address management coming soon!", Toast.LENGTH_SHORT).show()
        }

        settingsSection.setOnClickListener {
            Toast.makeText(requireContext(), "Settings coming soon!", Toast.LENGTH_SHORT).show()
        }

        helpSection.setOnClickListener {
            Toast.makeText(requireContext(), "Help & Support coming soon!", Toast.LENGTH_SHORT).show()
        }

        logoutSection.setOnClickListener {
            Toast.makeText(requireContext(), "Logout functionality coming soon!", Toast.LENGTH_SHORT).show()
        }

        return view
    }
}

