package com.example.myapplication.utils

import android.content.Context
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Helper to process images for "Real-time Exit" detection.
 * Uses ML Kit for local OCR as a first pass.
 */
class ObjectDetectorHelper(private val context: Context) {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun processImage(image: InputImage, onResult: (String) -> Unit) {
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                // Basic local heuristic for "EXIT" signs
                if (visionText.text.contains("EXIT", ignoreCase = true) || 
                    visionText.text.contains("EMERGENCY", ignoreCase = true)) {
                    onResult("EXIT SIGN DETECTED")
                } else {
                    onResult("Scanning...")
                }
                
                // Send to backend for advanced analysis (simulated)
                if (visionText.text.isNotEmpty()) {
                    sendToBackend(visionText.text)
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                onResult("Error scanning")
            }
    }

    private fun sendToBackend(scannedText: String) {
        CoroutineScope(Dispatchers.IO).launch {
             try {
                 // Placeholder: Send text to backend
             } catch (e: Exception) {
                 e.printStackTrace()
             }
        }
    }
}
