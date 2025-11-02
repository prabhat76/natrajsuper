# Testing & Debugging API Integration

## Current Status

The app has been updated with:
- ✅ Emoji-free, professional UI design
- ✅ Detailed logging for API calls
- ✅ API Debug Activity to test connectivity
- ✅ Proper error messages and debugging

## How to Test the API Integration

### Step 1: Configure WordPress Settings

1. **Open the app**
2. **Tap Profile** (bottom right)
3. **Tap Settings** (gear icon)
4. **Enter your WordPress credentials:**
   - Base URL: `https://www.natrajsuper.com`
   - Consumer Key: Get from WooCommerce → Settings → API
   - Consumer Secret: Get from WooCommerce → Settings → API
5. **Tap Save**

### Step 2: Test API Connectivity

1. **Open the app again**
2. **Tap Profile** (bottom right)
3. **Tap Help & Support** (this is now the API Debug tool)
4. You'll see:
   - Current credentials status
   - Categories fetched
   - Products fetched
   - Any errors with details

### Step 3: View Live Data on Home Screen

Once credentials are configured:
1. **Close and reopen the app**
2. **Go to Home tab**
3. You should see:
   - Categories with images from WordPress
   - Featured Products from your store
   - Recommended Products
   - Blog articles from WordPress

## Understanding the Debug Output

### Sample Success Output:
```
=== API DEBUG ===

Base URL: https://www.natrajsuper.com
Consumer Key: ck_1234...
Consumer Secret: cs_5678...

Attempting to fetch categories...
Categories fetched: 5
  - Category 1
  - Category 2
  - Category 3

Attempting to fetch featured products...
Products fetched: 8
  - Product 1 (₹2500)
  - Product 2 (₹5000)
  - Product 3 (₹1500)

SUCCESS: API is working!
```

### Sample Error Output:
```
=== API DEBUG ===

Base URL: https://www.natrajsuper.com
Consumer Key: NOT SET
Consumer Secret: NOT SET

ERROR: Credentials not configured!
```

## Common Issues & Solutions

### Issue: "NOT SET" for credentials

**Cause**: Settings not saved in SharedPreferences

**Solution**:
1. Go back to Profile → Settings
2. Make sure all three fields are filled
3. Tap Save and wait for toast "Saved"
4. Close and reopen the app

### Issue: "403 Forbidden" error

**Cause**: Invalid Consumer Key or Secret

**Solution**:
1. Log into WordPress: https://www.natrajsuper.com/wp-admin
2. Go to WooCommerce → Settings → Advanced → REST API
3. Check if API key exists and is enabled
4. Regenerate the key if needed
5. Copy exact Consumer Key and Secret (no spaces)
6. Update in app settings

### Issue: "404 Not Found"

**Cause**: WooCommerce not installed or REST API disabled

**Solution**:
1. Verify WooCommerce is installed: https://www.natrajsuper.com/wp-admin/admin.php?page=wc-settings
2. Check REST API endpoint: Visit `https://www.natrajsuper.com/wp-json/wc/v3/products`
3. Should return JSON (even if 401 Unauthorized with no auth)

### Issue: "401 Unauthorized"

**Cause**: Credentials rejected

**Solution**:
1. Verify Consumer Key and Secret are correct (case-sensitive)
2. Check if API key has "Read" permission
3. Try regenerating the API key

### Issue: Empty categories/products on Home

**Cause**: API is working but no data in store

**Solution**:
1. Log into WordPress admin
2. Add test products and categories
3. Mark some products as "Featured"
4. Refresh app

## Checking Logs Manually

### View Logcat Output:

```bash
# Clear previous logs
adb logcat -c

# View all logs related to API
adb logcat | grep "HomeFragment\|WooRepository\|DEBUG_CREDENTIALS"
```

### Expected Log Output:

```
HomeFragment: DEBUG_CREDENTIALS: baseUrl=https://..., ck=ck_1234..., cs=cs_5678...
HomeFragment: Credentials found, fetching categories from Woo...
HomeFragment: Calling repo.getCategories()...
HomeFragment: Categories fetched: 5 categories
HomeFragment: setupProducts: baseUrl=https://..., ck=ck_1234..., cs=cs_5678...
HomeFragment: Fetching featured products...
HomeFragment: Featured products fetched: 8
```

## Manual API Testing

### Test categories endpoint:

```bash
curl -X GET "https://www.natrajsuper.com/wp-json/wc/v3/products/categories?per_page=5&consumer_key=YOUR_KEY&consumer_secret=YOUR_SECRET"
```

### Test products endpoint:

```bash
curl -X GET "https://www.natrajsuper.com/wp-json/wc/v3/products?per_page=5&consumer_key=YOUR_KEY&consumer_secret=YOUR_SECRET"
```

Should return JSON arrays with data.

## Verifying Installation

Make sure the app has been rebuilt after code changes:

```bash
cd /Users/prabhatkumar/Desktop/akshay

# Build
./gradlew clean assembleDebug

# Install
adb install -r app/build/outputs/apk/debug/app-universal-debug.apk

# Launch
adb shell am start -n com.example.natraj/.SplashActivity
```

## File Structure

The API integration is organized as:
- `data/woo/WooClient.kt` - HTTP client with auth
- `data/woo/WooApi.kt` - Retrofit service interface
- `data/WooRepository.kt` - Data mapping and business logic
- `HomeFragment.kt` - UI with logging
- `APIDebugActivity.kt` - Debug tool for testing

## Next Steps

1. **Configure credentials** in app settings
2. **Test API Debug** tool to verify connectivity
3. **Check Home screen** for live data
4. **Review logcat** for any errors
5. **Report issues** with log output if problems persist

## Support

If data still doesn't show:
1. Run API Debug tool and take screenshot
2. Check logcat output
3. Verify WordPress site is accessible
4. Ensure WooCommerce has products and categories

