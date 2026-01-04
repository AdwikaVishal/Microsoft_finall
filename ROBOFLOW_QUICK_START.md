# 🚀 Roboflow Detection - Quick Start Guide

## ⚡ 3-Step Setup

### Step 1: Rebuild the App
```bash
./gradlew clean build
```

### Step 2: Run the App
```bash
./gradlew installDebug
```

### Step 3: Test Detection
1. Open camera/scan screen
2. Point at door/window/hallway/stairs
3. Tap scan button
4. See bounding boxes!

---

## 📱 Using in Your Code

### Basic Usage

```kotlin
// Get ViewModel
val viewModel = RoboflowScanViewModel(application)

// Scan an image
viewModel.scanImage(bitmap)

// Observe results
viewModel.uiState.collect { state ->
    when (state) {
        is ScanUiState.Loading -> showLoading()
        is ScanUiState.Success -> showResults(state.result)
        is ScanUiState.Error -> showError(state.message)
        is ScanUiState.Idle -> showCamera()
    }
}
```

### Drawing Bounding Boxes

```kotlin
val detectionResult = viewModel.detectionResult.collectAsState()

Canvas(modifier = Modifier.fillMaxSize()) {
    detectionResult.value?.allDetections?.forEach { pred ->
        val topLeftX = pred.x - (pred.width / 2)
        val topLeftY = pred.y - (pred.height / 2)
        
        drawRect(
            color = getColorForType(pred.class_name),
            topLeft = Offset(topLeftX, topLeftY),
            size = Size(pred.width, pred.height),
            style = Stroke(width = 4.dp.toPx())
        )
        
        drawText(
            text = "${pred.class_name} ${(pred.confidence * 100).toInt()}%",
            x = topLeftX,
            y = topLeftY - 10
        )
    }
}

fun getColorForType(className: String?): Color {
    return when (className?.lowercase()) {
        "door" -> Color.Green
        "window" -> Color.Blue
        "hallway", "hall" -> Color.Yellow
        "stairs", "stair" -> Color.Red
        else -> Color.White
    }
}
```

---

## 🎯 Detection Types

| Type | Color | API Key | URL |
|------|-------|---------|-----|
| **Doors** | 🟢 Green | `ROBOFLOW_DOORS_API_KEY` | `ROBOFLOW_DOORS_URL` |
| **Windows** | 🔵 Blue | `ROBOFLOW_WINDOWS_API_KEY` | `ROBOFLOW_WINDOWS_URL` |
| **Hallways** | 🟡 Yellow | `ROBOFLOW_HALL_API_KEY` | `ROBOFLOW_HALL_URL` |
| **Stairs** | 🔴 Red | `ROBOFLOW_STAIRS_API_KEY` | `ROBOFLOW_STAIRS_URL` |

---

## 🔍 Check Configuration

```kotlin
// Check if configured
val isConfigured = roboflowRepository.areApiKeysConfigured() && 
                   roboflowRepository.areUrlsConfigured()

// Get status
val status = roboflowRepository.getApiKeyStatus()
// Returns: "4/4 models configured"
```

---

## 🐛 Common Issues

### "No exits detected"
- ✅ Check lighting
- ✅ Get closer to object
- ✅ Try different angle

### "Scan failed"
- ✅ Check internet connection
- ✅ Verify API keys in `local.properties`
- ✅ Rebuild app: `./gradlew clean build`

### App crashes
- ✅ Check Logcat for errors
- ✅ Verify BuildConfig fields generated
- ✅ Clean and rebuild project

---

## 📊 Performance Tips

1. **Resize images** before scanning (faster API response)
2. **Cancel scans** when leaving screen
3. **Cache results** for same image
4. **Debounce** rapid scan requests

```kotlin
// Cancel scan when leaving screen
override fun onPause() {
    super.onPause()
    viewModel.cancelScan()
}
```

---

## 🎨 UI States

```kotlin
sealed class ScanUiState {
    object Idle          // Camera ready
    object Loading       // "Scanning..."
    data class Success   // Show results
    data class Error     // Show error message
}
```

---

## 🔐 Security

✅ API keys in `local.properties` (not in code)  
✅ BuildConfig compiles keys into app  
✅ ProGuard obfuscates in release builds  
✅ Keys never logged or exposed  

---

## 📞 Need Help?

1. Check `ROBOFLOW_INTEGRATION_COMPLETE.md` for full docs
2. Review Logcat for error messages
3. Test individual models first
4. Verify configuration in `local.properties`

---

**Happy detecting!** 🎉
