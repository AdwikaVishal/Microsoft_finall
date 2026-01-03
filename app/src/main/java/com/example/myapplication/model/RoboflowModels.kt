package com.example.myapplication.model

/**
 * Data models for Roboflow API communication
 */
data class RoboflowRequest(
    val api_key: String,
    val inputs: RoboflowInput
)

data class RoboflowInput(
    val image: RoboflowImage
)

data class RoboflowImage(
    val type: String = "base64",
    val value: String
)

data class RoboflowResponse(
    val predictions: List<RoboflowPrediction>? = null
)

data class RoboflowPrediction(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val class_name: String? = null,
    val confidence: Float
)
