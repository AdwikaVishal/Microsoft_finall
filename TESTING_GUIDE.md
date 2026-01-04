# Testing Guide - SenseSafe Android App

## ✅ All Tests Passing

**Status:** Unit tests passed successfully  
**Last Run:** January 3, 2026  
**Result:** BUILD SUCCESSFUL  

---

## 🧪 Available Test Commands

### Unit Tests (Fast, No Device Required)

```bash
# Run all unit tests (debug + release)
./gradlew test

# Run debug unit tests only (recommended for development)
./gradlew testDebugUnitTest

# Run release unit tests only
./gradlew testReleaseUnitTest

# Run tests with detailed output
./gradlew test --info

# Run tests and generate HTML report
./gradlew test
# Report location: app/build/reports/tests/testDebugUnitTest/index.html
```

### Instrumented Tests (Requires Device/Emulator)

```bash
# Run instrumented tests on connected device
./gradlew connectedDebugAndroidTest

# Run all connected checks
./gradlew connectedCheck

# Run instrumented tests with coverage
./gradlew connectedDebugAndroidTest jacocoTestReport
```

### Lint Checks

```bash
# Run lint checks
./gradlew lint

# Run lint and auto-fix issues
./gradlew lintFix

# Run lint on debug variant only
./gradlew lintDebug

# Generate lint report
./gradlew lint
# Report location: app/build/reports/lint-results.html
```

### All Verification Tasks

```bash
# Run all checks (tests + lint)
./gradlew check

# Run all checks with detailed output
./gradlew check --info
```

---

## 📊 Test Reports

### Unit Test Reports:
```
app/build/reports/tests/testDebugUnitTest/index.html
app/build/reports/tests/testReleaseUnitTest/index.html
```

### Instrumented Test Reports:
```
app/build/reports/androidTests/connected/index.html
```

### Lint Reports:
```
app/build/reports/lint-results.html
app/build/reports/lint-results.xml
```

### Coverage Reports (if configured):
```
app/build/reports/coverage/androidTest/debug/index.html
```

---

## 🎯 Testing Track Location Feature

### Manual Testing Checklist:

#### 1. Permission Flow:
```bash
# Install app
adb install app/build/outputs/apk/debug/app-debug.apk

# Launch app
adb shell am start -n com.example.myapplication/.OnboardingActivity

# Monitor logs
adb logcat | grep "TrackLocation"
```

**Test Cases:**
- [ ] First launch → Permission request appears
- [ ] Deny permission → Permission rationale screen
- [ ] Grant permission → Map loads
- [ ] Permanently deny → "Open Settings" button
- [ ] Settings button → App settings page

#### 2. Map Display:
**Test Cases:**
- [ ] Map loads outdoor terrain style
- [ ] Topographic contours visible
- [ ] Trails and paths visible
- [ ] Natural features labeled
- [ ] Load time < 3 seconds

#### 3. Location Marker:
**Test Cases:**
- [ ] Marker appears at correct location
- [ ] Marker shows user name (or "You")
- [ ] Text: "Your Name\nYou are here 📍"
- [ ] Text has white halo
- [ ] Camera animates smoothly

#### 4. Interactions:
**Test Cases:**
- [ ] Pinch-to-zoom works
- [ ] Rotation works (two-finger twist)
- [ ] Double-tap zoom works
- [ ] Map scrolling works
- [ ] No lag or stuttering

#### 5. Error Handling:
**Test Cases:**
- [ ] Disable GPS → Error overlay
- [ ] Tap "GPS Settings" → Location settings
- [ ] Tap "Retry" → Refetch location
- [ ] No internet → Map load error
- [ ] Timeout (10s) → Error overlay

#### 6. Navigation:
**Test Cases:**
- [ ] MainScreen → Track Location works
- [ ] Close button → Return to MainScreen
- [ ] Back button → Return to MainScreen
- [ ] Screen rotation → State preserved

---

## 🔍 Debugging Tests

### View Test Logs:
```bash
# View all logs
adb logcat

# Filter by tag
adb logcat -s TrackLocationViewModel
adb logcat -s TrackLocationScreen
adb logcat -s LocationRepository

# Filter by level
adb logcat *:E  # Errors only
adb logcat *:W  # Warnings and above
adb logcat *:D  # Debug and above

# Save logs to file
adb logcat > logs.txt
```

### Clear Logs:
```bash
adb logcat -c
```

### Monitor Specific Components:
```bash
# Track Location logs
adb logcat | grep -i "tracklocation"

# Mapbox logs
adb logcat | grep -i "mapbox"

# Location logs
adb logcat | grep -i "location"

# Permission logs
adb logcat | grep -i "permission"
```

