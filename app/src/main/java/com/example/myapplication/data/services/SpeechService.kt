package com.example.myapplication.data.services

import android.content.Context
import android.util.Log
import com.example.myapplication.BuildConfig

/**
 * Service to handle speech-to-text and translation with fallback logic.
 * 
 * Priority order:
 * 1. Android Native SpeechRecognizer (Primary - FREE, no API key needed)
 * 2. SpeechText.ai (Fallback 1 - if you have API key)
 * 3. Azure Speech Services (Fallback 2)
 * 4. Google Cloud Speech (Fallback 3)
 */
class SpeechService(private val context: Context? = null) {
    private val androidSpeechProvider by lazy { 
        context?.let { AndroidSpeechProvider(it) }
    }
    private val speechTextProvider = SpeechTextProvider()
    private val azureProvider = AzureSpeechProvider()
    private val googleSpeechProvider = GoogleSpeechProvider()

    /**
     * Transcribes audio and translates it to the target language.
     * Tries providers in order: Android Native → SpeechText → Azure → Google
     */
    suspend fun transcribeAndTranslate(audio: ByteArray, targetLang: String): String {
        // Try Android Native SpeechRecognizer (Primary - FREE!)
        if (androidSpeechProvider != null) {
            return try {
                val result = androidSpeechProvider!!.transcribeAndTranslate(audio, targetLang)
                Log.d("SpeechService", "Android Native Speech success")
                result
            } catch (e: Exception) {
                Log.w("SpeechService", "Android Native Speech failed → trying SpeechText.ai: ${e.message}")
                trySpeechTextFallback(audio, targetLang)
            }
        }
        
        // If Android Native not available, try SpeechText.ai
        return trySpeechTextFallback(audio, targetLang)
    }
    
    /**
     * Try SpeechText.ai as fallback, then Azure, then Google
     */
    private suspend fun trySpeechTextFallback(audio: ByteArray, targetLang: String): String {
        // Try SpeechText.ai
        if (BuildConfig.SPEECHTEXT_API_KEY.isNotEmpty()) {
            return try {
                val result = speechTextProvider.transcribeAndTranslate(audio, targetLang)
                Log.d("SpeechService", "SpeechText.ai success")
                result
            } catch (e: Exception) {
                Log.w("SpeechService", "SpeechText.ai failed → trying Azure: ${e.message}")
                tryAzureFallback(audio, targetLang)
            }
        }
        
        // If SpeechText not configured, try Azure
        return tryAzureFallback(audio, targetLang)
    }
    
    /**
     * Try Azure as fallback, then Google
     */
    private suspend fun tryAzureFallback(audio: ByteArray, targetLang: String): String {
        return try {
            val result = azureProvider.transcribeAndTranslate(audio, targetLang)
            Log.d("SpeechService", "Azure success")
            result
        } catch (e: Exception) {
            Log.w("SpeechService", "Azure failed → trying Google: ${e.message}")
            // Try Google (Final Fallback)
            try {
                val result = googleSpeechProvider.transcribeAndTranslate(audio, targetLang)
                Log.d("SpeechService", "Google Speech success")
                result
            } catch (e: Exception) {
                Log.e("SpeechService", "All speech services failed: ${e.message}")
                "Speech services are not configured yet."
            }
        }
    }
}
