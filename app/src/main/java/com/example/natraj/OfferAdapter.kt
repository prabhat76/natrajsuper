package com.example.natraj

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class OfferAdapter(
    private val offers: List<Offer>,
    private val onOfferClick: (Offer) -> Unit = {}
) : RecyclerView.Adapter<OfferAdapter.OfferViewHolder>() {

    inner class OfferViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val offerImage: ImageView = itemView.findViewById(R.id.offer_image)
        private val offerTitle: TextView = itemView.findViewById(R.id.offer_title)
        private val discountBadge: TextView = itemView.findViewById(R.id.discount_badge)
        private val salePrice: TextView = itemView.findViewById(R.id.sale_price)
        private val originalPrice: TextView = itemView.findViewById(R.id.original_price)

        fun bind(offer: Offer) {
            offerTitle.text = offer.title
            discountBadge.text = offer.discount
            salePrice.text = offer.salePrice
            originalPrice.text = offer.originalPrice
            
            // Add strikethrough to original price
            originalPrice.paintFlags = originalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            
            // Load image with Glide
            Glide.with(itemView.context)
                .load(offer.imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .into(offerImage)

            itemView.setOnClickListener { onOfferClick(offer) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfferViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_offer, parent, false)
        return OfferViewHolder(view)
    }

    override fun onBindViewHolder(holder: OfferViewHolder, position: Int) {
        holder.bind(offers[position])
    }

    override fun getItemCount(): Int = offers.size
}