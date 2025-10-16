package com.example.natraj

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CartAdapter(
    private var items: List<CartItem>,
    private val onQuantityChanged: (CartItem, Int) -> Unit,
    private val onRemove: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.VH>() {

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.cart_product_image)
        val name: TextView = itemView.findViewById(R.id.cart_product_name)
        val price: TextView = itemView.findViewById(R.id.cart_product_price)
        val qtyText: TextView = itemView.findViewById(R.id.quantity_text)
        val decBtn: Button = itemView.findViewById(R.id.decrease_quantity_btn)
        val incBtn: Button = itemView.findViewById(R.id.increase_quantity_btn)
        val removeBtn: ImageView = itemView.findViewById(R.id.remove_item_btn)

        fun bind(item: CartItem) {
            name.text = item.product.name
            price.text = "₹${(item.product.price * item.quantity).toInt()}"
            qtyText.text = item.quantity.toString()

            // Load image safely
            try {
                if (item.product.imageUrl.isNotEmpty()) {
                    Glide.with(itemView.context)
                        .load(item.product.imageUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(image)
                } else if (item.product.imageResId != 0) {
                    image.setImageResource(item.product.imageResId)
                } else {
                    image.setImageResource(R.drawable.ic_launcher_background)
                }
            } catch (e: Exception) {
                image.setImageResource(R.drawable.ic_launcher_background)
            }

            decBtn.setOnClickListener { 
                if (item.quantity > 1) {
                    onQuantityChanged(item, item.quantity - 1)
                }
            }
            incBtn.setOnClickListener { 
                onQuantityChanged(item, item.quantity + 1) 
            }
            removeBtn.setOnClickListener { 
                onRemove(item) 
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    fun update(newItems: List<CartItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
