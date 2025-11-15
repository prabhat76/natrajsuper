package com.example.natraj

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.natraj.data.woo.WooOrderResponse
import java.text.SimpleDateFormat
import java.util.Locale

class WooOrderAdapter(
    private var orders: List<WooOrderResponse>,
    private val onOrderClick: (WooOrderResponse) -> Unit
) : RecyclerView.Adapter<WooOrderAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productImage: ImageView = view.findViewById(R.id.order_product_image)
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
        
        // Load first product image if available
        val firstProduct = order.line_items?.firstOrNull()
        if (firstProduct?.image?.src != null) {
            Glide.with(holder.itemView.context)
                .load(firstProduct.image.src)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .centerCrop()
                .into(holder.productImage)
        } else {
            holder.productImage.setImageResource(R.drawable.ic_launcher_background)
        }
        
        holder.orderId.text = "Order #${order.number ?: order.id}"
        
        // Parse date from WooCommerce format (2024-11-08T12:30:45)
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val date = order.date_created?.let { inputFormat.parse(it) }
            holder.orderDate.text = date?.let { "Placed on ${outputFormat.format(it)}" } 
                ?: "Placed recently"
        } catch (e: Exception) {
            holder.orderDate.text = "Placed recently"
        }
        
        // Format status
        val statusText = when (order.status?.lowercase()) {
            "pending" -> "Pending Payment"
            "processing" -> "Processing"
            "on-hold" -> "On Hold"
            "completed" -> "Completed"
            "cancelled" -> "Cancelled"
            "refunded" -> "Refunded"
            "failed" -> "Failed"
            else -> order.status?.replaceFirstChar { it.uppercase() } ?: "Unknown"
        }
        holder.orderStatus.text = statusText
        
        // Format amount
        val total = order.total?.toDoubleOrNull() ?: 0.0
        holder.orderAmount.text = "₹${total.toInt()}"
        
        // Count items
        val itemCount = order.line_items?.size ?: 0
        holder.orderItems.text = "$itemCount item${if (itemCount != 1) "s" else ""}"

        // Status color
        val statusColor = when (order.status?.lowercase()) {
            "completed" -> android.R.color.holo_green_dark
            "cancelled", "refunded", "failed" -> android.R.color.holo_red_dark
            "processing" -> android.R.color.holo_blue_dark
            else -> android.R.color.holo_orange_dark
        }
        holder.orderStatus.setTextColor(holder.itemView.context.getColor(statusColor))

        holder.itemView.setOnClickListener {
            onOrderClick(order)
        }
    }

    override fun getItemCount() = orders.size
    
    fun updateOrders(newOrders: List<WooOrderResponse>) {
        orders = newOrders
        notifyDataSetChanged()
    }
}
