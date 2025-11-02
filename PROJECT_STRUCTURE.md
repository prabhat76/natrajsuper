# Natraj E-Commerce App - Project Structure

## 📁 Organized Frame Structure

```
app/src/main/
│
├── java/com/example/natraj/
│   ├── NatrajApplication.kt                 # App entry point
│   │
│   ├── ui/                                  # User Interface Layer
│   │   ├── activities/                      # Screen Activities
│   │   │   ├── MainActivity.kt              # Home/Main screen
│   │   │   ├── SplashActivity.kt            # App splash screen
│   │   │   ├── LoginActivity.kt             # User login
│   │   │   ├── SignupActivity.kt            # User registration
│   │   │   ├── ProductDetailActivity.kt     # Product detail view
│   │   │   ├── AllProductsActivity.kt       # All products listing
│   │   │   ├── Product360Activity.kt        # 360° product view
│   │   │   ├── CartActivity.kt              # Shopping cart
│   │   │   ├── AddressActivity.kt           # Address selection
│   │   │   ├── PaymentActivity.kt           # Payment method
│   │   │   ├── OrderConfirmationActivity.kt # Order confirmation
│   │   │   ├── OrdersActivity.kt            # Order history
│   │   │   ├── CatalogueActivity.kt         # PDF catalogue viewer
│   │   │   ├── BlogActivity.kt              # Blog articles
│   │   │   ├── WordPressSettingsActivity.kt # API settings
│   │   │   └── APIDebugActivity.kt          # API connectivity debug
│   │   │
│   │   ├── fragments/                       # Fragment Components
│   │   │   ├── HomeFragment.kt              # Home tab content
│   │   │   ├── CartFragment.kt              # Cart tab content
│   │   │   ├── CategoriesFragment.kt        # Categories tab
│   │   │   └── ProfileFragment.kt           # Profile tab content
│   │   │
│   │   └── adapters/                        # RecyclerView Adapters
│   │       ├── BannerAdapter.kt             # Promotional banners
│   │       ├── CategoryAdapter.kt           # Category listing
│   │       ├── SimpleCategoryAdapter.kt     # Simple category view
│   │       ├── ModernCategoryAdapter.kt     # Modern category design
│   │       ├── ProductAdapter.kt            # Product card adapter
│   │       ├── GridProductAdapter.kt        # Grid product layout
│   │       ├── HorizontalProductAdapter.kt  # Horizontal product scroll
│   │       ├── BlogAdapter.kt               # Blog articles list
│   │       ├── OfferAdapter.kt              # Promotional offers
│   │       ├── CartAdapter.kt               # Cart items list
│   │       ├── OrderAdapter.kt              # Order history list
│   │       ├── Product360Adapter.kt         # 360 view adapter
│   │       └── ProductImageCarouselAdapter.kt# Product image carousel
│   │
│   ├── data/                                # Data Layer
│   │   ├── model/                           # Data Models
│   │   │   ├── Product.kt                   # Product model
│   │   │   ├── Category.kt                  # Category model
│   │   │   ├── Order.kt                     # Order model
│   │   │   ├── CartItem.kt                  # Cart item model
│   │   │   ├── BlogPost.kt                  # Blog post model
│   │   │   ├── Offer.kt                     # Promotion offer model
│   │   │   ├── Banner.kt                    # Banner model
│   │   │   └── Address.kt                   # Delivery address model
│   │   │
│   │   ├── repository/                      # Repository Pattern (Data Access)
│   │   │   ├── WooRepository.kt             # WooCommerce API repository
│   │   │   └── WpRepository.kt              # WordPress API repository
│   │   │
│   │   ├── woo/                             # WooCommerce Integration
│   │   │   ├── WooClient.kt                 # Retrofit HTTP client
│   │   │   ├── WooApi.kt                    # Retrofit service interface
│   │   │   ├── WooPrefs.kt                  # SharedPreferences for Woo
│   │   │   └── models/                      # WooCommerce response models
│   │   │       ├── WooCategory.kt
│   │   │       ├── WooProduct.kt
│   │   │       ├── WooBilling.kt
│   │   │       ├── WooShipping.kt
│   │   │       ├── WooOrderLineItem.kt
│   │   │       └── WooOrder.kt
│   │   │
│   │   └── wp/                              # WordPress Integration
│   │       ├── WpClient.kt                  # WordPress HTTP client
│   │       ├── WpApi.kt                     # WordPress service interface
│   │       └── models/                      # WordPress response models
│   │           └── WpPost.kt
│   │
│   └── util/                                # Utility & Helper Classes
│       └── manager/                         # Business Logic Managers
│           ├── AuthManager.kt               # Authentication logic
│           ├── CartManager.kt               # Cart management
│           ├── ProductManager.kt            # Product data management
│           ├── BlogManager.kt               # Blog data management
│           ├── OfferManager.kt              # Promotional offers
│           ├── OrderManager.kt              # Order management
│           └── WishlistManager.kt           # Wishlist functionality
│
├── res/                                    # Resources
│   ├── layout/                             # XML layouts
│   ├── values/                             # Strings, colors, dimensions
│   ├── drawable/                           # Images and drawables
│   ├── menu/                               # Menu resources
│   └── xml/                                # Other XML resources
│
└── AndroidManifest.xml                     # App manifest
```

