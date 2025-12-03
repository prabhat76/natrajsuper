#!/bin/bash

echo "🏗️ Building Natraj Super with Enhanced Categories..."

cd /Users/prabhatkumar/Desktop/akshay

# Clean and build
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
            echo "📋 Enhanced Categories Features:"
            echo "• 9 categories with custom icons"
            echo "• Special offer badges (orange dots)"
            echo "• Product counts displayed"
            echo "• Gradient icon backgrounds"
            echo "• Colored category icons"
            echo "• Selection indicators"
            echo ""
            echo "🔍 Monitor categories:"
            echo "adb logcat | grep 'SimpleCategoryAdapter\\|HomeFragment'"
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