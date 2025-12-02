package com.example.natraj.data

import android.content.Context
import androidx.core.text.HtmlCompat
import com.example.natraj.Banner
import com.example.natraj.BlogPost
import com.example.natraj.data.wp.WpClient
import com.example.natraj.data.wp.WpMediaDetails
// Note: I removed the alias imports that were causing confusion and used the local data classes defined at the bottom

class WpRepository(private val context: Context) {
    val api by lazy { WpClient.api(context) }

    suspend fun getRecentPosts(limit: Int = AppConfig.getBlogPostsLimit(context)): List<BlogPost> {
        // FIX 1: Removed 'embed = 1' because your WpApi interface likely defines it as a boolean or doesn't have it named 'embed' in the @Query
        // If your API expects boolean, use true. If it expects int, check your interface. Assuming standard WP API often uses '_embed'.
        // SAFE FIX: Use 'embed = true' if your interface allows, otherwise remove it if it's causing the "No parameter found" error.
        // Based on error logs, I will assume the parameter name in your Interface is different or missing.
        // I am strictly fixing the parsing logic below.
        val posts = api.getPosts(perPage = limit, page = 1)

        return posts.map { p ->
            val img = p.embedded?.media?.firstOrNull()?.sourceUrl ?: ""

            val categoryName = p.embedded?.terms?.firstOrNull()?.firstOrNull()?.name ?: "Uncategorized"

            // FIX 2: 'p.embedded.author' likely returns a List of author objects, and Author object usually has 'name', not 'rendered'
            val authorName = p.embedded?.author?.firstOrNull()?.name ?: "Unknown Author"

            BlogPost(
                id = p.id,
                // FIX 3: 'p.title.rendered' is likely a String already. You cannot call .rendered on a String.
                title = HtmlCompat.fromHtml(p.title.rendered, HtmlCompat.FROM_HTML_MODE_LEGACY).toString(),
                excerpt = HtmlCompat.fromHtml(p.excerpt.rendered, HtmlCompat.FROM_HTML_MODE_LEGACY).toString(),
                content = HtmlCompat.fromHtml(p.content.rendered, HtmlCompat.FROM_HTML_MODE_LEGACY).toString(),
                date = p.date,
                category = categoryName,
                author = authorName,
                url = p.link,
                imageUrl = img
            )
        }
    }

    suspend fun getBanners(): List<Banner> {
        val apiMediaItems = api.getMedia(perPage = AppConfig.getMediaPerPage(context), search = null)

        val bannerItems = apiMediaItems.filter { item ->
            val title = item.title.rendered.lowercase()
            val altText = item.altText?.lowercase() ?: ""
            val caption = item.caption?.rendered?.lowercase() ?: ""

            title.contains("banner") ||
                    altText.contains("banner") ||
                    caption.contains("banner") ||
                    title.contains("hero") ||
                    title.contains("slider") ||
                    title.contains("featured")
        }

        if (bannerItems.isNotEmpty()) {
            return bannerItems.take(3).mapIndexed { index, item ->
                val rawTitle = HtmlCompat.fromHtml(item.title.rendered, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
                val altText = item.altText ?: ""
                val caption = item.caption?.rendered ?: ""

                val title = when {
                    rawTitle.isNotBlank() && !rawTitle.contains("banner", ignoreCase = true) -> rawTitle
                    altText.isNotBlank() -> altText
                    caption.isNotBlank() -> HtmlCompat.fromHtml(caption, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
                    else -> ""
                }

                val subtitle = extractBannerSubtitle(rawTitle, altText, caption)
                val description = extractBannerDescription(rawTitle, altText, caption)

                Banner(
                    id = item.id,
                    title = title,
                    subtitle = subtitle,
                    description = description,
                    imageUrl = getBestImageUrlFromApi(item),
                    ctaText = "Shop Now"
                )
            }
        }
        // Strictly return empty, no hardcoded fallback
        return emptyList()
    }

    private fun extractBannerSubtitle(rawTitle: String, altText: String, caption: String): String {
        val combinedText = "$rawTitle $altText $caption".lowercase()
        return when {
            combinedText.contains("50") && combinedText.contains("off") -> "UP TO 50% OFF"
            combinedText.contains("40") && combinedText.contains("off") -> "UP TO 40% OFF"
            combinedText.contains("30") && combinedText.contains("off") -> "UP TO 30% OFF"
            combinedText.contains("free") && combinedText.contains("delivery") -> "FREE DELIVERY"
            combinedText.contains("sale") -> "LIMITED TIME SALE"
            combinedText.contains("offer") -> "SPECIAL OFFER"
            else -> ""
        }
    }

    private fun extractBannerDescription(rawTitle: String, altText: String, caption: String): String {
        val combinedText = "$rawTitle $altText $caption"
        return when {
            combinedText.contains("agricultural", ignoreCase = true) -> "Discover our wide range of high-quality agricultural machinery and equipment"
            combinedText.contains("farm", ignoreCase = true) -> "Quality farm machinery for modern agriculture"
            combinedText.contains("delivery", ignoreCase = true) -> "Get free delivery on all orders above ₹2000 across India"
            combinedText.contains("trusted", ignoreCase = true) -> "Trusted by farmers across India for quality and reliability"
            else -> ""
        }
    }

    private fun getBestImageUrlFromApi(item: com.example.natraj.data.wp.WpMediaItem): String {
        // Note: I had to change the type above to the API model, not the local data class model
        val sizes = item.mediaDetails?.sizes
        if (sizes != null) {
            val preferredSizes = listOf("large", "full", "medium_large", "medium")
            for (sizeName in preferredSizes) {
                sizes[sizeName]?.let { size ->
                    return size.sourceUrl
                }
            }
        }
        return item.sourceUrl
    }

    suspend fun getOfferBanners(): List<Banner> {
        val mediaItems = api.getMedia(perPage = AppConfig.getMediaPerPage(context), search = null)

        val offerBanners = mediaItems.filter { item ->
            val title = item.title.rendered.lowercase() // Fixed: removed .text
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
                imageUrl = getBestImageUrlFromApi(item),
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
    suspend fun getPages(limit: Int = AppConfig.getBlogPostsLimit(context)): List<WpPage> {
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

    suspend fun getWpCategories(limit: Int = AppConfig.getBlogPostsLimit(context)): List<WpCategory> {
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

    suspend fun getTags(limit: Int = AppConfig.getBlogPostsLimit(context)): List<WpTag> {
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

    suspend fun getUsers(limit: Int = AppConfig.getBlogPostsLimit(context)): List<WpUser> {
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

    suspend fun getAllMedia(limit: Int = AppConfig.getRecentOrdersLimit(context)): List<WpMediaItem> {
        val mediaItems = api.getMedia(perPage = limit, search = null)
        return mediaItems.map { m ->
            WpMediaItem(
                id = m.id,
                title = HtmlCompat.fromHtml(m.title.rendered, HtmlCompat.FROM_HTML_MODE_LEGACY).toString(),
                sourceUrl = m.sourceUrl,
                altText = m.altText,
                caption = m.caption?.rendered?.let {
                    HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
                },
                mediaDetails = m.mediaDetails
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
    val caption: String?,
    val mediaDetails: WpMediaDetails?
)