## 🎯 Architecture Pattern

The project follows **Clean Architecture** with clear separation of concerns:

### **Layer 1: UI Layer** (`ui/`)
- **Activities**: Screen-level components
- **Fragments**: Reusable UI components
- **Adapters**: RecyclerView data binding

### **Layer 2: Data Layer** (`data/`)
- **Models**: Data classes (Product, Category, etc.)
- **Repository**: Data access abstraction (WooRepository, WpRepository)
- **WooCommerce & WordPress**: External API integrations

### **Layer 3: Utility Layer** (`util/`)
- **Managers**: Business logic and state management
  - AuthManager: Login/authentication
  - CartManager: Shopping cart operations
  - ProductManager: Product data handling
  - OrderManager: Order processing
  - BlogManager: Blog content
  - OfferManager: Promotions
  - WishlistManager: Favorites

## 📦 File Organization Summary

| Category | Location | Purpose |
|----------|----------|---------|
| **Screens (Activities)** | `ui/activities/` | Full-screen UI components |
| **UI Fragments** | `ui/fragments/` | Reusable screen sections |
| **List/Grid Adapters** | `ui/adapters/` | RecyclerView data adapters |
| **Data Models** | `data/model/` | Plain data classes |
| **Data Fetching** | `data/repository/` | API calls and data access |
| **External APIs** | `data/woo/`, `data/wp/` | API clients and models |
| **Business Logic** | `util/manager/` | Feature logic and state |
| **App Bootstrap** | Root package | NatrajApplication.kt |

## 🔄 Data Flow

```
UI (Activities/Fragments)
    ↓
Adapters (Display data)
    ↓
Managers (Business logic)
    ↓
Repository (Data access)
    ↓
API Clients (WooCommerce, WordPress)
    ↓
External APIs
```

## 🛠️ Key Components

### **WooCommerce Integration**
- Location: `data/woo/`
- Purpose: E-commerce data (products, categories, orders)
- Authentication: OAuth (Consumer Key/Secret)

### **WordPress Integration**
- Location: `data/wp/`
- Purpose: Blog articles and content
- Authentication: REST API access

### **Cart Management**
- Manager: `util/manager/CartManager.kt`
- Storage: SharedPreferences
- Operations: Add, remove, update items

### **Authentication**
- Manager: `util/manager/AuthManager.kt`
- Methods: Login, signup, logout
- Storage: Secure SharedPreferences

## ✅ Benefits of This Structure

1. **Scalability**: Easy to add new features in appropriate folders
2. **Maintainability**: Clear separation of concerns
3. **Testability**: Each layer can be tested independently
4. **Reusability**: Components can be reused across activities
5. **Navigation**: Easy to find files by feature
6. **Team Collaboration**: Clear ownership boundaries

## 🔍 Quick Navigation

- **Want to change UI?** → Go to `ui/`
- **Want to fix data issues?** → Go to `data/`
- **Want to add business logic?** → Go to `util/manager/`
- **Want to modify adapters?** → Go to `ui/adapters/`
- **Want to add new screen?** → Create in `ui/activities/`
- **Want to handle new API?** → Create in `data/`

## 📝 Removed Files

- ❌ `ProductManager.kt.bak` (backup file)
- ✅ All test files remain in `src/test/` and `src/androidTest/`

