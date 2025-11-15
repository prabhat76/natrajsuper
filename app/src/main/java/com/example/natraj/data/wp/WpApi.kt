package com.example.natraj.data.wp

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

data class WpRendered(val rendered: String)

data class WpMedia(@SerializedName("source_url") val sourceUrl: String?)

data class WpEmbedded(
    @SerializedName("wp:featuredmedia") val media: List<WpMedia>?,
    @SerializedName("wp:term") val terms: List<List<WpCategory>>?,
    @SerializedName("author") val author: List<WpUser>?
)

data class WpPost(
    val id: Int,
    val date: String,
    val link: String,
    val title: WpRendered,
    val excerpt: WpRendered,
    val content: WpRendered,
    val categories: List<Int>,
    val author: Int,
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

// Authentication models
data class WpLoginRequest(
    val username: String,
    val password: String
)

data class WpLoginResponse(
    val token: String,
    @SerializedName("user_email") val userEmail: String,
    @SerializedName("user_nicename") val userNicename: String,
    @SerializedName("user_display_name") val userDisplayName: String
)

data class WpRegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    @SerializedName("first_name") val firstName: String? = null,
    @SerializedName("last_name") val lastName: String? = null
)

data class WpRegisterResponse(
    val id: Int,
    val username: String,
    val email: String,
    @SerializedName("first_name") val firstName: String?,
    @SerializedName("last_name") val lastName: String?
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
    
    // Authentication endpoints (requires JWT Auth plugin)
    @POST("wp-json/jwt-auth/v1/token")
    suspend fun login(@Body request: WpLoginRequest): WpLoginResponse
    
    @POST("wp-json/wp/v2/users/register")
    suspend fun register(@Body request: WpRegisterRequest): WpRegisterResponse
}
