# 🔍 Roboflow Detection Debugging Guide

## Current Status
The app is capturing images successfully, but **no detections are being returned** from the Roboflow models. This guide will help you diagnose and fix the issue.

---

## 📋 Quick Checklist

Before diving into logs, verify these basics:

### ✅ 1. API Keys Configuration
Check `local.properties`:
```properties
ROBOFLOW_HALL_API_KEY=Qmr1K2CkeGUoEFjfEJvn
ROBOFLOW_DOORS_API_KEY=rAPgd7z8fy90FMC7RvEQ
ROBOFLOW_WINDOWS_API_KEY=rAPgd7z8fy90FMC7RvEQ
ROBOFLOW_STAIRS_API_KEY=Qmr1K2CkeGUoEFjfEJvn
```

**Action**: Verify these keys are valid in your Roboflow dashboard.

### ✅ 2. API URLs Configuration
Check `local.properties`:
```properties
ROBOFLOW_HALL_URL=https://serverless.roboflow.com/triahldataset/workflows/small-object-detection-sahi
ROBOFLOW_DOORS_URL=https://serverless.roboflow.com/exit-finder/workflows/small-object-detection-sahi-2
ROBOFLOW_WINDOWS_URL=https://serverless.roboflow.com/exit-finder/workflows/small-object-detection-sahi
ROBOFLOW_STAIRS_URL=https://serverless.roboflow.com/triahldataset/workflows/small-object-detection-sahi-2
```

**Action**: Verify these URLs match your Roboflow workflow endpoints.

### ✅ 3. Internet Connection
- Device must have active internet connection
- Check if device can reach `serverless.roboflow.com`

### ✅ 4. Rebuild the App
After changing `local.properties`, you **MUST** rebuild:
```bash
./gradlew clean
./gradlew assembleDebug
```

Or in Android Studio: **Build → Clean Project → Rebuild Project**

---

## 🔬 Step-by-Step Debugging

### Step 1: Run the App and Capture an Image

1. Open the app
2. Navigate to the Camera/Scan screen
3. Click the **Scan** button to capture an image
4. Watch the Logcat output

### Step 2: Check Logcat for Detection Logs

Open **Logcat** in Android Studio and filter by tag: `RoboflowRepository`

You should see logs like this:

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
[windows] URL: https://serverless.roboflow.com/exit-finder/workflows/small-object-detection-sahi
[windows] API Key: rAPgd7z8fy...
[windows] Image size: 123456 chars
[windows] 📡 Sending request...
[windows] ✅ Response received
[windows] Predictions count: 0

[doors] 🚀 Starting API call
[doors] URL: https://serverless.roboflow.com/exit-finder/workflows/small-object-detection-sahi-2
[doors] API Key: rAPgd7z8fy...
[doors] Image size: 123456 chars
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

## 🚨 Common Issues and Solutions

### Issue 1: "No Roboflow API keys configured"

**Symptom**: Log shows `❌ No Roboflow API keys configured`

**Solution**:
1. Check `local.properties` has the API keys
2. Rebuild the app: `./gradlew clean assembleDebug`
3. Verify BuildConfig is loading keys correctly

### Issue 2: "No internet connection"

**Symptom**: Log shows `❌ No internet connection`

**Solution**:
1. Check device WiFi/mobile data
2. Test internet: Open browser and visit `https://roboflow.com`
3. Check Android permissions for internet access

### Issue 3: API Call Fails with Error

**Symptom**: Log shows `❌ API call failed: <error message>`

**Possible Errors**:

#### A. `401 Unauthorized`
- **Cause**: Invalid API key
- **Solution**: Verify API key in Roboflow dashboard, update `local.properties`, rebuild

#### B. `404 Not Found`
- **Cause**: Invalid workflow URL
- **Solution**: Check workflow URL in Roboflow dashboard, update `local.properties`, rebuild

#### C. `Timeout` or `Connection refused`
- **Cause**: Network issue or Roboflow server down
- **Solution**: Check internet, try again later, verify Roboflow status

