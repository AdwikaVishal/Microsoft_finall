# OpenCV Dependency Fix Plan

## Problem Analysis

The Android build is failing with the error:
```
Could not find com.quickbirdstudios:opencv:4.9.0.0.
```

**Root Cause**: The OpenCV library `com.quickbirdstudios:opencv:4.9.0.0` is not available in the standard Maven repositories (Google and Maven Central).

## Solution Options

### Option 1: Add JitPack Repository (Recommended)
Add JitPack repository where the library might be hosted.

**Steps:**
1. Add JitPack repository to `settings.gradle.kts`
2. Verify the dependency works

### Option 2: Use Official OpenCV Android SDK
Use OpenCV from the official releases which are available through standard repositories.

**Steps:**
1. Replace the dependency with official OpenCV Android package
2. Update any necessary code references

### Option 3: Manual AAR Import
Download the OpenCV AAR file and import it locally.

**Steps:**
1. Download the OpenCV AAR file
2. Place it in the `app/libs` directory
3. Update build configuration to use local AAR

## Implementation Plan

I'll proceed with **Option 1** first (adding JitPack repository) as it's the least invasive and most likely to work.

### Current State
- **File**: `app/build.gradle.kts`
- **Dependency**: `implementation("com.quickbirdstudios:opencv:4.9.0.0")`
- **Repository Config**: `settings.gradle.kts` has only `google()` and `mavenCentral()`

### Fix Implementation
1. Add JitPack repository to `settings.gradle.kts`
2. Test the build
3. If JitPack doesn't work, try Option 2 or 3

## Files to Modify
1. `/Users/apple/imagine_backup/Microsoft_Imagine_Cup-main/settings.gradle.kts` - Add JitPack repository

## Success Criteria
- Build completes successfully without OpenCV dependency errors
- All existing functionality remains intact
- No regression in other dependencies
