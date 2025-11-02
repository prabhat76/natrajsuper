package com.example.natraj

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class BannerAdapter(
    private val banners: List<Banner>,
    private val onBannerClick: (Banner) -> Unit = {}
) : RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {

    inner class BannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val bannerImage: ImageView = itemView.findViewById(R.id.banner_image)
        private val bannerTitle: TextView = itemView.findViewById(R.id.banner_title)
        private val bannerSubtitle: TextView = itemView.findViewById(R.id.banner_subtitle)
        private val bannerDescription: TextView = itemView.findViewById(R.id.banner_description)
        private val ctaButton: Button = itemView.findViewById(R.id.banner_cta_button)

        fun bind(banner: Banner) {
            bannerTitle.text = banner.title
            bannerSubtitle.text = banner.subtitle
            bannerDescription.text = banner.description
            ctaButton.text = banner.ctaText

            // Load banner image
            if (banner.imageUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(banner.imageUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(bannerImage)
            }

            itemView.setOnClickListener { onBannerClick(banner) }
            ctaButton.setOnClickListener { onBannerClick(banner) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_banner, parent, false)
        return BannerViewHolder(view)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        holder.bind(banners[position])
    }

    override fun getItemCount(): Int = banners.size
}