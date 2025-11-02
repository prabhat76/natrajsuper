package com.example.natraj.data

import android.content.Context
import androidx.core.text.HtmlCompat
import com.example.natraj.BlogPost
import com.example.natraj.data.wp.WpClient

class WpRepository(private val context: Context) {
    private val api by lazy { WpClient.api(context) }

    suspend fun getRecentPosts(limit: Int = 5): List<BlogPost> {
        val posts = api.getPosts(perPage = limit)
        return posts.map { p ->
            val img = p.embedded?.media?.firstOrNull()?.sourceUrl ?: ""
            BlogPost(
                id = p.id,
                title = HtmlCompat.fromHtml(p.title.rendered, HtmlCompat.FROM_HTML_MODE_LEGACY).toString(),
                excerpt = HtmlCompat.fromHtml(p.excerpt.rendered, HtmlCompat.FROM_HTML_MODE_LEGACY).toString(),
                date = p.date,
                category = "",
                author = "",
                url = p.link,
                imageUrl = img
            )
        }
    }
}
