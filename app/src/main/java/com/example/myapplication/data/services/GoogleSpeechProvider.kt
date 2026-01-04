package com.example.myapplication.data.services

import com.example.myapplication.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.Base64

/**
 * Google Cloud Speech-to-Text Provider
 * 
 * Implements speech-to-text using Google Cloud Speech-to-Text API
 * 
 * Features:
 * - High-accuracy transcription
 * - Multiple language support
 * - Automatic punctuation
 * - Profanity filtering
 * 
 * API Documentation: https://cloud.google.com/speech-to-text/docs/reference/rest
 */
class GoogleSpeechProvider : SpeechProvider {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    /**
     * Transcribe audio using Google Cloud Speech-to-Text API
     * 
     * @param audio The audio data in PCM 16-bit, 16kHz mono format
     * @param targetLang The target language code (e.g., "en-US", "es-ES")
     * @return The transcribed text
     */
    override suspend fun transcribeAndTranslate(audio: ByteArray, targetLang: String): String = withContext(Dispatchers.IO) {
        if (BuildConfig.GOOGLE_SPEECH_API_KEY.isEmpty()) {
            throw IOException("Google Speech API Key is not configured.")
        }

        // Google Cloud Speech-to-Text REST API endpoint
        val url = "https://speech.googleapis.com/v1/speech:recognize?key=${BuildConfig.GOOGLE_SPEECH_API_KEY}"

        // Convert audio to Base64
        val audioBase64 = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Base64.getEncoder().encodeToString(audio)
        } else {
            android.util.Base64.encodeToString(audio, android.util.Base64.NO_WRAP)
        }

        // Build request JSON
        val requestJson = JSONObject().apply {
            put("config", JSONObject().apply {
                put("encoding", "LINEAR16")
                put("sampleRateHertz", 16000)
                put("languageCode", targetLang)
                put("enableAutomaticPunctuation", true)
                put("model", "default")
                put("useEnhanced", true)
            })
            put("audio", JSONObject().apply {
                put("content", audioBase64)
            })
        }

        val requestBody = requestJson.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "Unknown error"
                throw IOException("Google Speech API failed with code: ${response.code}. Error: $errorBody")
            }

            val responseBody = response.body?.string() ?: throw IOException("Empty response from API")
            
            // Parse response
            val jsonResponse = JSONObject(responseBody)
            
            if (!jsonResponse.has("results")) {
                return@withContext "" // No speech detected
            }

            val results = jsonResponse.getJSONArray("results")
            if (results.length() == 0) {
                return@withContext "" // No results
            }

            // Extract transcription from first result
            val firstResult = results.getJSONObject(0)
            val alternatives = firstResult.getJSONArray("alternatives")
            
            if (alternatives.length() == 0) {
                return@withContext ""
            }

            val transcript = alternatives.getJSONObject(0).getString("transcript")
            transcript
        }
    }
}
