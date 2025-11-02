package com.example.natraj.data.wp

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

data class WpRendered(val rendered: String)

data class WpMedia(@SerializedName("source_url") val sourceUrl: String?)

data class WpEmbedded(@SerializedName("wp:featuredmedia") val media: List<WpMedia>?)

data class WpPost(
    val id: Int,
    val date: String,
    val link: String,
    val title: WpRendered,
    val excerpt: WpRendered,
    @SerializedName("_embedded") val embedded: WpEmbedded?
)

interface WpApi {
    @GET("wp-json/wp/v2/posts")
    suspend fun getPosts(
        @Query("per_page") perPage: Int = 5,
        @Query("page") page: Int = 1,
        @Query("_embed") embed: Int = 1
    ): List<WpPost>
}
