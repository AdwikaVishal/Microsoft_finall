package com.example.myapplication

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.myapplication.utils.LocaleUtils
import com.example.myapplication.utils.LanguageHelper

open class BaseActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val savedLang = LanguageHelper.getSavedLanguage(newBase)
        super.attachBaseContext(LocaleUtils.applyLocale(newBase, savedLang))
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ensure locale is applied on activity creation
        val savedLang = LanguageHelper.getSavedLanguage(this)
        LocaleUtils.updateLocale(this, savedLang)
    }
}