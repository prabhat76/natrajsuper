# Natraj E-commerce App - Diwali Festival Theme

A modern Android e-commerce application for agricultural equipment with a festive Diwali theme.

## 🎆 Features

### Core Functionality
- **Product Catalog**: Browse agricultural equipment with detailed specifications
- **Shopping Cart**: Add/remove items with quantity management
- **Categories**: Organized product categories with visual icons
- **Search**: Find products by name, category, or article code
- **WhatsApp Integration**: Direct ordering via WhatsApp

### Design & UI
- **Festive Theme**: Orange, red, and gold Diwali color scheme
- **Traditional Branding**: "नत्राज" Hindi branding
- **Modern UI**: Material Design 3 components
- **Responsive Layout**: Optimized for different screen sizes

### Product Features
- High-quality product images with Glide loading
- Rating system and review counts
- Discount badges and pricing
- Detailed specifications and features
- Multiple product images carousel
- Wishlist functionality

## 🚀 Technical Stack

- **Language**: Kotlin
- **UI Framework**: Android Views with Material Design 3
- **Image Loading**: Glide
- **Data Storage**: SharedPreferences + JSON assets
- **Architecture**: MVVM pattern with managers
- **Navigation**: Fragment-based with bottom navigation

## 📱 App Structure

### Main Components
- `MainActivity`: Main container with bottom navigation
- `HomeFragment`: Featured products and banners
- `CategoriesFragment`: Product categories grid
- `CartFragment`: Shopping cart management
- `ProductDetailActivity`: Detailed product view

### Data Management
- `ProductManager`: Handles product data loading from JSON
- `CartManager`: Manages shopping cart state with persistence
- `WishlistManager`: Handles favorite products

### Product Categories
- Agriculture Sprayers
- Air Compressors
- Chaff Cutters
- Earth Augers
- Water Pumps
- Welding Machines
- Pressure Washers
- Electric Motors

## 🛠️ Setup & Installation

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Run on device/emulator (API 24+)

## 📦 Dependencies

- AndroidX Core, AppCompat, Material Design
- ViewPager2, RecyclerView, CardView
- Glide for image loading
- Gson for JSON parsing
- Retrofit & OkHttp for networking
- Kotlin Coroutines

## 🎨 Design Elements

- **Colors**: Festival oranges (#FF6B35), reds (#D32F2F), golds (#FFB300)
- **Typography**: Mix of English and Hindi text
- **Icons**: Agricultural and festive themes
- **Gradients**: Warm festive backgrounds

## 📄 License

This project is for educational and demonstration purposes.

# Test change by Ahmed