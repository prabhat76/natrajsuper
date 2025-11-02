# WordPress/WooCommerce Setup Guide - Natraj Super App

## ✅ What Was Fixed

### Lint Errors (48 → 0)
All critical lint errors have been resolved:
- ✅ `onBackPressed()` now calls `super.onBackPressed()`
- ✅ RecyclerView `setHasFixedSize()` issues fixed
- ✅ API level incompatibilities moved to version-specific folders
- ✅ Permissions restructured using `<queries>` element
- ✅ Indentation errors in BlogManager.kt and OfferManager.kt corrected

### Build Status
- ✅ **Build**: SUCCESSFUL (zero errors)
- ✅ **Lint**: SUCCESSFUL (zero errors)
- ✅ **Install**: SUCCESSFUL

---

## ⚠️ Why You're Seeing Mock Data

**The WordPress/WooCommerce API credentials are not configured.**

The app has two modes:
1. **WooCommerce Mode** (Active when credentials are saved) - Fetches REAL data from your WordPress site
2. **Mock Mode** (Active when credentials are missing) - Shows fallback mock data

### Current Status in Your App:
```
Categories: Shows toast "Configure WordPress settings to load categories"
Products:  Shows toast "Configure WordPress settings to load products"
Blog:      Falls back to local assets
```

---

## 🔧 Step-by-Step Setup

### Step 1: Get WooCommerce API Credentials

1. **Log into your WordPress Admin:**
   - URL: `https://www.natrajsuper.com/wp-admin`
   - Username & Password: (your credentials)

2. **Navigate to REST API Keys:**
   - Left menu → **WooCommerce** → **Settings**
   - Click **Advanced** tab
   - Click **REST API** (or **Developers** tab)

3. **Create New API Key:**
   - Click "Create an API key"
   - **Description**: "Natraj Mobile App"
   - **User**: Select your account
   - **Permissions**: Select **"Read"** (minimum for fetching data)
   - Click **Generate API key**

4. **Copy Your Credentials:**
   - Copy and save:
     - **Consumer Key**: `ck_xxxxxxx...`
     - **Consumer Secret**: `cs_xxxxxxx...`

---

### Step 2: Configure App Settings

**Option A: Using the App UI**

1. Open the **Natraj Super** app
2. Tap **Profile** (bottom right)
3. Tap **Settings** (gear icon) or **WordPress Settings**
4. Enter:
   - **Base URL**: `https://www.natrajsuper.com`
   - **Consumer Key**: `ck_xxxxxxx...` (from Step 1)
   - **Consumer Secret**: `cs_xxxxxxx...` (from Step 1)
5. Tap **Save**
6. Restart the app or close and reopen the Home tab

**Option B: Via Shared Preferences (Advanced)**

```kotlin
// In your app initialization code:
val prefs = WooPrefs(context)
prefs.baseUrl = "https://www.natrajsuper.com"
prefs.consumerKey = "ck_xxxxxxx..."
prefs.consumerSecret = "cs_xxxxxxx..."
```

---

### Step 3: Verify Data is Loading

Once configured, check the following:

1. **Categories Section**
   - Should display categories from WooCommerce (not mock data)
   - Images should load from your WordPress site
   - Clicking a category should show real products

2. **Featured Products**
   - Should display products marked as "featured" in WooCommerce
   - Prices and images from your store

3. **Recommended Products**
   - Should display random products from your catalog

4. **Blog Section**
   - Should display posts from your WordPress blog
   - Clicking should open the original post URL

---

## 🐛 Debugging Tips

### If Data Still Doesn't Load:

1. **Check Network Logs:**
   ```
   adb logcat | grep "HomeFragment\|WooRepository\|WooApi"
   ```

2. **Verify Settings Are Saved:**
   ```
   adb shell "run-as com.example.natraj cat /data/data/com.example.natraj/shared_prefs/woo_settings.xml"
   ```

3. **Test API Endpoint Manually:**
   ```bash
   curl -X GET "https://www.natrajsuper.com/wp-json/wc/v3/products?consumer_key=ck_xxx&consumer_secret=cs_xxx"
   ```
   Should return JSON with product data.

4. **Check WooCommerce Version:**
   - Must be WooCommerce 3.0+ for REST API

5. **Verify API Permissions:**
   - API key must have "Read" permission at minimum

---

## 📋 What Each API Endpoint Fetches

| Screen | Endpoint | Type |
|--------|----------|------|
| Home - Categories | `/wp-json/wc/v3/products/categories` | GET |
| Home - Featured | `/wp-json/wc/v3/products?featured=true` | GET |
| Home - Recommended | `/wp-json/wc/v3/products` | GET |
| Home - Blog | `/wp-json/wp/v2/posts?_embed` | GET |
| Products by Category | `/wp-json/wc/v3/products?category=ID` | GET |
| All Products | `/wp-json/wc/v3/products` | GET |
| Place Order | `/wp-json/wc/v3/orders` | POST |

---

## 🚀 Testing Checklist

After configuration, verify:

- [ ] Categories load with images from WordPress
- [ ] Clicking "View All" on categories shows real products
- [ ] Featured Products section displays real data
- [ ] Recommended Products shows store catalog
- [ ] Blog section displays WordPress posts
- [ ] Product detail pages show real prices/descriptions
- [ ] Adding to cart works
- [ ] Placing order via WooCommerce works

---

## 📁 Key Files Modified

- `HomeFragment.kt` - Woo integration, removed mock fallbacks
- `WordPressSettingsActivity.kt` - Settings UI
- `WooClient.kt` & `WooApi.kt` - API clients
- `WooRepository.kt` - Data mapping layer
- `WpRepository.kt` - WordPress posts integration
- `ProfileFragment.kt` - Settings navigation

---

## 🔐 Security Notes

- **Consumer Key/Secret**: Store securely (encrypted SharedPreferences recommended for production)
- **API Permissions**: Use "Read" only unless you need write access
- **HTTPS**: Always use HTTPS URLs (https://example.com, not http://)
- **Base URL**: Should end without `/` (code adds it automatically)

---

## 💡 Common Issues & Solutions

### Issue: "Configure WordPress settings to load categories"

**Solution**: Go to Settings → WordPress Settings and enter credentials.

### Issue: Empty Category/Product Lists

**Solution**:
1. Verify API key has "Read" permission
2. Check WooCommerce is installed and activated on WordPress
3. Verify at least one product is in your store

### Issue: Connection Timeout

**Solution**:
1. Check network connectivity
2. Verify base URL is correct (include https://)
3. Check WordPress site is accessible: `curl https://www.natrajsuper.com`

### Issue: 401 Unauthorized

**Solution**:
1. Regenerate API key (incorrect Consumer Key/Secret)
2. Verify API key has correct permissions
3. Check spelling of credentials (case-sensitive)

### Issue: 404 Not Found

**Solution**:
1. Verify WooCommerce REST API is enabled
2. Check WordPress version is 4.4+
3. Verify URL structure: `https://site.com/wp-json/wc/v3/`

---

## 📞 Support

For issues:
1. Check the Debug Logs section above
2. Verify credentials are correct in WordPress admin
3. Test endpoint with curl command
4. Check network connectivity
5. Reinstall app after changing credentials

