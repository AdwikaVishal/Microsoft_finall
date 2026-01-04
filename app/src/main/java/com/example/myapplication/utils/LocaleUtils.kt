package com.example.myapplication.utils

import android.content.Context
import android.content.res.Configuration
import java.util.*

object LocaleUtils {
    fun applyLocale(context: Context, langCode: String): Context {
        val locale = when (langCode) {
            "zh-rCN" -> Locale("zh", "CN")
            else -> Locale(langCode)
        }
        
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        
        return context.createConfigurationContext(config)
    }
    
    fun updateLocale(context: Context, langCode: String) {
        val locale = when (langCode) {
            "zh-rCN" -> Locale("zh", "CN")
            else -> Locale(langCode)
        }
        
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
}