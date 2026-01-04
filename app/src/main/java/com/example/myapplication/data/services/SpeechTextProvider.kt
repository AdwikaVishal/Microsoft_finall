package com.example.myapplication.data.services

import com.example.myapplication.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * SpeechText.ai Provider
 * 
 * API Documentation: https://speechtext.ai/speech-recognition-api
 * 
 * Features:
 * - Multi-language support
 * - High accuracy
 * - Punctuation and formatting
 */
class SpeechTextProvider : SpeechProvider {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    override suspend fun transcribeAndTranslate(audio: ByteArray, targetLang: String): String = withContext(Dispatchers.IO) {
        if (BuildConfig.SPEECHTEXT_API_KEY.isEmpty()) {
            throw IOException("SpeechText API Key is not configured.")
        }

        // SpeechText.ai API endpoint
        val url = "https://api.speechtext.ai/recognize"
        
        // Create temporary file for audio
        val tempFile = File.createTempFile("audio", ".wav")
        try {
            FileOutputStream(tempFile).use { it.write(audio) }
            
            // Build multipart request
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "audio_file",
                    "audio.wav",
                    tempFile.asRequestBody("audio/wav".toMediaType())
                )
                .addFormDataPart("language", mapLanguageCode(targetLang))
                .addFormDataPart("punctuation", "true")
                .addFormDataPart("format", "json")
                .build()

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer ${BuildConfig.SPEECHTEXT_API_KEY}")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "Unknown error"
                    throw IOException("SpeechText API failed with code: ${response.code}. Error: $errorBody")
                }
                
                val responseBody = response.body?.string() ?: throw IOException("Empty response")
                
                // Parse JSON response
                val jsonResponse = JSONObject(responseBody)
                
                // Extract transcript from response
                // SpeechText.ai returns: {"status": "success", "results": [{"transcript": "..."}]}
                if (jsonResponse.has("results")) {
                    val results = jsonResponse.getJSONArray("results")
                    if (results.length() > 0) {
                        val firstResult = results.getJSONObject(0)
                        return@withContext firstResult.optString("transcript", "")
                    }
                }
                
                // Alternative format: {"transcript": "..."}
                if (jsonResponse.has("transcript")) {
                    return@withContext jsonResponse.getString("transcript")
                }
                
                // Alternative format: {"text": "..."}
                if (jsonResponse.has("text")) {
                    return@withContext jsonResponse.getString("text")
                }
                
                ""
            }
        } finally {
            tempFile.delete()
        }
    }
    
    /**
     * Map language codes to SpeechText.ai format
     * SpeechText.ai uses ISO 639-1 codes (e.g., "en", "es", "fr")
     */
    private fun mapLanguageCode(languageCode: String): String {
        return when {
            languageCode.startsWith("en") -> "en-US"
            languageCode.startsWith("es") -> "es-ES"
            languageCode.startsWith("fr") -> "fr-FR"
            languageCode.startsWith("de") -> "de-DE"
            languageCode.startsWith("it") -> "it-IT"
            languageCode.startsWith("pt") -> "pt-BR"
            languageCode.startsWith("ja") -> "ja-JP"
            languageCode.startsWith("ko") -> "ko-KR"
            languageCode.startsWith("zh") -> "zh-CN"
            else -> languageCode
        }
    }
}
