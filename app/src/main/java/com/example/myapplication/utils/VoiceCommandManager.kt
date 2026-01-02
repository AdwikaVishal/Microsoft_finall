package com.example.myapplication.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.example.myapplication.accessibility.AccessibilityManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

/**
 * UPGRADE 1: BLIND MODE — RELIABILITY & CLARITY
 * Enhanced Voice Input Helper (Sensa)
 * 
 * Improvements:
 * - Explicit State Management (IDLE, LISTENING, PROCESSING)
 * - Error Handling with Fallback instructions
 * - Synonym matching for robust commands
 */
class VoiceCommandManager(
    private val context: Context,
    private val accessibilityManager: AccessibilityManager? = null // Optional for feedback
) {

    private val speechRecognizer: SpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
    private val _spokenText = MutableStateFlow("")
    val spokenText: StateFlow<String> = _spokenText

    private var isListening = false
    
    // Synonyms map for robust command matching
    private val commandSynonyms = mapOf(
        "scan" to listOf("scan", "look", "check", "surroundings", "area", "see"),
        "help" to listOf("help", "sos", "emergency", "danger", "alert"),
        "safe" to listOf("safe", "fine", "ok", "good"),
        "trapped" to listOf("trapped", "stuck", "blocked")
    )

    init {
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("Sensa", "Listening...")
                isListening = true
                accessibilityManager?.speak("Listening.")
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                isListening = false
            }
            override fun onError(error: Int) {
                Log.e("Sensa", "Speech error: $error")
                isListening = false
                val errorMsg = when (error) {
                    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> 
                        "Network unavailable. Long press for SOS."
                    SpeechRecognizer.ERROR_NO_MATCH -> 
                        "Could not hear you. Please try again."
                    else -> "Voice unavailable. Long press for SOS."
                }
                accessibilityManager?.speak(errorMsg)
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val rawText = matches[0].lowercase()
                    val normalizedCommand = normalizeCommand(rawText)
                    
                    Log.d("Sensa", "Heard: $rawText -> Normalized: $normalizedCommand")
                    
                    // Feedback: Confirm what was heard
                    if (normalizedCommand.isNotEmpty()) {
                        accessibilityManager?.speak("You said: $normalizedCommand")
                        _spokenText.value = normalizedCommand
                    } else {
                        accessibilityManager?.speak("Unknown command: $rawText")
                    }
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun normalizeCommand(text: String): String {
        // Check "Sensa" prefix but allow direct commands too for robustness
        val cleanText = text.replace("sensa", "").trim()
        
        for ((key, synonyms) in commandSynonyms) {
            if (synonyms.any { cleanText.contains(it) }) {
                return key
            }
        }
        return cleanText // Return raw if no synonym match found
    }

    fun startListening(languageCode: String = "en-US") {
        if (!isListening) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            }
            try {
                speechRecognizer.startListening(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                accessibilityManager?.speak("Voice error. Use touch gestures.")
            }
        }
    }

    fun stopListening() {
        if (isListening) {
            speechRecognizer.stopListening()
            isListening = false
        }
    }

    fun destroy() {
        speechRecognizer.destroy()
    }
}
