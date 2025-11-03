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

data class WpMediaItem(
    val id: Int,
    val title: WpRendered,
    @SerializedName("source_url") val sourceUrl: String,
    @SerializedName("alt_text") val altText: String?,
    @SerializedName("caption") val caption: WpRendered?
)

data class WpPage(
    val id: Int,
    val date: String,
    val link: String,
    val title: WpRendered,
    val content: WpRendered,
    val excerpt: WpRendered,
    @SerializedName("_embedded") val embedded: WpEmbedded?
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
    @SerializedName("avatar_urls") val avatarUrls: Map<String, String>?
)

interface WpApi {
    @GET("wp-json/wp/v2/posts")
    suspend fun getPosts(
        @Query("per_page") perPage: Int = 5,
        @Query("page") page: Int = 1,
        @Query("_embed") embed: Int = 1
    ): List<WpPost>

    @GET("wp-json/wp/v2/media")
    suspend fun getMedia(
        @Query("per_page") perPage: Int = 10,
        @Query("search") search: String? = null
    ): List<WpMediaItem>

    @GET("wp-json/wp/v2/pages")
    suspend fun getPages(
        @Query("per_page") perPage: Int = 10,
        @Query("page") page: Int = 1,
        @Query("_embed") embed: Int = 1
    ): List<WpPage>

    @GET("wp-json/wp/v2/categories")
    suspend fun getCategories(
        @Query("per_page") perPage: Int = 100,
        @Query("page") page: Int = 1
    ): List<WpCategory>

    @GET("wp-json/wp/v2/tags")
    suspend fun getTags(
        @Query("per_page") perPage: Int = 100,
        @Query("page") page: Int = 1
    ): List<WpTag>

    @GET("wp-json/wp/v2/users")
    suspend fun getUsers(
        @Query("per_page") perPage: Int = 10,
        @Query("page") page: Int = 1
    ): List<WpUser>
}
