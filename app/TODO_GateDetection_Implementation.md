# Gate/Exit Detection Implementation Plan

## Overview
Add OpenCV-based gate/exit detection to the CameraX live preview. This feature will detect rectangular contours (doors, gates, exits) using edge detection and contour analysis.

## Implementation Steps

### Step 1: Add OpenCV dependency to build.gradle.kts
- Add OpenCV Android SDK dependency

### Step 2: Create GateDetectionProcessor class
**Location:** `app/src/main/java/com/example/myapplication/ml/GateDetectionProcessor.kt`

**Features:**
- OpenCV image processing (grayscale, blur, Canny)
- Contour detection with filtering:
  - Minimum area threshold
  - Aspect ratio between 1.5 and 3.5
  - Shape approximation (4 vertices for rectangles)
- Frame persistence (3 consecutive frames) to reduce false positives
- Optional ML fallback (TFLite classifier)
- Background thread execution with Kotlin Coroutines
- StateFlow for detection results

### Step 3: Update CameraScreen.kt
- Integrate GateDetectionProcessor
- Display "Exit found" / "Scanning..." text
- Keep existing text detection feature untouched

### Step 4: Update ViewModel (optional)
- Add state for gate detection results
- Connect processor to UI state

## Key Design Decisions
1. **Separate processor class** - Follows single responsibility principle
2. **Non-blocking** - All processing on background dispatcher
3. **Frame persistence** - Exit must be detected in 3+ consecutive frames
4. **Backward compatible** - Existing ML Kit text detection remains intact
5. **Optional ML** - TFLite fallback only if model is available

## Dependencies
- OpenCV Android SDK 4.9.0
- Kotlin Coroutines
- CameraX

## Files to Create/Modify
1. ✅ `app/TODO_GateDetection_Implementation.md` (this file)
2. `app/build.gradle.kts` - Add OpenCV dependency
3. `app/src/main/java/com/example/myapplication/ml/GateDetectionProcessor.kt` (NEW)
4. `app/src/main/java/com/example/myapplication/ui/screens/CameraScreen.kt` - Integrate

