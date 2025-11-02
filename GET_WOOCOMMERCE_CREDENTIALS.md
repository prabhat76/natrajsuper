# 🔑 Getting Your WooCommerce API Credentials

## Quick Guide: 5-Minute Setup

### Prerequisites
✅ WordPress site running (https://www.natrajsuper.com)
✅ WooCommerce plugin installed and activated
✅ Admin access to your WordPress dashboard

---

## Steps

### 1️⃣ Log into WordPress Admin

**URL**: `https://www.natrajsuper.com/wp-admin`

Enter your admin username and password.

---

### 2️⃣ Navigate to REST API Settings

From the left sidebar:
- Click **WooCommerce** 
- Click **Settings**
- Look for and click the **Advanced** tab (or **Developers** in newer versions)
- Click **REST API** (or **API** link)

---

### 3️⃣ Create New API Key

Click the button labeled:
- "Create an API key" or 
- "Create API credentials" or
- "Add key"

---

### 4️⃣ Fill in the Form

| Field | Value |
|-------|-------|
| **Description** | `Natraj Mobile App` (or any name) |
| **User** | Select your account from dropdown |
| **Permissions** | Select `Read` |

**Why "Read"?** - The app only needs to fetch (read) data, not modify it.

---

### 5️⃣ Generate & Copy Credentials

Click **Generate API key**

You'll see a success message with two keys displayed:

```
Consumer key:     ck_1234567890abcdefghijklmnopqrst
Consumer secret:  cs_0987654321zyxwvutsrqponmlkjih
```

⚠️ **IMPORTANT**: 
- **Copy these values NOW** - you won't see them again!
- The "Consumer secret" is like a password - keep it private
- Paste them into a text editor temporarily

---

### 6️⃣ Configure the App

Open **Natraj Super** app:

1. Tap **Profile** (bottom right icon)
2. Tap **Settings** (⚙️ icon)
3. Fill in:
   - **Base URL**: `https://www.natrajsuper.com`
   - **Consumer Key**: `ck_1234567890...` (from step 5)
   - **Consumer Secret**: `cs_0987654321...` (from step 5)
4. Tap **Save**

---

### 7️⃣ Restart & Verify

1. Close the app completely
2. Reopen it
3. Go to **Home** tab
4. You should now see:
   - Real categories with images
   - Real products from your store
   - Blog posts from your WordPress site

✅ **Done!** Your app is now connected to your WordPress store.

---

## Troubleshooting

### "REST API" option doesn't appear in WooCommerce Settings

**Solution**: 
- Check WooCommerce version: **Settings** → **General** should show version
- Must be WooCommerce 3.0 or higher
- If older, update WooCommerce plugin

### Can't find the API section

**Alternative method**:
1. Go to WordPress: **Settings** → **Permalinks**
2. Make sure it's NOT set to "Plain" (use Post name)
3. Then go to **Extensions** (if you see it) or search "REST API" in WooCommerce

### API key doesn't work in the app

**Verify**:
1. Correct spelling (copy-paste, case-sensitive)
2. No extra spaces before/after
3. Correct permissions: should show `read` in WordPress admin
4. URL format: `https://` (not `http://`), ends without `/`

### Still not working?

Run this command in terminal to test if API is accessible:

```bash
curl -X GET "https://www.natrajsuper.com/wp-json/wc/v3/products" \
  -H "Authorization: Basic $(echo -n 'ck_xxx:cs_xxx' | base64)"
```

Replace `ck_xxx` and `cs_xxx` with your actual credentials.

If it returns product data → API works, check app settings.
If it returns error → API credentials or permissions issue.

---

## ✅ Checklist

Before considering setup complete:

- [ ] API key created in WooCommerce admin
- [ ] Consumer Key copied
- [ ] Consumer Secret copied
- [ ] Base URL entered in app (https://www.natrajsuper.com)
- [ ] Consumer Key entered in app
- [ ] Consumer Secret entered in app
- [ ] Settings saved
- [ ] App restarted
- [ ] Home screen shows real categories and images

---

## 🔒 Security Reminder

- Don't share your Consumer Secret publicly
- In production app, use encrypted storage (not plain SharedPreferences)
- Regenerate keys if you suspect compromise
- Use "Read" permission for fetching data only

