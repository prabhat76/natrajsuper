# WordPress API Integration Guide

## ✅ Integrated WordPress APIs

All WordPress REST APIs have been integrated into your Natraj Super app!

---

## 📋 Available APIs in Your App

### 1. **Get Blog Posts**
```kotlin
val repo = WpRepository(context)
val posts = repo.getRecentPosts(limit = 10)
// Returns: List<BlogPost>
```

### 2. **Get Banners (Carousel)**
```kotlin
val repo = WpRepository(context)
val banners = repo.getBanners()
// Returns: List<Banner> - Used in home carousel
```

### 3. **Get Offer Banners (Diwali Sale, etc.)**
```kotlin
val repo = WpRepository(context)
val offerBanners = repo.getOfferBanners()
// Returns: List<Banner> - Filtered for sale/offer/festival banners
```

### 4. **Get Pages**
```kotlin
val repo = WpRepository(context)
val pages = repo.getPages(limit = 10)
// Returns: List<WpPage>
// Contains: id, title, content, excerpt, link, date
```

### 5. **Get WordPress Categories**
```kotlin
val repo = WpRepository(context)
val categories = repo.getWpCategories(limit = 100)
// Returns: List<WpCategory>
// Contains: id, name, slug, description, count
```

### 6. **Get Tags**
```kotlin
val repo = WpRepository(context)
val tags = repo.getTags(limit = 100)
// Returns: List<WpTag>
// Contains: id, name, slug, description, count
```

### 7. **Get Users**
```kotlin
val repo = WpRepository(context)
val users = repo.getUsers(limit = 10)
// Returns: List<WpUser>
// Contains: id, name, slug, description, avatarUrls
```

### 8. **Get All Media**
```kotlin
val repo = WpRepository(context)
val media = repo.getAllMedia(limit = 50)
// Returns: List<WpMediaItem>
// Contains: id, title, sourceUrl, altText, caption
```

---

## 🎯 Currently Active in App

### **HomeFragment:**
- ✅ **Banner Carousel** - Fetches from `repo.getBanners()`
- ✅ **Offer Section** - Fetches from `repo.getOfferBanners()`
- ✅ **Blog Section** - Fetches from `repo.getRecentPosts()`

### **Features:**
- 🔄 **Auto-updates** when WordPress content changes
- 🎨 **Dynamic banners** from WordPress media
- 🛍️ **Click navigation** to products section
- ✅ **Custom toast messages** (green for success, red for error)
- 📱 **Offline fallback** to local data if API fails

---

## 🔌 API Endpoints Being Used

### Base URL: `https://www.natrajsuper.com`

| Endpoint | Method | Usage in App |
|----------|--------|--------------|
| `/wp-json/wp/v2/posts` | GET | Blog posts |
| `/wp-json/wp/v2/media` | GET | Banners & images |
| `/wp-json/wp/v2/pages` | GET | Available (not used yet) |
| `/wp-json/wp/v2/categories` | GET | Available (not used yet) |
| `/wp-json/wp/v2/tags` | GET | Available (not used yet) |
| `/wp-json/wp/v2/users` | GET | Available (not used yet) |
| `/wp-json/wc/v3/products` | GET | Products listing |
| `/wp-json/wc/v3/products/categories` | GET | Product categories |
| `/wp-json/wc/v3/orders` | POST | Create orders |

---

## 💡 Usage Examples

### Example 1: Display Pages in a New Activity
```kotlin
class PagesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        lifecycleScope.launch {
            try {
                val repo = WpRepository(this@PagesActivity)
                val pages = withContext(Dispatchers.IO) { repo.getPages() }
                
                pages.forEach { page ->
                    Log.d("Pages", "Title: ${page.title}")
                    Log.d("Pages", "Content: ${page.content}")
                }
            } catch (e: Exception) {
                Log.e("Pages", "Failed to fetch pages", e)
            }
        }
    }
}
```

### Example 2: Display WordPress Categories
```kotlin
lifecycleScope.launch {
    try {
        val repo = WpRepository(requireContext())
        val categories = withContext(Dispatchers.IO) { repo.getWpCategories() }
        
        categories.forEach { category ->
            Log.d("Categories", "${category.name}: ${category.count} posts")
        }
    } catch (e: Exception) {
        Log.e("Categories", "Failed to fetch categories", e)
    }
}
```

### Example 3: Display Authors/Users
```kotlin
lifecycleScope.launch {
    try {
        val repo = WpRepository(requireContext())
        val users = withContext(Dispatchers.IO) { repo.getUsers() }
        
        users.forEach { user ->
            Log.d("Users", "Author: ${user.name}")
            Log.d("Users", "Avatar: ${user.avatarUrls?.get("96")}")
        }
    } catch (e: Exception) {
        Log.e("Users", "Failed to fetch users", e)
    }
}
```

---

## 🧪 Testing in Postman

### No Authentication Required (WordPress APIs)
```
GET https://www.natrajsuper.com/wp-json/wp/v2/posts?per_page=5&_embed=1
GET https://www.natrajsuper.com/wp-json/wp/v2/media?search=banner
GET https://www.natrajsuper.com/wp-json/wp/v2/pages
GET https://www.natrajsuper.com/wp-json/wp/v2/categories
GET https://www.natrajsuper.com/wp-json/wp/v2/tags
GET https://www.natrajsuper.com/wp-json/wp/v2/users
```

### With Authentication (WooCommerce APIs)
```
GET https://www.natrajsuper.com/wp-json/wc/v3/products?consumer_key=YOUR_KEY&consumer_secret=YOUR_SECRET
```

---

## 📊 Data Models

### WpPage
```kotlin
data class WpPage(
    val id: Int,
    val date: String,
    val link: String,
    val title: String,
    val content: String,
    val excerpt: String
)
```

### WpCategory
```kotlin
data class WpCategory(
    val id: Int,
    val name: String,
    val slug: String,
    val description: String,
    val count: Int
)
```

### WpTag
```kotlin
data class WpTag(
    val id: Int,
    val name: String,
    val slug: String,
    val description: String,
    val count: Int
)
```

### WpUser
```kotlin
data class WpUser(
    val id: Int,
    val name: String,
    val slug: String,
    val description: String,
    val avatarUrls: Map<String, String>?
)
```

### WpMediaItem
```kotlin
data class WpMediaItem(
    val id: Int,
    val title: String,
    val sourceUrl: String,
    val altText: String?,
    val caption: String?
)
```

---

## 🎉 What's Working Now

1. ✅ **Banner carousel** fetches from WordPress media (searches for "banner")
2. ✅ **Offer section** fetches from WordPress media (searches for sale/offer/festival)
3. ✅ **Blog section** fetches latest posts from WordPress
4. ✅ **Banner clicks** navigate to AllProductsActivity
5. ✅ **Custom toast messages** (green/red) for feedback
6. ✅ **Auto-updates** when you change content in WordPress admin
7. ✅ **Fallback to local data** if WordPress API is unavailable

---

## 🚀 Next Steps

You can now use these APIs to:
- Create an "About Us" page by fetching from WordPress pages
- Display blog categories dynamically
- Show author information on blog posts
- Create a media gallery from WordPress media
- Filter blog posts by tags or categories
- Much more!

All the infrastructure is ready - just call the methods from `WpRepository` in any Fragment or Activity!
