package com.example.myapplication.utils

import android.app.Activity
import android.content.Context

object LanguageHelper {
    private const val PREFS_NAME = "lang_prefs"
    private const val LANG_KEY = "LANG"
    
    // Supported languages - Android resource qualifier format
    val SUPPORTED_LANGUAGES = mapOf(
        "en" to "English",
        "es" to "Español", 
        "fr" to "Français",
        "de" to "Deutsch",
        "zh-rCN" to "中文 (简体)",
        "ja" to "日本語"
    )

    fun saveLanguage(context: Context, langCode: String) {
        if (!SUPPORTED_LANGUAGES.containsKey(langCode)) return
        
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(LANG_KEY, langCode)
            .apply()
    }

    fun getSavedLanguage(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(LANG_KEY, "en") ?: "en"
    }
    
    fun changeLanguage(context: Context, langCode: String) {
        if (!SUPPORTED_LANGUAGES.containsKey(langCode)) return
        
        val currentLang = getSavedLanguage(context)
        if (currentLang == langCode) return // No change needed
        
        // Save the language preference
        saveLanguage(context, langCode)
        
        // Apply locale immediately
        LocaleUtils.updateLocale(context, langCode)
        
        // Recreate activity to apply changes
        if (context is Activity) {
            context.recreate()
        }
    }
}