#### D. `400 Bad Request`
- **Cause**: Invalid request format (image encoding issue)
- **Solution**: Check image is being converted to Base64 correctly

### Issue 4: API Returns 0 Predictions

**Symptom**: Log shows `Predictions count: 0` for all models

**Possible Causes**:

#### A. Image Quality
- Image is too dark/bright
- Image is blurry
- Objects are too small/far away

**Solution**: Try capturing different images with better lighting and closer objects

#### B. Model Confidence Threshold
- Roboflow models have a confidence threshold
- Predictions below threshold are filtered out

**Solution**: Check your Roboflow workflow settings and lower the confidence threshold

#### C. Wrong Object Types
- Models are trained for specific objects (doors, windows, stairs, halls)
- If image doesn't contain these objects, no predictions will be returned

**Solution**: Test with images that clearly contain doors, windows, stairs, or hallways

#### D. Model Not Trained
- Workflow might not be properly configured
- Model might not be deployed

**Solution**: Check Roboflow dashboard to ensure models are trained and deployed

### Issue 5: Predictions Returned But Not Displayed

**Symptom**: Log shows predictions, but UI shows "No exits detected yet"

**Possible Causes**:

#### A. Confidence Filtering
- App might be filtering low-confidence predictions
- Check `DetectionResult` class for filtering logic

#### B. UI State Not Updating
- ViewModel state might not be updating correctly
- Check `RoboflowScanViewModel.handleDetectionResult()`

**Solution**: Check ViewModel logs and UI state flow

---

## 🧪 Testing with cURL

To verify your API keys and URLs work independently of the app, test with cURL:

### Test DOORS Model:
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

### Test WINDOWS Model:
```bash
curl --location 'https://serverless.roboflow.com/exit-finder/workflows/small-object-detection-sahi' \
--header 'Content-Type: application/json' \
--data '{
  "api_key": "rAPgd7z8fy90FMC7RvEQ",
  "inputs": {
    "image": {
      "type": "url",
      "value": "https://source.unsplash.com/featured/?window"
    }
  }
}'
```

### Test HALL Model:
```bash
curl --location 'https://serverless.roboflow.com/triahldataset/workflows/small-object-detection-sahi' \
--header 'Content-Type: application/json' \
--data '{
  "api_key": "Qmr1K2CkeGUoEFjfEJvn",
  "inputs": {
    "image": {
      "type": "url",
      "value": "https://source.unsplash.com/featured/?hallway"
    }
  }
}'
```

### Test STAIRS Model:
```bash
curl --location 'https://serverless.roboflow.com/triahldataset/workflows/small-object-detection-sahi-2' \
--header 'Content-Type: application/json' \
--data '{
  "api_key": "Qmr1K2CkeGUoEFjfEJvn",
  "inputs": {
    "image": {
      "type": "url",
      "value": "https://source.unsplash.com/featured/?stairs"
    }
  }
}'
```

**Expected Response**:
```json
{
  "predictions": [
    {
      "x": 100,
      "y": 200,
      "width": 50,
      "height": 80,
      "confidence": 0.85,
      "class": "door",
      "class_name": "door"
    }
  ]
}
```

If cURL returns predictions but the app doesn't, the issue is in the app's image encoding or request format.

---

## 🔧 Advanced Debugging

### Enable Retrofit Logging

Add OkHttp logging interceptor to see raw HTTP requests/responses:

1. Open `RoboflowRepository.kt`
2. Add logging interceptor:

```kotlin
private val service: RoboflowService by lazy {
    val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    val retrofit = Retrofit.Builder()
        .baseUrl(RoboflowService.BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    retrofit.create(RoboflowService::class.java)
}
```

This will log the full HTTP request and response in Logcat.

### Check Base64 Image Encoding

Add this log to verify image encoding:

```kotlin
private fun bitmapToBase64(bitmap: Bitmap): String {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
    val bytes = outputStream.toByteArray()
    
    android.util.Log.d("RoboflowRepository", "Image bytes: ${bytes.size}")
    android.util.Log.d("RoboflowRepository", "First 50 bytes: ${bytes.take(50).joinToString()}")
    
    return Base64.encodeToString(bytes, Base64.NO_WRAP)
}
```

