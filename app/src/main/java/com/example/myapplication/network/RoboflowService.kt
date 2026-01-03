package com.example.myapplication.network

import com.example.myapplication.model.RoboflowRequest
import com.example.myapplication.model.RoboflowResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * Retrofit interface for Roboflow API
 * 
 * This service handles detection requests to multiple Roboflow models:
 * - Windows detection
 * - Doors detection
 * - Hallways detection
 * - Stairs detection
 * 
 * API Key Setup:
 * - Keys are loaded from BuildConfig:
 *   - RF_WINDOWS_KEY
 *   - RF_DOOR_KEY
 *   - RF_HALL_KEY
 *   - RF_STAIRS_KEY
 * 
 * URL Setup:
 * - Configure the model URLs below with your Roboflow model endpoints
 * - Format: "https://detect.roboflow.com/YOUR-MODEL-SLUG"
 *   (e.g., "https://detect.roboflow.com/windows-detection/1")
 */
interface RoboflowService {
    @POST
    suspend fun detect(
        @Url url: String,
        @Body request: RoboflowRequest
    ): RoboflowResponse
    
    companion object {
        /**
         * Roboflow base URL for all API calls
         */
        const val BASE_URL = "https://detect.roboflow.com/"
        
        // ============================================================
        // MODEL URLs - CONFIGURE THESE WITH YOUR ROBOFLOW MODELS
        // ============================================================
        // Replace the placeholders with your actual Roboflow model URLs
        // Find them in your Roboflow dashboard under each model's API section
        
        /**
         * Windows detection model URL
         * Configure: https://universe.roboflow.com/your-workspace/windows
         */
        private const val WINDOWS_URL_PLACEHOLDER = "YOUR WINDOWS ROBOFLOW URL"
        
        /**
         * Doors detection model URL
         * Configure: https://universe.roboflow.com/your-workspace/doors
         */
        private const val DOORS_URL_PLACEHOLDER = "YOUR DOORS ROBOFLOW URL"
        
        /**
         * Hallways detection model URL
         * Configure: https://universe.roboflow.com/your-workspace/hallways
         */
        private const val HALL_URL_PLACEHOLDER = "YOUR HALLWAYS ROBOFLOW URL"
        
        /**
         * Stairs detection model URL
         * Configure: https://universe.roboflow.com/your-workspace/stairs
         */
        private const val STAIRS_URL_PLACEHOLDER = "YOUR STAIRS ROBOFLOW URL"
        
        // Active URLs (update these with your actual Roboflow model URLs)
        // TODO: Replace placeholders with actual URLs from Roboflow dashboard
        val WINDOWS_URL: String = WINDOWS_URL_PLACEHOLDER
        val DOORS_URL: String = DOORS_URL_PLACEHOLDER
        val HALL_URL: String = HALL_URL_PLACEHOLDER
        val STAIRS_URL: String = STAIRS_URL_PLACEHOLDER
        
        /**
         * Check if all model URLs are configured
         */
        fun areUrlsConfigured(): Boolean {
            return WINDOWS_URL != WINDOWS_URL_PLACEHOLDER &&
                   DOORS_URL != DOORS_URL_PLACEHOLDER &&
                   HALL_URL != HALL_URL_PLACEHOLDER &&
                   STAIRS_URL != STAIRS_URL_PLACEHOLDER
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
                "windows" -> WINDOWS_URL
                "doors" -> DOORS_URL
                "hallways", "hall" -> HALL_URL
                "stairs", "stair" -> STAIRS_URL
                else -> null
            }
        }
    }
}

