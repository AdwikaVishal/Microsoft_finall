package com.example.myapplication

import android.app.Application
import android.content.Context
import com.mapbox.common.MapboxOptions
import com.example.myapplication.utils.LocaleUtils
import com.example.myapplication.utils.LanguageHelper

/**
 * Application class for SenseSafe
 * 
 * Initializes:
 * - Mapbox SDK with access token
 * - Locale configuration with Android-native resource resolution
 * - Other app-wide configurations
 */
class SenseSafeApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Mapbox with access token
        val mapboxToken = getString(R.string.mapbox_access_token)
        MapboxOptions.accessToken = mapboxToken
    }
    
    override fun attachBaseContext(base: Context) {
        val savedLang = LanguageHelper.getSavedLanguage(base)
        super.attachBaseContext(LocaleUtils.applyLocale(base, savedLang))
    }
}
