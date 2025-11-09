#!/bin/bash

# Test Order Flow Script
# This script helps verify the complete order placement workflow

echo "=========================================="
echo "Natraj Super - Order Flow Test"
echo "=========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Step 1: Checking device connection...${NC}"
if ! adb devices | grep -q "device$"; then
    echo -e "${RED}❌ No device connected!${NC}"
    echo "Please connect an Android device or start an emulator"
    exit 1
fi
echo -e "${GREEN}✓ Device connected${NC}"
echo ""

echo -e "${YELLOW}Step 2: Checking if app is installed...${NC}"
if ! adb shell pm list packages | grep -q "com.example.natraj"; then
    echo -e "${RED}❌ App not installed!${NC}"
    echo "Please install the app first:"
    echo "  ./gradlew assembleDebug"
    echo "  adb install -r app/build/outputs/apk/debug/app-universal-debug.apk"
    exit 1
fi
echo -e "${GREEN}✓ App is installed${NC}"
echo ""

echo -e "${YELLOW}Step 3: Clearing old logs...${NC}"
adb logcat -c
echo -e "${GREEN}✓ Logs cleared${NC}"
echo ""

echo -e "${YELLOW}Step 4: Launching app...${NC}"
adb shell monkey -p com.example.natraj 1 > /dev/null 2>&1
sleep 2
echo -e "${GREEN}✓ App launched${NC}"
echo ""

echo "=========================================="
echo "MONITORING ORDER FLOW"
echo "=========================================="
echo ""
echo -e "${YELLOW}Watching for order-related logs...${NC}"
echo -e "${YELLOW}(Press Ctrl+C to stop monitoring)${NC}"
echo ""
echo "Expected flow:"
echo "  1. NatrajApp logs showing WooCommerce configured"
echo "  2. PaymentActivity logs when placing order"
echo "  3. Order creation success/failure"
echo "  4. OrderConfirmation logs"
echo ""
echo "=========================================="
echo ""

# Monitor logs with color highlighting
adb logcat | grep --line-buffered -E "(NatrajApp|PaymentActivity|OrderConfirmation|WooRepository|WooClient)" | \
while IFS= read -r line; do
    if echo "$line" | grep -q "✓"; then
        echo -e "${GREEN}$line${NC}"
    elif echo "$line" | grep -q "✗\|Error\|ERROR\|Failed\|failed"; then
        echo -e "${RED}$line${NC}"
    elif echo "$line" | grep -q "==="; then
        echo -e "${YELLOW}$line${NC}"
    else
        echo "$line"
    fi
done
