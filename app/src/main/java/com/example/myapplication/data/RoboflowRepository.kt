package com.example.myapplication.data

import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Base64
import com.example.myapplication.BuildConfig
import com.example.myapplication.model.DetectionResult
import com.example.myapplication.model.ModelDetectionResult
import com.example.myapplication.model.RoboflowImage
import com.example.myapplication.model.RoboflowInput
import com.example.myapplication.model.RoboflowPrediction
import com.example.myapplication.model.RoboflowRequest
import com.example.myapplication.network.RoboflowService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream

/**
 * Repository for Roboflow detection APIs
 * 
 * This repository handles:
 * - Parallel detection calls to 4 Roboflow models (windows, doors, hallways, stairs)
 * - Merging results from all models
 * - Graceful error handling (skip failed API, continue others)
 * - Internet connectivity checks
 * 
 * Configuration:
 * - API Keys: Load from BuildConfig (RF_WINDOWS_KEY, RF_DOOR_KEY, RF_HALL_KEY, RF_STAIRS_KEY)
 * - Model URLs: Configure in RoboflowService companion object
 * 
 * Flow:
 * 1. Convert Bitmap to Base64
 * 2. Call all 4 APIs in parallel using awaitAll
 * 3. Merge predictions from all models
 * 4. Return DetectionResult with merged results
 */
