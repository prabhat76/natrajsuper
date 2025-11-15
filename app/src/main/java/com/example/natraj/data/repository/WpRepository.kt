package com.example.natraj.data

import android.content.Context
import androidx.core.text.HtmlCompat
import com.example.natraj.Banner
import com.example.natraj.BlogPost
import com.example.natraj.data.wp.WpClient
import com.example.natraj.data.wp.WpPage as WpApiPage
import com.example.natraj.data.wp.WpCategory as WpApiCategory
import com.example.natraj.data.wp.WpTag as WpApiTag
import com.example.natraj.data.wp.WpUser as WpApiUser
import com.example.natraj.data.wp.WpMediaItem as WpApiMediaItem

class WpRepository(private val context: Context) {
    val api by lazy { WpClient.api(context) }

    suspend fun getRecentPosts(limit: Int = 5): List<BlogPost> {
        val posts = api.getPosts(perPage = limit)
        
        // Fetch categories and authors for mapping if needed
        val categories = try { getWpCategories() } catch (e: Exception) { emptyList() }
        val users = try { getUsers() } catch (e: Exception) { emptyList() }
        
        return posts.map { p ->
            val img = p.embedded?.media?.firstOrNull()?.sourceUrl ?: ""
            
            // Extract category name from embedded or fallback
            val categoryName = p.embedded?.terms?.firstOrNull()?.firstOrNull()?.name
                ?: categories.firstOrNull { it.id == p.categories.firstOrNull() }?.name
                ?: "Uncategorized"
            
            // Extract author name from embedded or fallback
            val authorName = p.embedded?.author?.firstOrNull()?.name
                ?: users.firstOrNull { it.id == p.author }?.name
                ?: "Admin"
            
            BlogPost(
                id = p.id,
                title = HtmlCompat.fromHtml(p.title.rendered, HtmlCompat.FROM_HTML_MODE_LEGACY).toString(),
                excerpt = HtmlCompat.fromHtml(p.excerpt.rendered, HtmlCompat.FROM_HTML_MODE_LEGACY).toString(),
                content = p.content.rendered, // Full HTML content
                date = p.date,
                category = categoryName,
                author = authorName,
                url = p.link,
                imageUrl = img
            )
        }
    }

