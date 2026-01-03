package com.example.myapplication.data.services

import android.util.Log
import com.example.myapplication.BuildConfig
import com.example.myapplication.data.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class LibreTranslateService(private val userPreferencesRepository: UserPreferencesRepository) {
    private val client = OkHttpClient()

    /**
     * Translates text to the user's selected language using LibreTranslate.
     * @param text The text to translate.
     * @return The translated text, or the original text if translation fails.
     */
    suspend fun translate(text: String): String = withContext(Dispatchers.IO) {
        if (text.isBlank()) return@withContext ""

        val targetLang = userPreferencesRepository.language.first()
        // Assuming 'en' is the source language or default, so no translation needed if target is 'en'
        // Ideally, we should check source language too, but for UI strings coming from code (English), this is fine.
        if (targetLang == "en") return@withContext text

        if (BuildConfig.LIBRE_TRANSLATE_BASE_URL.isEmpty()) {
            Log.w("LibreTranslateService", "LibreTranslate Base URL not configured")
            return@withContext text
        }

        try {
            val jsonBody = JSONObject().apply {
                put("q", text)
                put("source", "en") // Assuming UI source is English
                put("target", targetLang)
                put("format", "text")
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())

            val request = Request.Builder()
                .url("${BuildConfig.LIBRE_TRANSLATE_BASE_URL.trimEnd('/')}/translate")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("LibreTranslateService", "Translation failed: ${response.code}")
                    return@withContext text
                }
                
                val responseString = response.body?.string() ?: return@withContext text
                val jsonResponse = JSONObject(responseString)
                jsonResponse.optString("translatedText", text)
            }
        } catch (e: Exception) {
            Log.e("LibreTranslateService", "Translation error: ${e.message}")
            text
        }
    }
}
