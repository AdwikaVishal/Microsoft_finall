package com.example.myapplication.data.services

import android.util.Log
import com.example.myapplication.BuildConfig

/**
 * Service to handle speech-to-text and translation with fallback logic.
 */
class SpeechService {
    private val speechTextProvider = SpeechTextProvider()
    private val azureProvider = AzureSpeechProvider()

    /**
     * Transcribes audio and translates it to the target language.
     * Primary: Azure Speech Services, Fallback: SpeechText API (only if Azure fails).
     */
    suspend fun transcribeAndTranslate(audio: ByteArray, targetLang: String): String {
        // Try Azure (Primary)
        return try {
            val result = azureProvider.transcribeAndTranslate(audio, targetLang)
            Log.d("SpeechService", "Azure success")
            result
        } catch (e: Exception) {
            Log.w("SpeechService", "Azure failed → switching to SpeechText: ${e.message}")
            // Try SpeechText (Fallback)
            try {
                val result = speechTextProvider.transcribeAndTranslate(audio, targetLang)
                Log.d("SpeechService", "SpeechText success")
                result
            } catch (e: Exception) {
                Log.e("SpeechService", "SpeechText failed: ${e.message}")
                "Speech services are not configured yet."
            }
        }
    }
}
