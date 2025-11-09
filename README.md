# Natraj Super - E-commerce App with WordPress/WooCommerce Integration

A modern Android e-commerce application for agricultural equipment with full WordPress/WooCommerce backend integration.

## � **Key Features - All Working!**

### ✅ **WordPress/WooCommerce Integration**
- **Products**: Fetched from WooCommerce API in real-time
- **Categories**: Synced with WooCommerce product categories  
- **Orders**: Created directly in WooCommerce backend
- **Tracking**: Delhivery shipment tracking integration
- **Payment Gateways**: Dynamic payment methods from WooCommerce

### ✅ **Complete Order Flow**
1. **Browse Products** - Real-time from `https://www.natrajsuper.com`
2. **Add to Cart** - Smart cart management
3. **Checkout** - Address validation & verification
4. **Payment Selection** - COD, Online, Bank Transfer
5. **Order Placement** - Creates order in WooCommerce
6. **Order Confirmation** - With tracking information
7. **Order History** - View all orders in Profile

### ✅ **Order Tracking**
- **Delhivery Integration**: Real-time shipment tracking
- **AWB Number**: Automatic tracking number management
- **Track Package**: Direct link to Delhivery tracking page
- **Order Status**: Synced with WooCommerce order status

### ✅ **Key Functionality**
- **Product Catalog**: Browse products from WordPress
- **Shopping Cart**: Add/remove items with quantity management
- **Categories**: Organized categories from WooCommerce
- **Search**: Find products by name or category
- **User Profile**: Login, orders, settings
- **Address Management**: Multiple delivery addresses

## 🚀 Technical Stack

- **Language**: Kotlin
- **Backend**: WordPress + WooCommerce REST API v3
- **UI Framework**: Android Views with Material Design 3
- **Image Loading**: Glide
- **Networking**: Retrofit 2 + OkHttp3
- **Authentication**: OAuth1 for WooCommerce API
- **Data**: SharedPreferences + WooCommerce backend
- **Architecture**: Repository pattern with Coroutines

## 📡 API Integration

### WooCommerce Endpoints Used:
- `GET /wp-json/wc/v3/products` - Fetch products
- `GET /wp-json/wc/v3/products/categories` - Fetch categories
- `POST /wp-json/wc/v3/orders` - Create order
- `GET /wp-json/wc/v3/orders/{id}` - Get order details
- `PUT /wp-json/wc/v3/orders/{id}` - Update order (for tracking)
- `GET /wp-json/wc/v3/payment_gateways` - List payment methods

### WordPress Configuration:
- **Site**: `https://www.natrajsuper.com`
- **API**: WooCommerce REST API v3
- **Auth**: Consumer Key & Secret (OAuth1)
- **Caching**: HTTP cache (5 min) + Memory cache

## 📱 App Structure

### Main Activities
- `MainActivity`: Main container with bottom navigation (Home, Categories, Cart, Profile)
- `AllProductsActivity`: Grid view of products with category filters
- `ProductDetailActivity`: Detailed product view with add to cart
- `AddressActivity`: Delivery address form with validation
- `PaymentActivity`: Payment method selection and order placement
- `OrderConfirmationActivity`: Order success with tracking info
- `OrdersActivity`: Order history and status

### Data Layer
- `WooRepository`: WooCommerce API operations with caching
- `WooClient`: Retrofit client with OAuth1 authentication
- `WooApi`: API endpoints definition
- `WooModels`: Data models for products, orders, tracking
- `DelhiveryTrackingManager`: Shipment tracking operations

### Managers
- `CartManager`: Shopping cart state management
- `AuthManager`: User authentication
- `OrderManager`: Local order storage (fallback)

## 🛠️ Setup & Installation

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 24+
- WordPress site with WooCommerce plugin
- WooCommerce REST API credentials

### Installation Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/prabhat76/natrajsuper.git
   cd natrajsuper
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - File → Open → Select project folder
   - Wait for Gradle sync

3. **Configure WooCommerce** (Already configured!)
   - Site URL: `https://www.natrajsuper.com`
   - Consumer Key: `ck_60e3de7255dafa3b78eeb2d96fec395cb0ceb19c`
   - Consumer Secret: `cs_2f8926db30ebb4366d135c1150ccbdd9cdb2b211`
   - (Credentials are pre-configured in `NatrajApplication.kt`)

4. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   adb install -r app/build/outputs/apk/debug/app-universal-debug.apk
   ```

## 🧪 Testing

See [TESTING_GUIDE.md](./TESTING_GUIDE.md) for comprehensive testing instructions including:
- How to place orders
- How to add tracking information
- How to verify orders in WooCommerce
- How to monitor logs
- Troubleshooting tips

### Quick Test:
```bash
# Monitor order placement
adb logcat | grep "PaymentActivity\|OrderConfirmation"

# Launch app
adb shell monkey -p com.example.natraj 1
```

## 📦 Key Dependencies

```gradle
// Networking
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
implementation 'com.squareup.okhttp3:okhttp:4.11.0'
implementation 'com.squareup.okhttp3:logging-interceptor:4.11.0'
implementation 'se.akerfeldt:okhttp-signpost:1.1.0'

// Image Loading
implementation 'com.github.bumptech.glide:glide:4.15.1'

// Coroutines
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1'

// Material Design
implementation 'com.google.android.material:material:1.9.0'
```

## 🎨 Design & Branding

- **Brand**: Natraj Super ("नत्राज")
- **Colors**: Orange (#FF6B35), Red (#D32F2F), Gold (#FFB300)
- **Typography**: Mix of English and Hindi
- **Theme**: Agricultural equipment, festive, modern

## 📊 Data Flow

```
User Action
    ↓
Activity/Fragment
    ↓
Repository (with caching)
    ↓
Retrofit API Client
    ↓
WooCommerce REST API
    ↓
WordPress Backend
```

## 🔍 Monitoring & Debugging

### Enable detailed logging:
```bash
# All app logs
adb logcat | grep "com.example.natraj"

# Payment flow
adb logcat | grep "PaymentActivity"

# Order confirmation
adb logcat | grep "OrderConfirmation"

# WooCommerce operations
adb logcat | grep "WooRepository\|WooClient"

# Tracking operations
adb logcat | grep "DelhiveryTracking"
```

## 📋 Features Roadmap

### ✅ Completed
- [x] WordPress/WooCommerce integration
- [x] Product catalog from WooCommerce
- [x] Category filtering
- [x] Shopping cart
- [x] Order placement in WooCommerce
- [x] Payment gateway selection
- [x] Order confirmation
- [x] Delhivery tracking support
- [x] Order history
- [x] HTTP & memory caching
- [x] Comprehensive logging

### 🚧 Planned
- [ ] User registration via WordPress
- [ ] JWT authentication
- [ ] Wishlist sync with WooCommerce
- [ ] Product reviews from WooCommerce
- [ ] Push notifications for order updates
- [ ] Auto-tracking status updates
- [ ] Multiple payment gateway integration
- [ ] Order filtering and search

## 📄 License

This project is for educational and demonstration purposes.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit pull requests.

## 📞 Support

For issues or questions:
- Check [TESTING_GUIDE.md](./TESTING_GUIDE.md)
- Review logs using monitoring commands above
- Check WooCommerce admin for order status

---

**Status**: ✅ **Fully Functional** - All core features working with WordPress/WooCommerce  
**Last Updated**: November 8, 2025  
**Version**: 1.0.0 (Full WordPress Integration)