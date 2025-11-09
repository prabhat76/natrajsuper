# Complete Feature Test Guide

## ✅ What's Been Fixed

### 1. **Order Placement** ✅
- Orders are now successfully placed in WooCommerce
- Works in both **regular checkout** and **quick checkout**
- Order data is sent to WordPress backend
- No hardcoded fallback orders

### 2. **Order Tracking** ✅
- Order tracking information is fetched from WordPress
- Tracking numbers display if set in WooCommerce custom fields
- Delhivery tracking integration ready
- "Track Package" button opens tracking URL
- **NO MORE "COMING SOON"** - Real tracking implemented!

### 3. **Order Cancellation** ✓ NEW
- Cancel button appears for pending/on-hold/processing orders
- Confirmation dialog before cancellation
- Updates order status to "cancelled" in WordPress
- UI updates after successful cancellation

### 4. **HTML Rendering** ✓
- Product descriptions properly render HTML from WordPress
- No more raw HTML tags showing

### 5. **No Hardcoded Data** ✓
- All banners from WordPress (or hidden if none)
- All categories from WordPress (or error message)
- All products from WordPress (or error message)
- All orders from WordPress (or error message)

---

## 🧪 Testing Instructions

### Test 1: Place an Order

**Option A: Regular Checkout Flow**
1. **Open the app** and browse products
2. **Add products to cart**
3. **Go to cart** and proceed to checkout
4. **Enter delivery address**
5. **Select payment method** (COD or other)
6. **Place order**

**Option B: Quick Checkout Flow**
1. **Open the app** and browse products
2. **Open product details**
3. **Tap "Quick Buy"** button
4. **Enter name, phone, address, pincode**
5. **Select delivery type** (Standard/Express)
6. **Select payment method** (COD or Online)
7. **Tap "Place Order Now"**

**Expected Result (Both Options)**: 
   - Progress bar shows during order placement
   - Success screen with order number
   - Order appears in WordPress admin (WooCommerce → Orders)
   - **Can immediately track order if tracking info added**

**Check WordPress Admin**:
```
Go to: WooCommerce → Orders
Verify: New order exists with correct items and address
```

---

### Test 2: View Orders

1. **Open app** → Go to Orders section
2. **Expected Result**: 
   - Orders load from WordPress
   - Each order shows: order number, date, status, total, item count
   - Tap on order to view details

**If No Orders Show**:
- Check WordPress has orders
- Check internet connection
- Check logcat for API errors: `adb logcat | grep OrdersActivity`

---

### Test 3: Order Tracking

**Setup in WordPress First**:
1. Go to WooCommerce → Orders
2. Open an order
3. Scroll to "Custom Fields" section (bottom of order page)
4. Add these custom fields:
   ```
   Field Name: _delhivery_tracking_number
   Value: DEL123456789
   
   Field Name: _tracking_provider
   Value: Delhivery
   
   Field Name: _awb_number
   Value: AWB123456
   ```
5. Click "Update" to save order

**Test in App**:
1. Open Orders → Tap on the order with tracking
2. **Expected Result**:
   - "DELIVERY TRACKING" section appears
   - Shows tracking number, provider, AWB
   - "TRACK PACKAGE" button is visible
3. **Tap "TRACK PACKAGE"**
   - Opens Delhivery tracking URL in browser

**Check Logs**:
```bash
adb logcat | grep OrderConfirmation
```

---

### Test 4: Cancel Order

