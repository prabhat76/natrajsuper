package com.example.natraj

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class OrdersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders)

        val backButton = findViewById<ImageView>(R.id.orders_back_button)
        val ordersRecycler = findViewById<RecyclerView>(R.id.orders_recycler)
        val emptyView = findViewById<LinearLayout>(R.id.orders_empty_view)

        backButton.setOnClickListener {
            finish()
        }

        val orders = OrderManager.getOrders()

        if (orders.isEmpty()) {
            ordersRecycler.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        } else {
            ordersRecycler.visibility = View.VISIBLE
            emptyView.visibility = View.GONE

            ordersRecycler.layoutManager = LinearLayoutManager(this)
            ordersRecycler.adapter = OrderAdapter(orders) { order ->
                // TODO: Navigate to order details
            }
        }
    }
}
