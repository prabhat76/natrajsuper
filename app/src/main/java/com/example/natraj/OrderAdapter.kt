package com.example.natraj

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class OrderAdapter(
    private val orders: List<Order>,
    private val onOrderClick: (Order) -> Unit
) : RecyclerView.Adapter<OrderAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val orderId: TextView = view.findViewById(R.id.order_id)
        val orderDate: TextView = view.findViewById(R.id.order_date)
        val orderStatus: TextView = view.findViewById(R.id.order_status)
        val orderAmount: TextView = view.findViewById(R.id.order_amount)
        val orderItems: TextView = view.findViewById(R.id.order_items)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orders[position]
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        
        holder.orderId.text = "Order #${order.id}"
        holder.orderDate.text = "Placed on ${dateFormat.format(order.orderDate)}"
        holder.orderStatus.text = order.status.name
        holder.orderAmount.text = "₹${order.totalAmount.toInt()}"
        holder.orderItems.text = "${order.items.size} items"

        val statusColor = when (order.status) {
            OrderStatus.DELIVERED -> android.R.color.holo_green_dark
            OrderStatus.CANCELLED -> android.R.color.holo_red_dark
            else -> android.R.color.holo_orange_dark
        }
        holder.orderStatus.setTextColor(holder.itemView.context.getColor(statusColor))

        holder.itemView.setOnClickListener {
            onOrderClick(order)
        }
    }

    override fun getItemCount() = orders.size
}