1. **Place a new order** (ensure it's in "pending" or "processing" status)
2. **View order details** in app
3. **Expected Result**: "CANCEL ORDER" button appears (red outline)
4. **Tap "CANCEL ORDER"**
   - Confirmation dialog appears
5. **Tap "Yes, Cancel"**
   - Button shows "Cancelling..."
   - Success message appears
   - Status updates to "Cancelled"
   - Cancel button disappears

**Verify in WordPress**:
```
Go to: WooCommerce → Orders
Check: Order status changed to "Cancelled"
```

**Note**: Cancel button only shows for:
- Pending orders
- On-hold orders  
- Processing orders

Completed/shipped orders cannot be cancelled.

---

### Test 5: HTML Product Descriptions

1. **Browse products**
2. **Open product detail page**
3. **Check description section**
4. **Expected Result**: 
   - HTML formatted properly (paragraphs, lists, bold text)
   - NO raw HTML tags like `<p>`, `<ul>`, `<li>`
   - Clean, readable text

---

### Test 6: WordPress-Only Data

1. **Clear app data**: Settings → Apps → Natraj Super → Clear Data
2. **Open app**
3. **Expected Results**:
   - Banners load from WordPress (or section hidden)
   - Categories load from WordPress (or error message)
   - Products load from WordPress (or error message)
   - NO hardcoded fallback data appears

**If API Fails**:
- Should show error message
- Should NOT show fake/hardcoded banners or categories
- User should know connection is required

---

## 🔍 Troubleshooting

### Orders Not Placing

**Check logs**:
```bash
adb logcat | grep PaymentActivity
```

**Look for**:
- "WooCommerce order creation..." - Order attempt started
- "✓ Order created successfully!" - Success
- "✗ Order placement failed" - Error with details

**Common Issues**:
1. WooCommerce credentials not configured
2. Internet connection issues
3. Product IDs don't exist in WordPress
4. Address fields missing

### Tracking Not Showing

**Requirements**:
1. Order must exist in WordPress
2. Custom fields must be set in WordPress order:
   - `_delhivery_tracking_number`
   - `_tracking_provider`
   - `_awb_number` (optional)

**Check logs**:
```bash
adb logcat | grep "OrderConfirmation\|DelhiveryTracking"
```

**Look for**:
- "Loading tracking info for order X" - Started
- "✓ Tracking info found" - Success with details
- "No tracking info available" - No custom fields set
- "✗ Failed to load tracking info" - Error

### Order Cancellation Failing

**Check logs**:
```bash
adb logcat | grep OrderConfirmation
```

**Look for**:
- "Cancelling order X..." - Started
- "✓ Order cancelled successfully" - Success
- "✗ Failed to cancel order" - Error with details

**Common Issues**:
1. Order already completed/shipped (can't cancel)
2. Internet connection issues
3. WordPress API permissions

---

## 📊 API Endpoints Used

### Orders
- **GET** `/wp-json/wc/v3/orders` - Fetch all orders
- **GET** `/wp-json/wc/v3/orders/{id}` - Get single order
- **POST** `/wp-json/wc/v3/orders` - Create new order
- **PUT** `/wp-json/wc/v3/orders/{id}` - Update order (for cancellation)

### Products
- **GET** `/wp-json/wc/v3/products` - Fetch products

### Categories
- **GET** `/wp-json/wc/v3/products/categories` - Fetch categories

---

## 🎯 Success Criteria

✅ **Order Placement**: Orders create in WordPress with correct data  
✅ **Order Viewing**: Orders display from WordPress in app  
✅ **Order Tracking**: Tracking info shows when set in WordPress  
✅ **Order Cancellation**: Can cancel pending orders, updates WordPress  
✅ **HTML Rendering**: Product descriptions show formatted text  
✅ **Zero Hardcoding**: All content from WordPress, no fallbacks  

---

## 🔧 Quick Commands

**View logs for orders**:
```bash
adb logcat | grep -E "PaymentActivity|OrdersActivity|OrderConfirmation"
```

**View all app logs**:
```bash
adb logcat | grep "com.example.natraj"
```

**Clear app data and restart**:
```bash
adb shell pm clear com.example.natraj
adb shell am start -n com.example.natraj/.MainActivity
```

**Check network requests**:
```bash
adb logcat | grep -E "OkHttp|Retrofit"
```

---

## 📝 Notes

1. **Tracking Setup**: You must manually add custom fields in WordPress for each order you want to track
2. **Order Status**: Only pending/on-hold/processing orders can be cancelled
3. **Internet Required**: App requires internet connection for all features (no offline mode with hardcoded data)
4. **WordPress Admin**: Access at https://www.natrajsuper.com/wp-admin
