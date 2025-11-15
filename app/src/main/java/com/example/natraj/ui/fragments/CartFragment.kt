package com.example.natraj

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Toast

class CartFragment : Fragment() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: CartAdapter
    private lateinit var itemCountText: TextView
    private lateinit var subtotalText: TextView
    private lateinit var discountText: TextView
    private lateinit var totalText: TextView
    private lateinit var checkoutBtn: Button
    private lateinit var emptyLayout: View
    private lateinit var summaryCard: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_cart, container, false)

        recycler = view.findViewById(R.id.cart_recycler)
        itemCountText = view.findViewById(R.id.cart_item_count)
        subtotalText = view.findViewById(R.id.subtotal_amount)
        discountText = view.findViewById(R.id.discount_amount)
        totalText = view.findViewById(R.id.total_amount)
        checkoutBtn = view.findViewById(R.id.checkout_btn)
        emptyLayout = view.findViewById(R.id.empty_cart_layout)
        summaryCard = view.findViewById(R.id.cart_summary_card)

        adapter = CartAdapter(CartManager.getItems(), { item, qty ->
            CartManager.updateQuantity(item.product, qty)
            refresh()
        }, { item ->
            CartManager.remove(item.product)
            refresh()
        })

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        view.findViewById<Button>(R.id.continue_shopping_btn).setOnClickListener {
            // Switch to home fragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }

        checkoutBtn.setOnClickListener {
            if (CartManager.getItems().isEmpty()) {
                if (isAdded && context != null) {
                    Toast.makeText(requireContext(), "Cart is empty", Toast.LENGTH_SHORT).show()
                }
                return@setOnClickListener
            }
            
            // Check minimum order amount
            val items = CartManager.getItems()
            val subtotal = items.sumOf { it.product.price * it.quantity }
            val MINIMUM_ORDER_AMOUNT = 700.0
            
            if (subtotal < MINIMUM_ORDER_AMOUNT) {
                if (isAdded && context != null) {
                    Toast.makeText(
                        requireContext(), 
                        "Minimum order amount is ₹${MINIMUM_ORDER_AMOUNT.toInt()}. Current: ₹${subtotal.toInt()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                return@setOnClickListener
            }
            
            // Check login status before checkout
            if (!AuthManager.isLoggedIn()) {
                showLoginPromptForCheckout()
            } else {
                // Navigate to address page
                val intent = Intent(requireContext(), AddressActivity::class.java)
                startActivity(intent)
            }
        }

        refresh()
        return view
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        try {
            val items = CartManager.getItems()
            adapter.update(items)
            
            if (items.isEmpty()) {
                emptyLayout.visibility = View.VISIBLE
                summaryCard.visibility = View.GONE
                recycler.visibility = View.GONE
            } else {
                emptyLayout.visibility = View.GONE
                summaryCard.visibility = View.VISIBLE
                recycler.visibility = View.VISIBLE
                
                val subtotal = items.sumOf { it.product.price * it.quantity }
                val discount = subtotal * 0.05 // 5% discount
                val afterDiscount = subtotal - discount
                val delivery = if (afterDiscount > 50000) 0.0 else 500.0
                val total = afterDiscount + delivery
                
                val totalItems = items.sumOf { it.quantity }
                itemCountText.text = "Price ($totalItems items)"
                subtotalText.text = "₹${subtotal.toInt()}"
                discountText.text = "− ₹${discount.toInt()}"
                totalText.text = "₹${total.toInt()}"
            }
        } catch (e: Exception) {
            // Handle any errors gracefully
            emptyLayout.visibility = View.VISIBLE
            summaryCard.visibility = View.GONE
            recycler.visibility = View.GONE
        }
    }
    
    private fun showLoginPromptForCheckout() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Login for Better Experience")
            .setMessage("Login to access saved addresses and track your orders. You can also continue as guest.")
            .setPositiveButton("Login") { _, _ ->
                val intent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(intent)
            }
            .setNegativeButton("Continue as Guest") { _, _ ->
                // Allow guest checkout
                val intent = Intent(requireContext(), AddressActivity::class.java)
                startActivity(intent)
            }
            .setNeutralButton("Cancel", null)
            .show()
    }
}

