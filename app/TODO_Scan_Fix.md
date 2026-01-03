# Scan Feature Fix - TODO List

## Goal
Fix ML Kit crash ("Image is already closed") and display recognized text instead of always showing "Scanning..."

## Issues
1. Log shows "Image is already closed" from ML Kit
2. ML Kit always prints "Scanning..." even when text exists

## Fixes Required

### 1. ObjectDetectorHelper.kt - Fix image closing
- [x] Move `imageProxy.close()` into ML Kit's `addOnCompleteListener`
- [x] Ensure image is closed only AFTER ML Kit finishes processing
- [x] Keep all SCAN_DEBUG logs

### 2. CameraScreen.kt - Fix text display
- [x] Update `processImageProxy` to properly handle image lifecycle
- [x] Display actual detected OCR text from ML Kit
- [x] Show "No text detected" when text is empty
- [x] Keep all SCAN_DEBUG logs

## Implementation Steps

1. [x] Edit ObjectDetectorHelper.kt - Fix image closing in addOnCompleteListener
2. [x] Edit CameraScreen.kt - Update processImageProxy and text display logic
3. [x] Verify all changes are correct

## Notes
- Only modify scan analyzer and related ViewModel
- Do not touch unrelated screens
- Add clear comments explaining each change

## Changes Made

### ObjectDetectorHelper.kt
- Added `addOnCompleteListener` to ML Kit's process call
- Removed `onComplete()` calls from success/failure listeners
- Image is now closed ONLY after `addOnCompleteListener` fires
- This prevents "Image is already closed" crash

### CameraScreen.kt  
- Added comment explaining the text display fix
- Text is now always shown from ML Kit result (not limited to EXIT/EMERGENCY)
- Exit detection logic still checks for EXIT keyword in the result

