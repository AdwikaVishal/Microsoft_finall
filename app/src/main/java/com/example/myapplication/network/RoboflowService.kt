package com.example.myapplication.network

import com.example.myapplication.BuildConfig
import com.example.myapplication.model.RoboflowRequest
import com.example.myapplication.model.RoboflowResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * Retrofit interface for Roboflow Serverless Workflow API
 * 
 * This service handles detection requests to multiple Roboflow models:
 * - Windows detection
 * - Doors detection
 * - Hallways detection
 * - Stairs detection
 * 
 * API Configuration:
 * - URLs and API Keys are loaded from BuildConfig (configured in local.properties)
 * - Each model has its own URL and API key
 * - Supports dynamic URL selection based on detection type
 */
interface RoboflowService {
    @POST
    suspend fun detect(
        @Url url: String,
        @Body request: RoboflowRequest
    ): RoboflowResponse
    
    companion object {
        /**
         * Roboflow Serverless Workflow base URL
         */
        const val BASE_URL = "https://serverless.roboflow.com/"
        
        // ============================================================
        // MODEL URLs - Loaded from BuildConfig
        // ============================================================
        
        /**
         * Windows detection model URL
         */
        val WINDOWS_URL: String
            get() = BuildConfig.ROBOFLOW_WINDOWS_URL
        
        /**
         * Doors detection model URL
         */
        val DOORS_URL: String
            get() = BuildConfig.ROBOFLOW_DOORS_URL
        
        /**
         * Hallways detection model URL
         */
        val HALL_URL: String
            get() = BuildConfig.ROBOFLOW_HALL_URL
        
        /**
         * Stairs detection model URL
         */
        val STAIRS_URL: String
            get() = BuildConfig.ROBOFLOW_STAIRS_URL
        
        // ============================================================
        // API KEYS - Loaded from BuildConfig
        // ============================================================
        
        /**
         * Windows detection API key
         */
        val WINDOWS_API_KEY: String
            get() = BuildConfig.ROBOFLOW_WINDOWS_API_KEY
        
        /**
         * Doors detection API key
         */
        val DOORS_API_KEY: String
            get() = BuildConfig.ROBOFLOW_DOORS_API_KEY
        
        /**
         * Hallways detection API key
         */
        val HALL_API_KEY: String
            get() = BuildConfig.ROBOFLOW_HALL_API_KEY
        
        /**
         * Stairs detection API key
         */
        val STAIRS_API_KEY: String
            get() = BuildConfig.ROBOFLOW_STAIRS_API_KEY
        
        // ============================================================
        // CONFIGURATION CHECKS
        // ============================================================
        
        /**
         * Check if all model URLs are configured
         */
        fun areUrlsConfigured(): Boolean {
            return WINDOWS_URL.isNotEmpty() &&
                   DOORS_URL.isNotEmpty() &&
                   HALL_URL.isNotEmpty() &&
                   STAIRS_URL.isNotEmpty()
        }
        
        /**
         * Check if all API keys are configured
         */
        fun areApiKeysConfigured(): Boolean {
            return WINDOWS_API_KEY.isNotEmpty() &&
                   DOORS_API_KEY.isNotEmpty() &&
                   HALL_API_KEY.isNotEmpty() &&
                   STAIRS_API_KEY.isNotEmpty()
        }
        
        /**
         * Get list of all model URLs
         */
        fun getAllUrls(): List<String> = listOf(WINDOWS_URL, DOORS_URL, HALL_URL, STAIRS_URL)
        
        /**
         * Get URL by model name
         */
        fun getUrlForModel(modelName: String): String? {
            return when (modelName.lowercase()) {
                "windows", "window" -> WINDOWS_URL
                "doors", "door" -> DOORS_URL
                "hallways", "hall", "hallway" -> HALL_URL
                "stairs", "stair", "staircase" -> STAIRS_URL
                else -> null
            }
        }
        
        /**
         * Get API key by model name
         */
        fun getApiKeyForModel(modelName: String): String? {
            return when (modelName.lowercase()) {
                "windows", "window" -> WINDOWS_API_KEY
                "doors", "door" -> DOORS_API_KEY
                "hallways", "hall", "hallway" -> HALL_API_KEY
                "stairs", "stair", "staircase" -> STAIRS_API_KEY
                else -> null
            }
        }
    }
}

