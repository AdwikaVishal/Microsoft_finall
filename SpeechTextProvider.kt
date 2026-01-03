package com.example.myapplication.data.services

import com.example.myapplication.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class SpeechTextProvider : SpeechProvider {
    private val client = OkHttpClient()

    override suspend fun transcribeAndTranslate(audio: ByteArray, targetLang: String): String = withContext(Dispatchers.IO) {
        if (BuildConfig.SPEECHTEXT_API_KEY.isEmpty()) {
            throw IOException("SpeechText API Key is not configured.")
        }

        // Updated URL for SpeechText.ai API
        val url = "https://api.speechtext.ai/recognize"
        
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", "audio.wav", audio.toRequestBody("audio/wav".toMediaTypeOrNull()))
            .addFormDataPart("language", targetLang)
            .addFormDataPart("output", "txt")  // Request text output
            .build()

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer ${BuildConfig.SPEECHTEXT_API_KEY}")  // Try Bearer token instead
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("SpeechText API failed with code: ${response.code}")
            }
            response.body?.string() ?: ""
        }
    }
}
