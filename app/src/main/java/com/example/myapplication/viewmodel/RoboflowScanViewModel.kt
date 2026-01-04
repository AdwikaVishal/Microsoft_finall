package com.example.myapplication.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.RoboflowRepository
import com.example.myapplication.model.BoundingBox
import com.example.myapplication.model.DetectedObject
import com.example.myapplication.model.DetectionResult
import com.example.myapplication.model.ScanResult
import com.example.myapplication.model.ScanUiState
import com.example.myapplication.network.RoboflowService
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * RoboflowScanViewModel - Manages scanning using 4 sequential Roboflow APIs
 * 
 * This ViewModel handles:
 * - Capturing images from CameraX
 * - Resizing images to prevent network errors
 * - Sending images to ALL 4 Roboflow models sequentially (windows, doors, hallways, stairs)
 * - Merging results from all models
 * - Managing UI state (Idle, Loading, Success, Error)
 * - Drawing bounding boxes on detected objects
 * - Speaking "Exit found" when any exit is detected
 * - Graceful error handling without app crashes
 * - No auto-cancel during API requests (prevents SocketException)
 * 
 * Architecture: MVVM pattern consistent with existing project
 * 
 * State Flow:
 * - Idle: Camera preview active, ready to scan
 * - Loading: "Scanning..." text shown, API requests in progress
 * - Success: Results available, bounding boxes drawn
 * - Error: Friendly error message, no crash
 * 
 * API Keys Configuration (in local.properties):
 * - RF_WINDOWS_KEY: API key for windows model
 * - RF_DOOR_KEY: API key for doors model
 * - RF_HALL_KEY: API key for hallways model
 * - RF_STAIRS_KEY: API key for stairs model
 * 
 * @property application Application context for initializing repository
 */
class RoboflowScanViewModel(application: Application) : AndroidViewModel(application) {

    // ============================================================
    // REPOSITORY
    // ============================================================
    
    /**
     * Roboflow repository for API interactions
     * Handles parallel detection calls to 4 models
     */
    private val roboflowRepository = RoboflowRepository(application)
    
    // ============================================================
    // UI STATE
    // ============================================================
    
    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()
    
    private val _scanResult = MutableStateFlow<ScanResult?>(null)
    val scanResult: StateFlow<ScanResult?> = _scanResult.asStateFlow()
    
    /**
     * Detection result from the 4-model parallel scan
     */
    private val _detectionResult = MutableStateFlow<DetectionResult?>(null)
    val detectionResult: StateFlow<DetectionResult?> = _detectionResult.asStateFlow()
    
    // ============================================================
    // CONFIGURATION STATUS
    // ============================================================
    
    private val _isConfigured = MutableStateFlow(false)
    val isConfigured: StateFlow<Boolean> = _isConfigured.asStateFlow()
    
    private val _apiKeyStatus = MutableStateFlow("Checking...")
    val apiKeyStatus: StateFlow<String> = _apiKeyStatus.asStateFlow()
    
    // ============================================================
    // SCAN JOB (for cancellation)
    // ============================================================
    
    /**
     * Job tracking the current scan operation.
     * Used to cancel the scan when user leaves the screen.
     */
    private var scanJob: Job? = null
    
    // ============================================================
    // INITIALIZATION
    // ============================================================
    
    init {
        checkConfiguration()
        
        // Debug: Log BuildConfig values for verification
        Log.d(TAG, "========================================")
        Log.d(TAG, "🔧 ROBOFLOW CONFIGURATION CHECK")
        Log.d(TAG, "========================================")
        Log.d(TAG, "WINDOWS_URL: ${RoboflowService.WINDOWS_URL}")
        Log.d(TAG, "WINDOWS_KEY: ${RoboflowService.WINDOWS_API_KEY.take(10)}...")
        Log.d(TAG, "DOORS_URL: ${RoboflowService.DOORS_URL}")
        Log.d(TAG, "DOORS_KEY: ${RoboflowService.DOORS_API_KEY.take(10)}...")
        Log.d(TAG, "HALL_URL: ${RoboflowService.HALL_URL}")
        Log.d(TAG, "HALL_KEY: ${RoboflowService.HALL_API_KEY.take(10)}...")
        Log.d(TAG, "STAIRS_URL: ${RoboflowService.STAIRS_URL}")
        Log.d(TAG, "STAIRS_KEY: ${RoboflowService.STAIRS_API_KEY.take(10)}...")
        Log.d(TAG, "Configuration status: ${if (isConfigured.value) "✅ READY" else "❌ NOT CONFIGURED"}")
        Log.d(TAG, "========================================")
    }
    
    /**
     * Check if all Roboflow models are properly configured.
     * Updates configuration status flows.
     */
    private fun checkConfiguration() {
        _isConfigured.value = roboflowRepository.areApiKeysConfigured() && 
                              roboflowRepository.areUrlsConfigured()
        _apiKeyStatus.value = roboflowRepository.getApiKeyStatus()
    }
    
    // ============================================================
    // SCAN OPERATIONS
    // ============================================================
    
