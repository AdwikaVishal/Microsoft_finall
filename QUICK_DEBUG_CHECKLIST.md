# ⚡ Quick Debug Checklist

## Before Running the App

### 1. Verify `local.properties` ✅
```properties
# Check these lines exist:
ROBOFLOW_HALL_URL=https://serverless.roboflow.com/triahldataset/workflows/small-object-detection-sahi
ROBOFLOW_HALL_API_KEY=Qmr1K2CkeGUoEFjfEJvn

ROBOFLOW_DOORS_URL=https://serverless.roboflow.com/exit-finder/workflows/small-object-detection-sahi-2
ROBOFLOW_DOORS_API_KEY=rAPgd7z8fy90FMC7RvEQ

ROBOFLOW_WINDOWS_URL=https://serverless.roboflow.com/exit-finder/workflows/small-object-detection-sahi
ROBOFLOW_WINDOWS_API_KEY=rAPgd7z8fy90FMC7RvEQ

ROBOFLOW_STAIRS_URL=https://serverless.roboflow.com/triahldataset/workflows/small-object-detection-sahi-2
ROBOFLOW_STAIRS_API_KEY=Qmr1K2CkeGUoEFjfEJvn
```

### 2. Rebuild the App 🔨
```bash
./gradlew clean
./gradlew assembleDebug
```

Or: **Build → Clean Project → Rebuild Project**

### 3. Check Device Internet 🌐
- WiFi or mobile data enabled
- Can access `https://roboflow.com` in browser

---

## Running the App

### 1. Open Logcat 📊
Filter by: `RoboflowRepository` or `RoboflowScanViewModel`

### 2. Navigate to Camera Screen 📷
Open the scan/camera feature in the app

### 3. Check Configuration Logs 🔧
You should see:
```
========================================
🔧 ROBOFLOW CONFIGURATION CHECK
========================================
WINDOWS_URL: https://serverless.roboflow.com/...
WINDOWS_KEY: rAPgd7z8fy...
DOORS_URL: https://serverless.roboflow.com/...
DOORS_KEY: rAPgd7z8fy...
...
Configuration status: ✅ READY
========================================
```

**If you see `❌ NOT CONFIGURED`**: Rebuild the app!

### 4. Click Scan Button 🎯
Capture an image

### 5. Check Detection Logs 🔍
You should see:
```
========================================
🚀 STARTING DETECTION SCAN
========================================
Bitmap size: 1920x1080
✅ Internet connection available
✅ API keys configured: 4/4 models configured
🔄 Converting bitmap to Base64...
✅ Base64 conversion complete: 123456 characters
🔄 Starting parallel API calls to 4 models...

[windows] 🚀 Starting API call
[windows] URL: https://...
[windows] API Key: rAPgd7z8fy...
[windows] Image size: 123456 chars
[windows] 📡 Sending request...
[windows] ✅ Response received
[windows] Predictions count: 0

[doors] 🚀 Starting API call
[doors] 📡 Sending request...
[doors] ✅ Response received
[doors] Predictions count: 2
[doors] Prediction 0: door (confidence: 0.85)
[doors] Prediction 1: door (confidence: 0.72)

✅ All API calls completed
Results summary:
  Model 1 (windows): 0 predictions, error: none
  Model 2 (doors): 2 predictions, error: none
  Model 3 (hallways): 0 predictions, error: none
  Model 4 (stairs): 0 predictions, error: none

========================================
🏁 SCAN COMPLETE
Total detections: 2
Has exits: true
Message: Exit found — 2 object(s) detected
========================================
```

---

## What the Logs Tell You

### ✅ Success Indicators
- `✅ Internet connection available`
- `✅ API keys configured: 4/4 models configured`
- `✅ Response received` (for each model)
- `Predictions count: X` where X > 0
- `Has exits: true`

### ⚠️ Warning Indicators
- `Predictions count: 0` (all models) → Try different image or lower confidence threshold
- `error: <message>` → Check specific error

### ❌ Error Indicators
- `❌ No internet connection` → Check device internet
- `❌ No Roboflow API keys configured` → Rebuild app
- `❌ API call failed: 401 Unauthorized` → Invalid API key
- `❌ API call failed: 404 Not Found` → Invalid URL
- `❌ API call failed: Timeout` → Network issue

---

## Quick Fixes

### Problem: "No Roboflow API keys configured"
**Fix**: 
1. Check `local.properties` has the keys
2. Run: `./gradlew clean assembleDebug`
3. Reinstall app

### Problem: "No internet connection"
**Fix**:
1. Enable WiFi/mobile data
2. Test: Open browser → visit `https://roboflow.com`
3. Check app has internet permission

### Problem: "401 Unauthorized"
**Fix**:
1. Verify API keys in Roboflow dashboard
2. Update `local.properties`
3. Rebuild app

### Problem: "404 Not Found"
**Fix**:
1. Verify workflow URLs in Roboflow dashboard
2. Update `local.properties`
3. Rebuild app

### Problem: "Predictions count: 0" (all models)
**Fix**:
1. Try different images with clear doors/windows/stairs/halls
2. Ensure good lighting
3. Lower confidence threshold in Roboflow dashboard
4. Test with cURL to verify models work

---

## Test with cURL

Quick test to verify API works:

```bash
curl --location 'https://serverless.roboflow.com/exit-finder/workflows/small-object-detection-sahi-2' \
--header 'Content-Type: application/json' \
--data '{
  "api_key": "rAPgd7z8fy90FMC7RvEQ",
  "inputs": {
    "image": {
      "type": "url",
      "value": "https://source.unsplash.com/featured/?door"
    }
  }
}'
```

**Expected**: JSON response with predictions array

---

## Summary

1. ✅ Verify `local.properties`
2. 🔨 Rebuild app
3. 📊 Open Logcat
4. 📷 Open camera screen → Check configuration logs
5. 🎯 Click scan → Check detection logs
6. 🔍 Analyze results

**If predictions count = 0**: Try different images or lower confidence threshold in Roboflow

**If API errors**: Check API keys and URLs in Roboflow dashboard

**For detailed help**: See `ROBOFLOW_DEBUGGING_GUIDE.md`

---

**You're all set! Run the app and check the logs.** 🚀
