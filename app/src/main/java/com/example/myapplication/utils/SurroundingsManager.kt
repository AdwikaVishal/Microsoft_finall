package com.example.myapplication.utils

import com.example.myapplication.data.AbilityProfile
import kotlin.random.Random

/**
 * UPGRADE 2: SCAN SURROUNDINGS — CONSISTENT OUTPUT
 * Manages interpretation of surroundings for different accessibility needs.
 * 
 * Improvements:
 * - Added Confidence Check (Mocked for now, but infrastructure is ready)
 * - Standardized output per profile
 * - Graceful failure if data is empty
 */
object SurroundingsManager {

    data class SurroundingsInfo(
        val description: String,
        val distanceMeters: Int,
        val hazard: String?,
        val direction: String,
        val visualIcon: String, // Name of icon to show
        val possibleExit: Boolean = false,
        val confidence: Float = 1.0f // New confidence score
    )

    // Mock dataset mimicking a CSV loaded into memory
    private val mockData = listOf(
        SurroundingsInfo("Exit", 10, null, "Ahead", "arrow_straight", true, 0.9f),
        SurroundingsInfo("Stairs", 5, "Collapsing", "Left", "warning_stairs", false, 0.8f),
        SurroundingsInfo("Corridor", 15, "Blocked", "Right", "stop_sign", false, 0.95f),
        SurroundingsInfo("Window", 2, null, "Right", "window_icon", false, 0.6f),
        SurroundingsInfo("Fire Extinguisher", 3, null, "Left", "extinguisher_icon", false, 0.85f),
        SurroundingsInfo("Safe Zone", 20, null, "Ahead", "safe_zone_icon", true, 0.9f)
    )

    fun scan(profile: AbilityProfile): SurroundingsResult {
        // Simulate analyzing current camera frame and matching with dataset
        val info = mockData.random()

        // Scan Confidence Check
        if (info.confidence < 0.5f) {
             return SurroundingsResult.Audio("Scan incomplete. Please move the phone slowly.")
        }

        return when (profile) {
            AbilityProfile.BLIND -> {
                val text = buildString {
                    append("Scanning complete. ")
                    append("${info.description} is ${info.distanceMeters} meters ${info.direction}. ")
                    if (info.hazard != null) {
                        append("Warning: ${info.hazard}. ")
                    }
                    if (info.possibleExit) {
                         append("Possible exit detected ahead. Proceed carefully.")
                    }
                }
                SurroundingsResult.Audio(text)
            }
            AbilityProfile.DEAF -> {
                SurroundingsResult.Visual(
                    text = "${info.description} ${info.distanceMeters}m",
                    icon = info.visualIcon,
                    direction = info.direction,
                    hazard = info.hazard,
                    isExit = info.possibleExit
                )
            }
            AbilityProfile.ELDERLY -> {
                // Simplified text
                val text = if (info.hazard != null) {
                    "DANGER: ${info.hazard}"
                } else {
                    "Go ${info.direction} to ${info.description}"
                }
                SurroundingsResult.SimpleText(text)
            }
            AbilityProfile.NON_VERBAL -> {
                SurroundingsResult.Visual(
                    text = info.description,
                    icon = info.visualIcon,
                    direction = info.direction,
                    hazard = info.hazard,
                    isExit = info.possibleExit
                )
            }
            else -> {
                SurroundingsResult.Full(info)
            }
        }
    }

    sealed class SurroundingsResult {
        data class Audio(val message: String) : SurroundingsResult()
        data class Visual(val text: String, val icon: String, val direction: String, val hazard: String?, val isExit: Boolean) : SurroundingsResult()
        data class SimpleText(val message: String) : SurroundingsResult()
        data class Full(val info: SurroundingsInfo) : SurroundingsResult()
    }
}
