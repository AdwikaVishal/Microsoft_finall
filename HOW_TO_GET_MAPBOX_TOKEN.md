# How to Get Your Mapbox Access Token

## Quick Start (5 minutes)

### Step 1: Sign Up for Mapbox (Free)

1. Go to [https://account.mapbox.com/auth/signup/](https://account.mapbox.com/auth/signup/)
2. Sign up with:
   - Email address
   - Username
   - Password
3. Verify your email
4. No credit card required for free tier!

### Step 2: Create Access Token

1. After login, you'll see the **Access Tokens** page automatically
   - Or go to: [https://account.mapbox.com/access-tokens/](https://account.mapbox.com/access-tokens/)

2. You'll see a **Default public token** already created
   - This token starts with `pk.`
   - You can use this token immediately!

3. **Option A: Use Default Token**
   - Click the copy icon next to the default token
   - Paste it into `local.properties`
   - Done!

4. **Option B: Create New Token (Recommended for Production)**
   - Click **"Create a token"** button
   - Token name: `SenseSafe Android`
   - Scopes: Keep all **Public scopes** checked (default)
   - URL restrictions: Leave empty for now (add later)
   - Click **"Create token"**
   - Copy the new token (starts with `pk.`)

### Step 3: Add Token to Project

Open `local.properties` in your project root and add:

```properties
MAPBOX_ACCESS_TOKEN=pk.eyJ1IjoieW91cnVzZXJuYW1lIiwiYSI6ImNsZjN4eXo5YjBhZmczZW1xdGp5cWp5cWoifQ.example_token_here
```

**Replace the example with your actual token!**

### Step 4: Rebuild App

```bash
./gradlew clean assembleDebug
```

---

## Free Tier Limits

Mapbox offers a generous free tier:

| Resource | Free Tier Limit | Typical Usage |
|----------|----------------|---------------|
| **Map Loads** | 50,000/month | ~1,600/day |
| **API Requests** | 100,000/month | ~3,300/day |
| **Geocoding** | 100,000/month | Not used yet |
| **Directions** | 100,000/month | Not used yet |

**For reference:**
- 1 map load = Opening the Track Location screen once
- Development + moderate production use = Well within limits
- Upgrade only if you exceed limits (unlikely for most apps)

---

## Token Security (Production)

### For Development:
- Use the default public token
- No restrictions needed
- Safe to use in client apps

### For Production:
1. **Create a production-specific token**
2. **Add URL restrictions:**
   - Go to token settings
   - Add: `com.example.myapplication://*`
   - This restricts token to your app only

3. **Monitor usage:**
   - Check [Mapbox Statistics](https://account.mapbox.com/statistics/)
   - Set up usage alerts
   - Track monthly consumption

4. **Rotate tokens periodically:**
   - Create new token
   - Update app
   - Delete old token after rollout

---

## Troubleshooting

### Issue: "Invalid access token" error

**Causes:**
- Token not copied correctly
- Token has expired (rare)
- Token was deleted from Mapbox dashboard

**Solutions:**
1. Verify token in `local.properties` starts with `pk.`
2. Check for extra spaces or line breaks
3. Generate a new token from Mapbox dashboard
4. Rebuild app after updating token

### Issue: "Token not found" error

**Cause:** `local.properties` not configured correctly

**Solution:**
1. Ensure `local.properties` is in project root (same level as `build.gradle.kts`)
2. Check file format:
   ```properties
   MAPBOX_ACCESS_TOKEN=pk.your_token_here
   ```
3. No quotes around the token
4. No spaces before/after `=`

### Issue: Map loads but shows "Unauthorized" error

**Cause:** Token doesn't have required scopes

**Solution:**
1. Go to [Access Tokens](https://account.mapbox.com/access-tokens/)
2. Click on your token
3. Ensure these scopes are checked:
   - ✅ `styles:read`
   - ✅ `fonts:read`
   - ✅ `datasets:read`
4. Save changes

---

## Alternative: MapTiler Only (No Mapbox)

If you prefer not to use Mapbox, you can use MapTiler SDK directly:

### Option 1: MapTiler SDK (Alternative)
```kotlin
// In build.gradle.kts
implementation("com.maptiler:maptiler-sdk-android:1.0.0")

// No Mapbox token needed
// Only MapTiler API key required
```

**Pros:**
- Simpler setup (one API key)
- Direct MapTiler integration

**Cons:**
- Less mature SDK
- Fewer features
- Smaller community

### Option 2: Mapbox + MapTiler (Current Implementation)
```kotlin
// In build.gradle.kts
implementation("com.mapbox.maps:android:11.0.0")

// Requires both:
// - Mapbox access token (for SDK)
// - MapTiler API key (for style)
```

**Pros:**
- Mature, stable SDK
- Rich feature set
- Large community
- Better documentation

**Cons:**
- Two API keys to manage
- Slightly more complex setup

**Recommendation:** Stick with Mapbox + MapTiler (current implementation) for production apps.

---

## Quick Reference

### Mapbox Dashboard Links:
- **Sign Up**: https://account.mapbox.com/auth/signup/
- **Access Tokens**: https://account.mapbox.com/access-tokens/
- **Statistics**: https://account.mapbox.com/statistics/
- **Documentation**: https://docs.mapbox.com/android/maps/guides/

### MapTiler Dashboard Links:
- **Sign Up**: https://cloud.maptiler.com/auth/widget
- **API Keys**: https://cloud.maptiler.com/account/keys/
- **Map Styles**: https://cloud.maptiler.com/maps/
- **Documentation**: https://docs.maptiler.com/

### Support:
- **Mapbox Support**: https://support.mapbox.com/
- **MapTiler Support**: https://support.maptiler.com/

---

## Summary

1. **Sign up** for free Mapbox account (no credit card)
2. **Copy** default public token (starts with `pk.`)
3. **Add** to `local.properties`:
   ```properties
   MAPBOX_ACCESS_TOKEN=pk.your_token_here
   ```
4. **Rebuild** app: `./gradlew clean assembleDebug`
5. **Test** Track Location feature

That's it! The MapTiler API key is already provided in the code, so you only need the Mapbox token to get started. 🗺️
