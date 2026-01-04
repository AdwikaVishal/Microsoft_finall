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
 * - Sequential detection calls to 4 Roboflow models (windows, doors, hallways, stairs)
 * - Image resizing to reduce payload size (prevents SocketException)
 * - Merging results from all models
 * - Graceful error handling (skip failed API, continue others)
 * - Internet connectivity checks
 * - Extended timeouts for large image uploads
 * 
 * Configuration:
 * - API Keys: Load from BuildConfig (RF_WINDOWS_KEY, RF_DOOR_KEY, RF_HALL_KEY, RF_STAIRS_KEY)
 * - Model URLs: Configure in RoboflowService companion object
 * 
 * Flow:
 * 1. Resize Bitmap to max 1280x960 (reduces payload from ~2MB to ~200KB)
 * 2. Convert Bitmap to Base64
 * 3. Call all 4 APIs sequentially (not parallel to avoid network overload)
 * 4. Merge predictions from all models
 * 5. Return DetectionResult with merged results
 */
class RoboflowRepository(
    private val context: Context? = null
) {
    // ============================================================
    // RETROFIT SERVICE
    // ============================================================
    
    private val service: RoboflowService by lazy {
        val client = okhttp3.OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        
        val retrofit = Retrofit.Builder()
            .baseUrl(RoboflowService.BASE_URL)
            .client(client)
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
        return RoboflowService.WINDOWS_API_KEY.isNotEmpty() ||
               RoboflowService.DOORS_API_KEY.isNotEmpty() ||
               RoboflowService.HALL_API_KEY.isNotEmpty() ||
               RoboflowService.STAIRS_API_KEY.isNotEmpty()
    }
    
    /**
     * Get status of all API keys.
     */
    fun getApiKeyStatus(): String {
        val keys = mapOf(
            "Windows" to RoboflowService.WINDOWS_API_KEY,
            "Doors" to RoboflowService.DOORS_API_KEY,
            "Hallways" to RoboflowService.HALL_API_KEY,
            "Stairs" to RoboflowService.STAIRS_API_KEY
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
        android.util.Log.d("RoboflowRepository", "========================================")
        android.util.Log.d("RoboflowRepository", "🚀 STARTING DETECTION SCAN")
        android.util.Log.d("RoboflowRepository", "========================================")
        android.util.Log.d("RoboflowRepository", "Original bitmap size: ${bitmap.width}x${bitmap.height}")
        
        // Check internet connectivity first
        if (!isInternetAvailable()) {
            android.util.Log.e("RoboflowRepository", "❌ No internet connection")
            return@withContext DetectionResult.noExitsDetected().copy(
                exitMessage = "No internet connection"
            )
        }
        android.util.Log.d("RoboflowRepository", "✅ Internet connection available")
        
        // Check if at least one API key is configured
        if (!areApiKeysConfigured()) {
            android.util.Log.e("RoboflowRepository", "❌ No Roboflow API keys configured")
            return@withContext DetectionResult.noExitsDetected().copy(
                exitMessage = "No Roboflow API keys configured"
            )
        }
        android.util.Log.d("RoboflowRepository", "✅ API keys configured: ${getApiKeyStatus()}")
        
        // ✅ FIX 1: Resize image to reduce payload size (from ~2MB to ~200KB)
        val resizedBitmap = if (bitmap.width > 1280 || bitmap.height > 960) {
            val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
            val targetWidth: Int
            val targetHeight: Int
            
            if (aspectRatio > 1) {
                // Landscape
                targetWidth = 1280
                targetHeight = (1280 / aspectRatio).toInt()
            } else {
                // Portrait
                targetHeight = 960
                targetWidth = (960 * aspectRatio).toInt()
            }
            
            android.util.Log.d("RoboflowRepository", "🔄 Resizing bitmap to ${targetWidth}x${targetHeight}")
            Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
        } else {
            android.util.Log.d("RoboflowRepository", "✅ Bitmap already optimal size")
            bitmap
        }
        
        android.util.Log.d("RoboflowRepository", "Final bitmap size: ${resizedBitmap.width}x${resizedBitmap.height}")
        
        // Convert image to Base64
        android.util.Log.d("RoboflowRepository", "🔄 Converting bitmap to Base64...")
        val base64Image = bitmapToBase64(resizedBitmap)
        android.util.Log.d("RoboflowRepository", "✅ Base64 conversion complete: ${base64Image.length} characters")
        
        // ✅ FIX 2: Sequential API calls instead of parallel (avoid network overload)
        android.util.Log.d("RoboflowRepository", "🔄 Starting sequential API calls to 4 models...")
        val results = listOf(
            // Windows detection
            callModelApi(
                modelName = "windows",
                url = RoboflowService.WINDOWS_URL,
                apiKey = RoboflowService.WINDOWS_API_KEY,
                base64Image = base64Image
            ),
            // Doors detection
            callModelApi(
                modelName = "doors",
                url = RoboflowService.DOORS_URL,
                apiKey = RoboflowService.DOORS_API_KEY,
                base64Image = base64Image
            ),
            // Hallways detection
            callModelApi(
                modelName = "hallways",
                url = RoboflowService.HALL_URL,
                apiKey = RoboflowService.HALL_API_KEY,
                base64Image = base64Image
            ),
            // Stairs detection
            callModelApi(
                modelName = "stairs",
                url = RoboflowService.STAIRS_URL,
                apiKey = RoboflowService.STAIRS_API_KEY,
                base64Image = base64Image
            )
        )
        
        android.util.Log.d("RoboflowRepository", "✅ All API calls completed")
        android.util.Log.d("RoboflowRepository", "Results summary:")
        results.forEachIndexed { index, result ->
            android.util.Log.d("RoboflowRepository", "  Model ${index + 1} (${result.modelName}): ${result.predictionsOrEmpty.size} predictions, error: ${result.error ?: "none"}")
        }
        
        // Merge all predictions
        val finalResult = mergeResults(results)
        android.util.Log.d("RoboflowRepository", "========================================")
        android.util.Log.d("RoboflowRepository", "🏁 SCAN COMPLETE")
        android.util.Log.d("RoboflowRepository", "Total detections: ${finalResult.allDetections.size}")
        android.util.Log.d("RoboflowRepository", "Has exits: ${finalResult.hasExits}")
        android.util.Log.d("RoboflowRepository", "Message: ${finalResult.exitMessage}")
        android.util.Log.d("RoboflowRepository", "========================================")
        
        finalResult
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
            android.util.Log.w("RoboflowRepository", "[$modelName] API key not configured")
            return ModelDetectionResult(
                modelName = modelName,
                error = "API key not configured"
            )
        }
        
        // Skip if URL is still empty or not configured
        if (url.isEmpty()) {
            android.util.Log.w("RoboflowRepository", "[$modelName] URL not configured")
            return ModelDetectionResult(
                modelName = modelName,
                error = "URL not configured"
            )
        }
        
        return try {
            android.util.Log.d("RoboflowRepository", "[$modelName] 🚀 Starting API call")
            android.util.Log.d("RoboflowRepository", "[$modelName] URL: $url")
            android.util.Log.d("RoboflowRepository", "[$modelName] API Key: ${apiKey.take(10)}...")
            android.util.Log.d("RoboflowRepository", "[$modelName] Image size: ${base64Image.length} chars")
            
            val request = RoboflowRequest(
                api_key = apiKey,
                inputs = RoboflowInput(
                    image = RoboflowImage(
                        type = "base64",
                        value = base64Image
                    )
                )
            )
            
            android.util.Log.d("RoboflowRepository", "[$modelName] 📡 Sending request...")
            val response = service.detect(url, request)
            
            android.util.Log.d("RoboflowRepository", "[$modelName] ✅ Response received")
            android.util.Log.d("RoboflowRepository", "[$modelName] Predictions count: ${response.predictions?.size ?: 0}")
            
            response.predictions?.forEachIndexed { index, pred ->
                android.util.Log.d("RoboflowRepository", 
                    "[$modelName] Prediction $index: ${pred.class_name} (confidence: ${pred.confidence})")
            }
            
            ModelDetectionResult(
                modelName = modelName,
                predictions = response.predictions ?: emptyList()
            )
        } catch (e: Exception) {
            // Log error but don't crash - other models can still run
            android.util.Log.e("RoboflowRepository", "[$modelName] ❌ API call failed: ${e.message}", e)
            android.util.Log.e("RoboflowRepository", "[$modelName] Exception type: ${e.javaClass.simpleName}")
            
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