    suspend fun getBanners(): List<Banner> {
        val mediaItems = api.getMedia(perPage = 10, search = "banner")
        return mediaItems.filter { 
            it.sourceUrl.isNotEmpty() && it.title.rendered.contains("banner", ignoreCase = true)
        }.mapIndexed { index, item ->
            val title = HtmlCompat.fromHtml(item.title.rendered, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
            Banner(
                id = item.id,
                title = extractTitle(title, index),
                subtitle = extractSubtitle(index),
                description = extractDescription(index),
                imageUrl = item.sourceUrl,
                ctaText = "Shop Now"
            )
        }
    }

    private fun extractTitle(rawTitle: String, index: Int): String {
        return when {
            rawTitle.contains("1") || index == 0 -> "Festival Sale"
            rawTitle.contains("2") || index == 1 -> "Premium Tools"
            else -> "Quality Assured"
        }
    }

    private fun extractSubtitle(index: Int): String {
        return when (index) {
            0 -> "UP TO 50% OFF"
            1 -> "FREE DELIVERY"
            else -> "BEST PRICES"
        }
    }

    private fun extractDescription(index: Int): String {
        return when (index) {
            0 -> "On all agricultural equipment"
            1 -> "On orders above ₹2000"
            else -> "Guaranteed authentic products"
        }
    }

    suspend fun getOfferBanners(): List<Banner> {
        // Fetch promotional/offer banners (Diwali sale, festival offers, etc.)
        val mediaItems = api.getMedia(perPage = 20, search = null)
        
        // Filter for offer/sale/festival related banners
        val offerBanners = mediaItems.filter { item ->
            val title = item.title.rendered.lowercase()
            title.contains("diwali") || 
            title.contains("sale") || 
            title.contains("offer") || 
            title.contains("festival") ||
            title.contains("discount") ||
            title.contains("promo")
        }
        
        return offerBanners.mapIndexed { index, item ->
            val rawTitle = HtmlCompat.fromHtml(item.title.rendered, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
            Banner(
                id = item.id,
                title = extractOfferTitle(rawTitle),
                subtitle = extractOfferSubtitle(rawTitle),
                description = extractOfferDescription(rawTitle),
                imageUrl = item.sourceUrl,
                ctaText = "Shop Now"
            )
        }
    }

    private fun extractOfferTitle(rawTitle: String): String {
        return when {
            rawTitle.contains("diwali", ignoreCase = true) -> "Diwali Sale"
            rawTitle.contains("clearance", ignoreCase = true) -> "Clearance Sale"
            rawTitle.contains("mega", ignoreCase = true) -> "Mega Sale"
            rawTitle.contains("special", ignoreCase = true) -> "Special Offer"
            else -> "Limited Time Deal"
        }
    }

    private fun extractOfferSubtitle(rawTitle: String): String {
        return when {
            rawTitle.contains("50", ignoreCase = true) -> "UP TO 50% OFF"
            rawTitle.contains("40", ignoreCase = true) -> "UP TO 40% OFF"
            rawTitle.contains("30", ignoreCase = true) -> "UP TO 30% OFF"
            rawTitle.contains("off", ignoreCase = true) -> "HUGE DISCOUNTS"
            else -> "LIMITED TIME ONLY"
        }
    }

    private fun extractOfferDescription(rawTitle: String): String {
        return when {
            rawTitle.contains("diwali", ignoreCase = true) -> "Celebrate with amazing deals on all products"
            rawTitle.contains("clearance", ignoreCase = true) -> "Clear out old stock at unbeatable prices"
            rawTitle.contains("special", ignoreCase = true) -> "Get the best deals on premium products"
            else -> "Don't miss out on these exclusive offers"
        }
    }

    // New WordPress API methods
    suspend fun getPages(limit: Int = 10): List<WpPage> {
        val pages = api.getPages(perPage = limit)
        return pages.map { p ->
            WpPage(
                id = p.id,
                date = p.date,
                link = p.link,
                title = HtmlCompat.fromHtml(p.title.rendered, HtmlCompat.FROM_HTML_MODE_LEGACY).toString(),
                content = HtmlCompat.fromHtml(p.content.rendered, HtmlCompat.FROM_HTML_MODE_LEGACY).toString(),
                excerpt = HtmlCompat.fromHtml(p.excerpt.rendered, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
            )
        }
    }

    suspend fun getWpCategories(limit: Int = 100): List<WpCategory> {
        val categories = api.getCategories(perPage = limit)
        return categories.map { c ->
            WpCategory(
                id = c.id,
                name = c.name,
                slug = c.slug,
                description = c.description,
                count = c.count
            )
        }
    }

    suspend fun getTags(limit: Int = 100): List<WpTag> {
        val tags = api.getTags(perPage = limit)
        return tags.map { t ->
            WpTag(
                id = t.id,
                name = t.name,
                slug = t.slug,
                description = t.description,
                count = t.count
            )
        }
    }

    suspend fun getUsers(limit: Int = 10): List<WpUser> {
        val users = api.getUsers(perPage = limit)
        return users.map { u ->
            WpUser(
                id = u.id,
                name = u.name,
                slug = u.slug,
                description = u.description,
                avatarUrls = u.avatarUrls
            )
        }
    }

    suspend fun getAllMedia(limit: Int = 50): List<WpMediaItem> {
        val mediaItems = api.getMedia(perPage = limit, search = null)
        return mediaItems.map { m ->
            WpMediaItem(
                id = m.id,
                title = HtmlCompat.fromHtml(m.title.rendered, HtmlCompat.FROM_HTML_MODE_LEGACY).toString(),
                sourceUrl = m.sourceUrl,
                altText = m.altText,
                caption = m.caption?.rendered?.let { 
                    HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY).toString() 
                }
            )
        }
    }
}

// Data classes for WordPress content
data class WpPage(
    val id: Int,
    val date: String,
    val link: String,
    val title: String,
    val content: String,
    val excerpt: String
)

data class WpCategory(
    val id: Int,
    val name: String,
    val slug: String,
    val description: String,
    val count: Int
)

data class WpTag(
    val id: Int,
    val name: String,
    val slug: String,
    val description: String,
    val count: Int
)

data class WpUser(
    val id: Int,
    val name: String,
    val slug: String,
    val description: String,
    val avatarUrls: Map<String, String>?
)

data class WpMediaItem(
    val id: Int,
    val title: String,
    val sourceUrl: String,
    val altText: String?,
    val caption: String?
)