---

## 🚨 Common Test Issues

### Issue: `testClasses` task not found
**Error:**
```
Cannot locate tasks that match ':app:testClasses'
```

**Solution:**
Use correct Android test tasks:
```bash
./gradlew test                    # Unit tests
./gradlew testDebugUnitTest       # Debug unit tests
./gradlew connectedDebugAndroidTest  # Instrumented tests
```

### Issue: Tests fail with "No connected devices"
**Solution:**
```bash
# Check connected devices
adb devices

# Start emulator
emulator -avd Pixel_5_API_34

# Or connect physical device via USB
```

### Issue: Permission denied errors
**Solution:**
```bash
# Grant all permissions
adb shell pm grant com.example.myapplication android.permission.ACCESS_FINE_LOCATION
adb shell pm grant com.example.myapplication android.permission.ACCESS_COARSE_LOCATION
adb shell pm grant com.example.myapplication android.permission.CAMERA
```

### Issue: Map doesn't load
**Solution:**
1. Check Mapbox token in `local.properties`
2. Verify internet connection
3. Check Logcat for errors:
   ```bash
   adb logcat | grep -i "mapbox\|maptiler"
   ```

---

## 📝 Writing Tests for Track Location

### Unit Test Example:
```kotlin
// app/src/test/java/com/example/myapplication/viewmodel/TrackLocationViewModelTest.kt
class TrackLocationViewModelTest {
    
    @Test
    fun `fetchCurrentLocation updates state to Loading`() {
        // Given
        val viewModel = TrackLocationViewModel(...)
        
        // When
        viewModel.fetchCurrentLocation()
        
        // Then
        assertTrue(viewModel.locationState.value is LocationState.Loading)
    }
    
    @Test
    fun `successful location fetch updates state to Success`() {
        // Given
        val mockLocation = Location("mock").apply {
            latitude = 37.7749
            longitude = -122.4194
        }
        
        // When
        // ... trigger location fetch
        
        // Then
        val state = viewModel.locationState.value
        assertTrue(state is LocationState.Success)
        assertEquals(37.7749, (state as LocationState.Success).latitude, 0.0001)
    }
}
```

### Instrumented Test Example:
```kotlin
// app/src/androidTest/java/com/example/myapplication/ui/TrackLocationScreenTest.kt
@RunWith(AndroidJUnit4::class)
class TrackLocationScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun trackLocationScreen_displaysMap() {
        // Given
        composeTestRule.setContent {
            TrackLocationScreen(onNavigateBack = {})
        }
        
        // Then
        composeTestRule.onNodeWithText("Track Location").assertExists()
    }
}
```

---

## 🎯 Test Coverage

### Current Coverage:
- **Unit Tests:** Basic ViewModel tests
- **Instrumented Tests:** Not yet implemented
- **Manual Tests:** Required for Track Location

### Recommended Coverage:
1. **Unit Tests:**
   - LocationRepository location fetching
   - TrackLocationViewModel state management
   - UserPreferencesRepository data storage

2. **Instrumented Tests:**
   - TrackLocationScreen UI rendering
   - Permission flow
   - Map interactions
   - Navigation flow

3. **Integration Tests:**
   - End-to-end location tracking
   - Error handling scenarios
   - Network failure scenarios

---

## 📚 Testing Resources

### Android Testing:
- **Official Guide:** https://developer.android.com/training/testing
- **Compose Testing:** https://developer.android.com/jetpack/compose/testing
- **Espresso:** https://developer.android.com/training/testing/espresso

### Mapbox Testing:
- **Mapbox Testing Guide:** https://docs.mapbox.com/android/maps/guides/testing/
- **Mock Location:** https://developer.android.com/training/location/location-testing

### Tools:
- **JUnit 4:** Unit testing framework
- **Mockito:** Mocking framework
- **Espresso:** UI testing framework
- **Compose Test:** Jetpack Compose testing

---

## ✅ Summary

**Current Status:**
- ✅ Unit tests passing
- ✅ Build successful
- ✅ No compilation errors
- ⚠️ Manual testing required for Track Location

**Next Steps:**
1. Install APK on device/emulator
2. Run manual test checklist
3. Verify all features work
4. (Optional) Write instrumented tests

**Test Commands:**
```bash
# Quick test
./gradlew testDebugUnitTest

# Full verification
./gradlew check

# Install and test
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

*Last Updated: January 3, 2026*  
*Test Status: PASSING*  
*Coverage: Basic unit tests*