    /**
     * Perform object detection on the given image using ALL 4 models sequentially.
     * 
     * This method:
     * 1. Cancels any existing scan job
     * 2. Sets UI to Loading state ("Scanning...")
     * 3. Launches a new coroutine for sequential API calls
     * 4. Calls 4 models: windows, doors, hallways, stairs (one after another)
     * 5. Merges all predictions
     * 6. Handles success, empty results, and error cases
     * 
     * @param bitmap The image to analyze
     */
    fun scanImage(bitmap: Bitmap) {
        // Cancel any in-progress scan
        cancelScan()
        
        // Set loading state with "Scanning..." message
        _uiState.value = ScanUiState.Loading
        _scanResult.value = null
        _detectionResult.value = null
        
        // Start new scan job
        scanJob = viewModelScope.launch {
            try {
                // Call all 4 models sequentially (not parallel to avoid network overload)
                val result = roboflowRepository.detectAllModels(bitmap)
                _detectionResult.value = result
                handleDetectionResult(result)
            } catch (e: Exception) {
                // Handle errors gracefully - no crash
                handleError(e.message ?: "An unknown error occurred")
            }
        }
    }
    
    /**
     * Handle the detection result from parallel API calls.
     * 
     * @param result Merged result from all 4 models
     */
    private fun handleDetectionResult(result: DetectionResult) {
        if (result.hasExits) {
            // Convert predictions to detected objects
            val detectedObjects = result.allDetections.map { pred ->
                DetectedObject(
                    name = pred.class_name ?: "Unknown",
                    confidence = pred.confidence,
                    boundingBox = BoundingBox(
                        // Roboflow returns CENTER coordinates
                        // Convert to top-left for drawing
                        x = pred.x - (pred.width / 2),
                        y = pred.y - (pred.height / 2),
                        width = pred.width,
                        height = pred.height
                    )
                )
            }
            
            val scanResult = ScanResult(
                tags = detectedObjects.map { it.name }.distinct(),
                objects = detectedObjects,
                confidence = detectedObjects.maxOfOrNull { it.confidence } ?: 0f,
                description = result.exitMessage
            )
            
            _uiState.value = ScanUiState.Success(scanResult)
            _scanResult.value = scanResult
            
            Log.d(TAG, "Exit found: ${detectedObjects.size} object(s) detected from 4 models")
        } else {
            // No exits detected
            Log.d(TAG, "No exits detected from any model")
            _uiState.value = ScanUiState.Success(
                ScanResult(
                    description = result.exitMessage,
                    tags = emptyList(),
                    objects = emptyList(),
                    confidence = 0f
                )
            )
            _scanResult.value = ScanResult(description = result.exitMessage)
        }
    }
    
    /**
     * Handle scan errors with friendly messages.
     * 
     * @param errorMessage The error message to display
     */
    private fun handleError(errorMessage: String) {
        Log.e(TAG, "Scan error: $errorMessage")
        
        // Create user-friendly error message
        val friendlyMessage = when {
            errorMessage.contains("No internet", ignoreCase = true) -> 
                "No internet connection. Please check your network and try again."
            errorMessage.contains("API Key", ignoreCase = true) -> 
                "Scan feature not configured. Please add your Roboflow API keys."
            errorMessage.contains("URL not configured", ignoreCase = true) -> 
                "Model URLs not configured. Please update RoboflowService.kt"
            errorMessage.contains("connection", ignoreCase = true) -> 
                "Unable to connect to the server. Please try again later."
            else -> "Scan failed: ${errorMessage}"
        }
        
        _uiState.value = ScanUiState.Error(friendlyMessage)
    }
    
    // ============================================================
    // CANCELLATION
    // ============================================================
    
    /**
     * Cancel any in-progress scan operation.
     * 
     * Call this when the user leaves the camera screen
     * to prevent memory leaks and unnecessary API calls.
     */
    fun cancelScan() {
        scanJob?.cancel()
        scanJob = null
        Log.d(TAG, "Scan cancelled")
    }
    
    // ============================================================
    // STATE MANAGEMENT
    // ============================================================
    
    /**
     * Reset the scan state to idle.
     * Call this when the user dismisses results or wants to scan again.
     */
    fun resetState() {
        cancelScan()
        _uiState.value = ScanUiState.Idle
        _scanResult.value = null
        _detectionResult.value = null
        Log.d(TAG, "State reset to Idle")
    }
    
    /**
     * Clear error state and return to idle.
     * Call this when the user dismisses an error message.
     */
    fun clearError() {
        if (_uiState.value is ScanUiState.Error) {
            _uiState.value = ScanUiState.Idle
        }
    }
    
    /**
     * Show a toast message for errors.
     * 
     * @param context Android context for showing Toast
     * @param message Message to display
     */
    fun showErrorToast(context: android.content.Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
    
    /**
     * Check if an exit was detected in the last scan.
     * Used for triggering "Exit found" speech.
     * 
     * @return true if any exit was detected
     */
    fun wasExitDetected(): Boolean {
        return _detectionResult.value?.hasExits == true
    }
    
    /**
     * Get the exit message for speech output.
     * 
     * @return "Exit found" if exit detected, null otherwise
     */
    fun getExitSpeechMessage(): String? {
        return if (_detectionResult.value?.hasExits == true) {
            "Exit found — follow highlighted area"
        } else {
            null
        }
    }
    
    // ============================================================
    // CLEANUP
    // ============================================================
    
    /**
     * Called when ViewModel is cleared.
     * Ensures any in-progress scan is cancelled.
     */
    override fun onCleared() {
        super.onCleared()
        cancelScan()
        Log.d(TAG, "ViewModel cleared, scan cancelled")
    }
    
    companion object {
        private const val TAG = "RoboflowScanViewModel"
    }
}

/**
 * Factory for creating RoboflowScanViewModel with Application context.
 * 
 * This factory follows the existing ViewModelFactory pattern
 * in the project for proper dependency injection.
 */
class RoboflowScanViewModelFactory(
    private val application: Application
) : androidx.lifecycle.ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoboflowScanViewModel::class.java)) {
            return RoboflowScanViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

