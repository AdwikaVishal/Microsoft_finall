package com.example.myapplication.model

/**
 * Represents the merged detection result from multiple Roboflow models.
 * 
 * This data class combines results from 4 detection APIs:
 * - Windows
 * - Doors  
 * - Hallways
 * - Stairs
 * 
 * @property allDetections All predictions from all models
 * @property hasExits Whether any exit was detected
 * @property exitMessage User-friendly message about exits
 */
data class DetectionResult(
    val allDetections: List<RoboflowPrediction> = emptyList(),
    val hasExits: Boolean = false,
    val exitMessage: String = "No exits detected yet"
) {
    /**
     * Get detections grouped by model type
     */
    val windowsDetections: List<RoboflowPrediction>
        get() = allDetections.filter { it.class_name?.lowercase()?.contains("window") == true }
    
    val doorsDetections: List<RoboflowPrediction>
        get() = allDetections.filter { it.class_name?.lowercase()?.contains("door") == true }
    
    val hallwaysDetections: List<RoboflowPrediction>
        get() = allDetections.filter { it.class_name?.lowercase()?.contains("hallway") == true }
    
    val stairsDetections: List<RoboflowPrediction>
        get() = allDetections.filter { it.class_name?.lowercase()?.contains("stair") == true }
    
    /**
     * Get total count of all detections
     */
    val totalCount: Int
        get() = allDetections.size
    
    /**
     * Check if any model returned results
     */
    val isEmpty: Boolean
        get() = allDetections.isEmpty()
    
    companion object {
        /**
         * Create a "no exits detected" result
         */
        fun noExitsDetected(): DetectionResult = DetectionResult(
            allDetections = emptyList(),
            hasExits = false,
            exitMessage = "No exits detected yet — keep scanning"
        )
        
        /**
         * Create an "exit found" result
         */
        fun exitFound(detections: List<RoboflowPrediction>): DetectionResult {
            val exitTypes = detections.mapNotNull { it.class_name }.distinct()
            return DetectionResult(
                allDetections = detections,
                hasExits = true,
                exitMessage = "Exit found — follow highlighted area"
            )
        }
    }
}

/**
 * Result wrapper for individual model detection
 * 
 * @property modelName Name of the model (windows/doors/hallways/stairs)
 * @property predictions Predictions from this model (null if failed)
 * @property error Error message if API call failed (null if successful)
 */
data class ModelDetectionResult(
    val modelName: String,
    val predictions: List<RoboflowPrediction>? = null,
    val error: String? = null
) {
    /**
     * Check if this model's detection was successful
     */
    val isSuccess: Boolean
        get() = error == null && predictions != null
    
    /**
     * Get predictions or empty list
     */
    val predictionsOrEmpty: List<RoboflowPrediction>
        get() = predictions ?: emptyList()
}

