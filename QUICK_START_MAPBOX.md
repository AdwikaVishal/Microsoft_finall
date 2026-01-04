# Track Location - Quick Start Guide

## 🚀 Get Started in 5 Minutes

### Step 1: Get Mapbox Token
```
1. Visit: https://account.mapbox.com/auth/signup/
2. Sign up (free, no credit card)
3. Copy default token (starts with pk.)
```

### Step 2: Configure
Open `local.properties` and add:
```properties
MAPBOX_ACCESS_TOKEN=pk.your_token_here
```

### Step 3: Build
```bash
./gradlew clean assembleDebug
```

### Step 4: Test
```
1. Launch app
2. Tap "Track Location"
3. Grant permission
4. See outdoor terrain map
5. Your marker appears
```

---

## ✅ What You Get

- 🗺️ **Outdoor Terrain Map** - Topographic contours, trails, elevation
- 📍 **Your Location Marker** - Shows your name and position
- 🎯 **Smooth Animations** - Camera flies to your location
- ⚡ **Fast Loading** - Vector tiles load in <3 seconds
- 🔋 **Battery Efficient** - Optimized location fetching
- 🎨 **Beautiful UI** - Material Design 3 with loading/error states

---

## 📚 Full Documentation

- **MAPBOX_MAPTILER_SETUP.md** - Complete setup guide
- **HOW_TO_GET_MAPBOX_TOKEN.md** - Detailed token guide
- **MAPBOX_MIGRATION_COMPLETE.md** - Migration details
- **TRACK_LOCATION_FINAL_STATUS.md** - Implementation status

---

## 🐛 Troubleshooting

### Map shows blank screen?
- Check Mapbox token is valid (starts with `pk.`)
- Ensure internet connection
- Rebuild app after adding token

### "Location permission not granted"?
- Grant permission when prompted
- Or enable in app settings

### "Unable to get location"?
- Enable GPS/Location services
- Go outdoors for better signal
- Wait 30-60s for first GPS fix

---

## 🎯 That's It!

The feature is fully implemented and ready to use. Just add your Mapbox token and test it out! 🗺️📍

**Need help?** See the full documentation files listed above.
