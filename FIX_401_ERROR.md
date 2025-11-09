# Fix 401 Unauthorized Error

## Problem
Getting **HTTP 401 Unauthorized** when trying to place orders in WooCommerce.

## Root Cause
The WooCommerce API keys need **Read/Write** permissions, but they might only have **Read** permissions.

---

## Solution: Regenerate API Keys with Write Permissions

### Step 1: Login to WordPress Admin
1. Go to: https://www.natrajsuper.com/wp-admin
2. Login with your credentials

### Step 2: Navigate to WooCommerce API Settings
1. In WordPress admin, go to: **WooCommerce** → **Settings**
2. Click on **Advanced** tab
3. Click on **REST API** sub-tab
4. You should see existing API keys listed

### Step 3: Check Current API Key Permissions
1. Find the API key: `ck_60e3de7255dafa3b78eeb2d96fec395cb0ceb19c`
2. Click **Edit** or **View**
3. Check the **Permissions** field
4. If it says **"Read"** or **"Read only"**, that's the problem!

### Step 4: Create New API Key with Write Permissions
1. Click **Add key** button
2. Fill in the form:
   - **Description**: `Android App - Full Access`
   - **User**: Select an Administrator user
   - **Permissions**: Select **"Read/Write"** ✓✓✓ (IMPORTANT!)
3. Click **Generate API key**

### Step 5: Copy the New Keys
WordPress will show you:
```
Consumer key: ck_xxxxxxxxxxxxxxxxxxxxx
Consumer secret: cs_yyyyyyyyyyyyyyyyyyyyy
```

**⚠️ IMPORTANT**: Copy these immediately! WordPress will only show them once.

### Step 6: Update Keys in App

**Option A: Via App Settings (If you have a settings screen)**
1. Open app → Go to Settings/Profile
2. Enter new Consumer Key and Secret
3. Save

**Option B: Via ADB Command (Quick method)**
```bash
# Clear existing keys
adb shell "run-as com.example.natraj rm /data/data/com.example.natraj/shared_prefs/woo_settings.xml"

# Restart app and enter new keys through setup
```

**Option C: Direct XML Update (Advanced)**
Create a new `woo_settings.xml` with new keys and push it:
```bash
# Create file with new keys
cat > /tmp/woo_settings.xml << 'EOF'
<?xml version='1.0' encoding='utf-8' standalone='yes' ?>
<map>
    <string name="base_url">https://www.natrajsuper.com</string>
    <string name="consumer_key">YOUR_NEW_CONSUMER_KEY_HERE</string>
    <string name="consumer_secret">YOUR_NEW_CONSUMER_SECRET_HERE</string>
</map>
EOF

# Push to device
adb push /tmp/woo_settings.xml /sdcard/woo_settings.xml
adb shell "run-as com.example.natraj cp /sdcard/woo_settings.xml /data/data/com.example.natraj/shared_prefs/woo_settings.xml"
adb shell "run-as com.example.natraj chmod 660 /data/data/com.example.natraj/shared_prefs/woo_settings.xml"

# Restart app
adb shell am force-stop com.example.natraj
```

---

## Verify Permissions in WordPress

### Current API Keys to Check:

**Consumer Key**: `ck_60e3de7255dafa3b78eeb2d96fec395cb0ceb19c`

Go to: https://www.natrajsuper.com/wp-admin/admin.php?page=wc-settings&tab=advanced&section=keys

1. Find this key in the list
2. Check **Permissions** column
3. Should show: **Read/Write** ✓
4. If it shows **Read**, click Edit and change to **Read/Write**

---

## Test After Updating

### Test 1: Check API Access
```bash
# Test with new credentials
curl -X GET "https://www.natrajsuper.com/wp-json/wc/v3/products?consumer_key=YOUR_KEY&consumer_secret=YOUR_SECRET" -H "Content-Type: application/json"
```

Should return product list (not 401 error).

### Test 2: Test Order Creation
```bash
curl -X POST "https://www.natrajsuper.com/wp-json/wc/v3/orders?consumer_key=YOUR_KEY&consumer_secret=YOUR_SECRET" \
  -H "Content-Type: application/json" \
  -d '{
    "payment_method": "cod",
    "payment_method_title": "Cash on Delivery",
    "set_paid": false,
    "billing": {
      "first_name": "Test",
      "last_name": "User",
      "address_1": "Test Address",
      "city": "Test City",
      "state": "TN",
      "postcode": "600001",
      "country": "IN",
      "email": "test@example.com",
      "phone": "9876543210"
    },
    "line_items": [
      {
        "product_id": 123,
        "quantity": 1
      }
    ]
  }'
```

Should return order details (not 401 error).

### Test 3: Place Order in App
1. Open app
2. Browse products
3. Add to cart
4. Checkout
5. Place order
6. Should succeed ✓

---

## Common Issues

### Issue 1: Still Getting 401 After Updating Keys
**Solution**: 
- Make sure you copied the FULL key (they're very long)
- Make sure no extra spaces before/after
- Restart the app completely
- Clear app data if needed

### Issue 2: Can Read Products But Can't Create Orders
**Solution**: 
- The key has **Read** permission only
- Must regenerate with **Read/Write** permission

### Issue 3: Consumer Secret Not Working
**Solution**:
- Consumer Secret is only shown ONCE when created
- If you lost it, delete the old key and create a new one
- Cannot retrieve the secret later

---

## Quick Fix Command

If you have the new API keys, run this:

```bash
#!/bin/bash
# Replace with your new keys
NEW_KEY="ck_your_new_key_here"
NEW_SECRET="cs_your_new_secret_here"

# Update app settings
adb shell "run-as com.example.natraj rm /data/data/com.example.natraj/shared_prefs/woo_settings.xml"

cat > /tmp/woo_settings.xml << EOF
<?xml version='1.0' encoding='utf-8' standalone='yes' ?>
<map>
    <string name="base_url">https://www.natrajsuper.com</string>
    <string name="consumer_key">$NEW_KEY</string>
    <string name="consumer_secret">$NEW_SECRET</string>
</map>
EOF

adb push /tmp/woo_settings.xml /sdcard/woo_settings.xml
adb shell "run-as com.example.natraj cp /sdcard/woo_settings.xml /data/data/com.example.natraj/shared_prefs/woo_settings.xml"
adb shell "run-as com.example.natraj chmod 660 /data/data/com.example.natraj/shared_prefs/woo_settings.xml"
adb shell am force-stop com.example.natraj

echo "✓ API keys updated! Open the app and try placing an order."
```

---

## Summary

✅ **The 401 error means**: API keys don't have write permission  
✅ **Solution**: Generate new keys with **Read/Write** permission  
✅ **Where**: WooCommerce → Settings → Advanced → REST API  
✅ **Then**: Update keys in app and restart  

After fixing, orders will create successfully! 🎉
