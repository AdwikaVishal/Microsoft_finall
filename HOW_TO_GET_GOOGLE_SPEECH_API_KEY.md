# How to Get Google Cloud Speech-to-Text API Key

## 🚨 Current Issue

The key `b5adb7fe5d424661928c6ee323684480` is **NOT a valid Google Cloud API key**.

**Error:**
```
API key not valid. Please pass a valid API key.
```

---

## ✅ How to Get a Valid Google Cloud API Key

### Step 1: Create Google Cloud Account

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Sign in with your Google account
3. Accept terms of service
4. **Note:** You may need to add a credit card, but there's a generous free tier

### Step 2: Create a New Project

1. Click the project dropdown at the top
2. Click "New Project"
3. Name: "SenseSafe" (or any name)
4. Click "Create"
5. Wait for project creation (30 seconds)

### Step 3: Enable Speech-to-Text API

1. Go to [Speech-to-Text API](https://console.cloud.google.com/apis/library/speech.googleapis.com)
2. Make sure your project is selected
3. Click "Enable"
4. Wait for API to be enabled (30 seconds)

### Step 4: Create API Key

1. Go to [Credentials](https://console.cloud.google.com/apis/credentials)
2. Click "Create Credentials" → "API Key"
3. Copy the API key (starts with `AIza...`)
4. **Important:** Click "Restrict Key" (recommended)

### Step 5: Restrict API Key (Recommended)

1. Click on the API key you just created
2. Under "API restrictions":
   - Select "Restrict key"
   - Check "Cloud Speech-to-Text API"
3. Under "Application restrictions":
   - Select "Android apps"
   - Click "Add an item"
   - Package name: `com.example.myapplication`
   - SHA-1: Get from your keystore (see below)
4. Click "Save"

### Step 6: Get SHA-1 Fingerprint (For Restrictions)

```bash
# Debug keystore (for development)
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android

# Look for SHA1 line, copy the fingerprint
```

---

## 🔧 Configure in Your App

### Update local.properties:

```properties
# Replace with your actual Google Cloud API key
GOOGLE_SPEECH_API_KEY=AIzaSyBdVl-cGnzGZnHYxikqe5fhsdwxLJoy9gs
```

### Rebuild App:

```bash
./gradlew clean assembleDebug
```

---

## 💰 Free Tier Limits

Google Cloud Speech-to-Text offers:

| Feature | Free Tier |
|---------|-----------|
| **Standard Model** | 60 minutes/month |
| **Enhanced Model** | 60 minutes/month |
| **Data Logging** | Optional (for better models) |

**After free tier:**
- Standard: $0.006 per 15 seconds
- Enhanced: $0.009 per 15 seconds

**For your app:**
- 10-second recording = ~$0.004 per request
- 1000 requests = ~$4

---

## 🔄 Alternative: Use Existing Providers

If you don't want to set up Google Cloud, your app already has fallback providers:

### Option 1: Use Azure Speech (Already Configured)

Azure is already in your code. Just add credentials to `local.properties`:

```properties
AZURE_KEY=your_azure_key_here
AZURE_ENDPOINT=https://eastus.api.cognitive.microsoft.com/
```

Get Azure key from: https://portal.azure.com/

### Option 2: Use SpeechText.ai (Already Configured)

Add to `local.properties`:

```properties
SPEECHTEXT_API_KEY=your_speechtext_key_here
```

Get key from: https://speechtext.ai/

### Option 3: Use Android's Built-in Speech Recognition (Free!)

I can modify the code to use Android's native `SpeechRecognizer` which is:
- ✅ Free (no API key needed)
- ✅ Works offline
- ✅ Good accuracy
- ✅ No setup required

Would you like me to implement this?

---

## 🎯 Recommended Solution

**For Development/Testing:**
Use Android's built-in SpeechRecognizer (free, no setup)

**For Production:**
Use Google Cloud Speech-to-Text (best accuracy, $4/1000 requests)

---

## 📝 Summary

The key you provided (`b5adb7fe5d424661928c6ee323684480`) is not a Google Cloud API key. 

**Next Steps:**
1. Get a real Google Cloud API key (see steps above)
2. OR use Azure/SpeechText.ai (add their keys)
3. OR use Android's built-in speech recognition (I can implement this)

Let me know which option you prefer!
