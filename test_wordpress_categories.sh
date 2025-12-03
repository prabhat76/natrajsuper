#!/bin/bash

echo "🌐 Testing WordPress Categories Integration..."

cd /Users/prabhatkumar/Desktop/akshay

# Build the app
echo "🏗️ Building app..."
./gradlew clean assembleDebug

if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
    
    # Install if device connected
    if adb devices | grep -q "device$"; then
        echo "📱 Installing on device..."
        adb install -r app/build/outputs/apk/debug/app-universal-debug.apk
        
        if [ $? -eq 0 ]; then
            echo "✅ Installation successful!"
            echo "🚀 Launching app..."
            adb shell am start -n com.example.natraj/.ui.activities.MainActivity
            
            echo ""
            echo "📋 WordPress Categories Integration:"
            echo "• Fetching categories from https://www.natrajsuper.com"
            echo "• Loading category images from WordPress"
            echo "• Showing product counts from WooCommerce"
            echo "• Special offer badges for categories with >10 products"
            echo "• Fallback to local icons if API fails"
            echo ""
            echo "🔍 Monitor category loading:"
            echo "adb logcat | grep 'WooRepository\\|HomeFragment\\|SimpleCategoryAdapter'"
            echo ""
            echo "🌐 Test API directly:"
            echo "curl -u ck_60e3de7255dafa3b78eeb2d96fec395cb0ceb19c:cs_2f8926db30ebb4366d135c1150ccbdd9cdb2b211 \\"
            echo "     'https://www.natrajsuper.com/wp-json/wc/v3/products/categories'"
        else
            echo "❌ Installation failed"
        fi
    else
        echo "📱 No device connected. APK built successfully."
        echo "Connect device and run: adb install -r app/build/outputs/apk/debug/app-universal-debug.apk"
    fi
else
    echo "❌ Build failed"
    exit 1
fi