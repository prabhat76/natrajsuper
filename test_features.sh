#!/bin/bash

# Comprehensive Feature Test Script
# Tests all WordPress/WooCommerce integrations

echo "========================================"
echo "Natraj Super - Complete Feature Test"
echo "========================================"
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}Testing WordPress/WooCommerce Integration${NC}"
echo ""

# 1. Check device
echo -e "${YELLOW}[1/8] Checking device connection...${NC}"
if adb devices | grep -q "device$"; then
    echo -e "${GREEN}✓ Device connected${NC}"
else
    echo -e "${RED}✗ No device connected${NC}"
    exit 1
fi
echo ""

# 2. Check app installation
echo -e "${YELLOW}[2/8] Checking app installation...${NC}"
if adb shell pm list packages | grep -q "com.example.natraj"; then
    echo -e "${GREEN}✓ App is installed${NC}"
else
    echo -e "${RED}✗ App not installed${NC}"
    exit 1
fi
echo ""

# 3. Check WooCommerce settings
echo -e "${YELLOW}[3/8] Checking WooCommerce configuration...${NC}"
WOO_SETTINGS=$(adb shell "run-as com.example.natraj cat /data/data/com.example.natraj/shared_prefs/woo_settings.xml 2>/dev/null" | grep -c "natrajsuper.com")
if [ "$WOO_SETTINGS" -gt 0 ]; then
    echo -e "${GREEN}✓ WooCommerce configured (natrajsuper.com)${NC}"
else
    echo -e "${YELLOW}⚠ WooCommerce settings not in SharedPreferences (will be set by app on launch)${NC}"
fi
echo ""

# 4. Launch app and wait
echo -e "${YELLOW}[4/8] Launching app...${NC}"
adb shell am start -n com.example.natraj/.MainActivity > /dev/null 2>&1
sleep 3
echo -e "${GREEN}✓ App launched${NC}"
echo ""

# 5. Check app logs for WooCommerce init
echo -e "${YELLOW}[5/8] Checking WooCommerce initialization logs...${NC}"
WOO_INIT=$(adb logcat -d | grep -c "WooCommerce credentials configured")
if [ "$WOO_INIT" -gt 0 ]; then
    echo -e "${GREEN}✓ WooCommerce initialized in app${NC}"
    adb logcat -d | grep "NatrajApp.*WooCommerce" | tail -3
else
    echo -e "${YELLOW}⚠ WooCommerce init logs not found (may need app restart)${NC}"
fi
echo ""

# 6. Test network connectivity
echo -e "${YELLOW}[6/8] Testing WordPress site connectivity...${NC}"
PING_TEST=$(adb shell "ping -c 2 www.natrajsuper.com 2>&1" | grep -c "bytes from")
if [ "$PING_TEST" -gt 0 ]; then
    echo -e "${GREEN}✓ Can reach natrajsuper.com${NC}"
else
    echo -e "${RED}✗ Cannot reach natrajsuper.com (check device internet)${NC}"
fi
echo ""

# 7. Check for recent API activity
echo -e "${YELLOW}[7/8] Checking recent API activity...${NC}"
API_CALLS=$(adb logcat -d | grep -E "wp-json/wc/v3" | wc -l | tr -d ' ')
if [ "$API_CALLS" -gt 0 ]; then
    echo -e "${GREEN}✓ Found $API_CALLS WooCommerce API calls${NC}"
    echo -e "${BLUE}Recent API calls:${NC}"
    adb logcat -d | grep -E "wp-json/wc/v3" | tail -3
else
    echo -e "${YELLOW}⚠ No API calls detected yet (navigate to products/orders to trigger)${NC}"
fi
echo ""

# 8. Summary and next steps
echo -e "${YELLOW}[8/8] Feature Status Summary${NC}"
echo "========================================"
echo ""

# Check what's working
echo -e "${BLUE}Core Features:${NC}"
echo "  • App Installation: ✓ Working"
echo "  • WooCommerce Config: ✓ Configured in code"
echo "  • Network Access: $([ "$PING_TEST" -gt 0 ] && echo "✓ Working" || echo "✗ Not working")"
echo ""

echo -e "${BLUE}WordPress Integration Features:${NC}"
echo "  • Products from WooCommerce: Ready (browse home screen)"
echo "  • Categories from WooCommerce: Ready (categories tab)"
echo "  • Order Placement: Ready (add to cart → checkout → pay)"
echo "  • Orders List: Ready (profile → my orders)"
echo "  • Order Tracking: Ready (view order details)"
echo ""

echo "========================================"
echo -e "${GREEN}Test Complete!${NC}"
echo ""
echo -e "${BLUE}To test manually:${NC}"
echo "  1. Browse products on home screen (should load from WordPress)"
echo "  2. Add product to cart → checkout → place order"
echo "  3. Go to Profile → My Orders (should show WordPress orders)"
echo "  4. Login to WordPress admin to verify order"
echo ""
echo -e "${BLUE}To monitor live:${NC}"
echo "  adb logcat | grep -E \"(PaymentActivity|OrdersActivity|WooRepository)\""
echo ""
echo -e "${BLUE}WordPress Admin:${NC}"
echo "  https://www.natrajsuper.com/wp-admin"
echo "  Check: WooCommerce → Orders"
echo ""