### Verify BuildConfig Values

Add this to `RoboflowScanViewModel.init`:

```kotlin
init {
    checkConfiguration()
    
    // Debug: Print all BuildConfig values
    android.util.Log.d("RoboflowScanViewModel", "=== BuildConfig Values ===")
    android.util.Log.d("RoboflowScanViewModel", "WINDOWS_URL: ${BuildConfig.ROBOFLOW_WINDOWS_URL}")
    android.util.Log.d("RoboflowScanViewModel", "WINDOWS_KEY: ${BuildConfig.ROBOFLOW_WINDOWS_API_KEY.take(10)}...")
    android.util.Log.d("RoboflowScanViewModel", "DOORS_URL: ${BuildConfig.ROBOFLOW_DOORS_URL}")
    android.util.Log.d("RoboflowScanViewModel", "DOORS_KEY: ${BuildConfig.ROBOFLOW_DOORS_API_KEY.take(10)}...")
    android.util.Log.d("RoboflowScanViewModel", "HALL_URL: ${BuildConfig.ROBOFLOW_HALL_URL}")
    android.util.Log.d("RoboflowScanViewModel", "HALL_KEY: ${BuildConfig.ROBOFLOW_HALL_API_KEY.take(10)}...")
    android.util.Log.d("RoboflowScanViewModel", "STAIRS_URL: ${BuildConfig.ROBOFLOW_STAIRS_URL}")
    android.util.Log.d("RoboflowScanViewModel", "STAIRS_KEY: ${BuildConfig.ROBOFLOW_STAIRS_API_KEY.take(10)}...")
}
```

---

## 📊 Expected Behavior

### Successful Detection Flow:

1. **User clicks Scan button**
2. **Camera captures image** → Bitmap created
3. **ViewModel calls `scanImage(bitmap)`**
4. **Repository converts bitmap to Base64**
5. **4 parallel API calls** to Roboflow models
6. **Each model returns predictions** (or empty array)
7. **Predictions merged** into single `DetectionResult`
8. **UI updates**:
   - If predictions found → Show bounding boxes + "Exit found" speech
   - If no predictions → Show "No exits detected yet" with retry button

### Current Issue:

- Steps 1-6 work correctly
- Step 7: All models return **0 predictions**
- Step 8: UI shows "No exits detected yet"

**This is NOT an error** — it's expected behavior when models don't detect anything.

---

## 🎯 Next Steps

### 1. Run the App and Check Logs

Run the app, capture an image, and check Logcat for the detailed logs we added.

### 2. Test with cURL

Use the cURL commands above to verify your API keys and URLs work.

### 3. Test with Different Images

Try capturing images with:
- Clear, well-lit doors
- Windows with visible frames
- Hallways with clear perspective
- Stairs with visible steps

### 4. Check Roboflow Dashboard

1. Log into Roboflow
2. Check your workflows are deployed
3. Verify confidence thresholds
4. Test with sample images in the dashboard

### 5. Lower Confidence Threshold

If predictions exist but are filtered out, lower the confidence threshold in your Roboflow workflow settings.

---

## 📞 Support

If you're still stuck after following this guide:

1. **Share Logcat output** — Copy the full log from "STARTING DETECTION SCAN" to "SCAN COMPLETE"
2. **Share cURL test results** — Show what the API returns when tested with cURL
3. **Share test image** — Describe what objects are in the image you're testing with
4. **Share Roboflow dashboard** — Screenshot of your workflow configuration

---

## ✅ Success Indicators

You'll know it's working when you see:

```
[doors] Predictions count: 2
[doors] Prediction 0: door (confidence: 0.85)
[doors] Prediction 1: door (confidence: 0.72)

🏁 SCAN COMPLETE
Total detections: 2
Has exits: true
```

And the UI shows:
- Green bounding boxes around detected objects
- "Exit found" speech plays
- Result overlay shows detected objects with confidence scores

---

**Good luck debugging! 🚀**
