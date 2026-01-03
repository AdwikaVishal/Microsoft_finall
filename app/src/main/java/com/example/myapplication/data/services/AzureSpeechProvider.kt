package com.example.myapplication.data.services

import com.example.myapplication.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class AzureSpeechProvider : SpeechProvider {
    private val client = OkHttpClient()

    override suspend fun transcribeAndTranslate(audio: ByteArray, targetLang: String): String = withContext(Dispatchers.IO) {
        if (BuildConfig.AZURE_KEY.isEmpty() || BuildConfig.AZURE_ENDPOINT.isEmpty()) {
            throw IOException("Azure API Key or Endpoint is not configured.")
        }

        // Parse region from endpoint if needed, or use full endpoint
        // Example endpoint in local.properties: https://eastus.api.cognitive.microsoft.com/
        // Speech URL format: https://<region>.stt.speech.microsoft.com/...
        // We will assume AZURE_ENDPOINT contains the base URL or region.
        // For safety, let's try to construct the URL.
        
        val endpoint = BuildConfig.AZURE_ENDPOINT.trimEnd('/')
        val url = if (endpoint.contains("stt.speech")) {
            "$endpoint/speech/recognition/conversation/cognitiveservices/v1?language=$targetLang"
        } else {
            // Fallback assumption: endpoint is just the region or general base
            // If it's a general URL, try to extract region, or just append standard path if it looks like a base
            "$endpoint/speech/recognition/conversation/cognitiveservices/v1?language=$targetLang"
        }

        val requestBody = audio.toRequestBody("audio/wav; codecs=audio/pcm; samplerate=16000".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url)
            .addHeader("Ocp-Apim-Subscription-Key", BuildConfig.AZURE_KEY)
            .addHeader("Accept", "application/json")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                // Try to read error body
                val errorBody = response.body?.string()
                throw IOException("Azure API failed with code: ${response.code}, message: $errorBody")
            }
            val responseString = response.body?.string() ?: ""
            // Azure returns JSON: {"RecognitionStatus":"Success","DisplayText":"..."}
            try {
                val json = JSONObject(responseString)
                if (json.getString("RecognitionStatus") == "Success") {
                    json.getString("DisplayText")
                } else {
                    throw IOException("Azure Recognition Failed: ${json.optString("RecognitionStatus")}")
                }
            } catch (e: Exception) {
                throw IOException("Failed to parse Azure response: ${e.message}")
            }
        }
    }
}