class RoboflowRepository(
    private val context: Context? = null
) {
    // ============================================================
    // RETROFIT SERVICE
    // ============================================================
    
    private val service: RoboflowService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(RoboflowService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(RoboflowService::class.java)
    }
    
    // ============================================================
    // API KEY CHECKS
    // ============================================================
    
    /**
     * Check if all required API keys are configured.
     * 
     * @return true if at least one model has a valid API key
     */
    fun areApiKeysConfigured(): Boolean {
        return BuildConfig.RF_WINDOWS_KEY.isNotEmpty() ||
               BuildConfig.RF_DOOR_KEY.isNotEmpty() ||
               BuildConfig.RF_HALL_KEY.isNotEmpty() ||
               BuildConfig.RF_STAIRS_KEY.isNotEmpty()
    }
    
    /**
     * Get status of all API keys.
     */
    fun getApiKeyStatus(): String {
        val keys = mapOf(
            "Windows" to BuildConfig.RF_WINDOWS_KEY,
            "Doors" to BuildConfig.RF_DOOR_KEY,
            "Hallways" to BuildConfig.RF_HALL_KEY,
            "Stairs" to BuildConfig.RF_STAIRS_KEY
        )
        
        val configured = keys.count { it.value.isNotEmpty() }
        return "$configured/4 models configured"
    }
    
    // ============================================================
    // CONNECTIVITY CHECK
    // ============================================================
    
    /**
     * Check if device has internet connectivity.
     */
    fun isInternetAvailable(): Boolean {
        if (context == null) return true // Assume available if no context
        
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) 
            as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    // ============================================================
    // MAIN DETECTION METHOD - Parallel API Calls
    // ============================================================
    
    /**
     * Detect exits by calling all 4 Roboflow models in parallel.
     * 
     * This method:
     * 1. Checks internet connectivity
     * 2. Converts Bitmap to Base64
     * 3. Makes parallel API calls to all 4 models
     * 4. Merges all predictions into a single DetectionResult
     * 5. Gracefully handles individual API failures (skips failed models)
     * 
     * Models called:
     * - Windows (RF_WINDOWS_KEY + WINDOWS_URL)
     * - Doors (RF_DOOR_KEY + DOORS_URL)
     * - Hallways (RF_HALL_KEY + HALL_URL)
     * - Stairs (RF_STAIRS_KEY + STAIRS_URL)
     * 
     * @param bitmap The image to analyze
     * @return DetectionResult with merged predictions from all models
     */
    suspend fun detectAllModels(bitmap: Bitmap): DetectionResult = withContext(Dispatchers.IO) {
        // Check internet connectivity first
        if (!isInternetAvailable()) {
            return@withContext DetectionResult.noExitsDetected().copy(
                exitMessage = "No internet connection"
            )
        }
        
        // Check if at least one API key is configured
        if (!areApiKeysConfigured()) {
            return@withContext DetectionResult.noExitsDetected().copy(
                exitMessage = "No Roboflow API keys configured"
            )
        }
        
        // Convert image to Base64
        val base64Image = bitmapToBase64(bitmap)
        
        // Make parallel API calls to all 4 models
        // Using async/awaitAll for non-blocking parallel execution
        val results = awaitAll(
            // Windows detection
            async { 
                callModelApi(
                    modelName = "windows",
                    url = RoboflowService.WINDOWS_URL,
                    apiKey = BuildConfig.RF_WINDOWS_KEY,
                    base64Image = base64Image
                )
            },
            // Doors detection
            async { 
                callModelApi(
                    modelName = "doors",
                    url = RoboflowService.DOORS_URL,
                    apiKey = BuildConfig.RF_DOOR_KEY,
                    base64Image = base64Image
                )
            },
            // Hallways detection
            async { 
                callModelApi(
                    modelName = "hallways",
                    url = RoboflowService.HALL_URL,
                    apiKey = BuildConfig.RF_HALL_KEY,
                    base64Image = base64Image
                )
            },
            // Stairs detection
            async { 
                callModelApi(
                    modelName = "stairs",
                    url = RoboflowService.STAIRS_URL,
                    apiKey = BuildConfig.RF_STAIRS_KEY,
                    base64Image = base64Image
                )
            }
        )
        
        // Merge all predictions
        mergeResults(results)
    }
    
    /**
     * Call a single model's API endpoint.
     * 
     * If API key is empty or URL is not configured, returns a failed result
     * without throwing an exception (graceful handling).
     * 
     * @param modelName Name of the model (for logging)
     * @param url Full Roboflow API URL
     * @param apiKey API key for this model
     * @param base64Image Base64-encoded image
     * @return ModelDetectionResult with predictions or error
     */
    private suspend fun callModelApi(
        modelName: String,
        url: String,
        apiKey: String,
        base64Image: String
    ): ModelDetectionResult {
        // Skip if API key is not configured
        if (apiKey.isEmpty()) {
            return ModelDetectionResult(
                modelName = modelName,
                error = "API key not configured"
            )
        }
        
        // Skip if URL is still a placeholder
        if (url.startsWith("YOUR ")) {
            return ModelDetectionResult(
                modelName = modelName,
                error = "URL not configured"
            )
        }
        
        return try {
            val request = RoboflowRequest(
                api_key = apiKey,
                inputs = RoboflowInput(
                    image = RoboflowImage(
                        type = "base64",
                        value = base64Image
                    )
                )
            )
            
            val response = service.detect(url, request)
            
            ModelDetectionResult(
                modelName = modelName,
                predictions = response.predictions ?: emptyList()
            )
        } catch (e: Exception) {
            // Log error but don't crash - other models can still run
            ModelDetectionResult(
                modelName = modelName,
                error = e.message ?: "Unknown error"
            )
        }
    }
    
    /**
     * Merge results from all 4 model API calls.
     * 
     * Combines predictions from successful calls into a single DetectionResult.
     * If all calls fail, returns "No exits detected yet".
     * 
     * @param results List of results from each model
     * @return DetectionResult with merged predictions
     */
    private fun mergeResults(results: List<ModelDetectionResult>): DetectionResult {
        // Collect all predictions from successful calls
        val allPredictions = results.flatMap { result ->
            result.predictionsOrEmpty
        }
        
        // Log results for debugging
        results.forEach { result ->
            if (!result.isSuccess) {
                // Silently skip failed models (graceful handling)
            }
        }
        
        return if (allPredictions.isEmpty()) {
            // No detections from any model
            DetectionResult.noExitsDetected()
        } else {
            // Exit found - merge predictions
            DetectionResult.exitFound(allPredictions)
        }
    }
    
    // ============================================================
    // LEGACY METHOD - Single Model Detection
    // ============================================================
    
    /**
     * Detect objects using a single Roboflow model.
     * Kept for backward compatibility.
     * 
     * @param bitmap The image to analyze
     * @param url Full Roboflow API URL
     * @param apiKey API key
     * @return List of predictions
     */
    @Deprecated("Use detectAllModels() for parallel detection")
    suspend fun detectSingleModel(
        bitmap: Bitmap,
        url: String,
        apiKey: String
    ): List<RoboflowPrediction> = withContext(Dispatchers.IO) {
        if (apiKey.isEmpty() || url.startsWith("YOUR ")) {
            return@withContext emptyList()
        }
        
        if (!isInternetAvailable()) {
            return@withContext emptyList()
        }
        
        val base64Image = bitmapToBase64(bitmap)
        
        try {
            val request = RoboflowRequest(
                api_key = apiKey,
                inputs = RoboflowInput(
                    image = RoboflowImage(
                        type = "base64",
                        value = base64Image
                    )
                )
            )
            
            service.detect(url, request).predictions ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // ============================================================
    // HELPER METHODS
    // ============================================================
    
    /**
     * Convert Bitmap to Base64-encoded JPEG string.
     */
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }
    
    /**
     * Check if model URLs are configured.
     */
    fun areUrlsConfigured(): Boolean = RoboflowService.areUrlsConfigured()
}

