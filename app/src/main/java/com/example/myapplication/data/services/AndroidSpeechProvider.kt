package com.example.myapplication.data.services

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Android Native SpeechRecognizer Provider
 * 
 * Uses Android's built-in speech recognition (Google Voice Services)
 * 
 * Advantages:
 * - ✅ FREE - No API key required
 * - ✅ Works offline (if language pack installed)
 * - ✅ Fast and accurate
 * - ✅ No network required
 * - ✅ Integrated with device
 * 
 * Note: This doesn't use the audio ByteArray parameter since Android's
 * SpeechRecognizer handles audio capture internally.
 */
class AndroidSpeechProvider(private val context: Context) : SpeechProvider {

    companion object {
        private const val TAG = "AndroidSpeechProvider"
    }

    /**
     * Transcribe speech using Android's native SpeechRecognizer
     * 
     * IMPORTANT: The audio parameter is NOT USED by this provider!
     * Android's SpeechRecognizer captures audio directly from the microphone.
     * The audio ByteArray is ignored - pass empty array or any value.
     * 
     * @param audio IGNORED - Android handles audio capture internally
     * @param targetLang Language code (e.g., "en-US", "es-ES")
     * @return Transcribed text or empty string if no speech detected
     */
    override suspend fun transcribeAndTranslate(audio: ByteArray, targetLang: String): String = withContext(Dispatchers.Main) {
        // Check if speech recognition is available
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            throw IOException("Speech recognition not available on this device")
        }

        suspendCancellableCoroutine { continuation ->
            val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            
            val recognitionListener = object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    Log.d(TAG, "Ready for speech")
                }

                override fun onBeginningOfSpeech() {
                    Log.d(TAG, "Speech started")
                }

                override fun onRmsChanged(rmsdB: Float) {
                    // Audio level changed
                }

                override fun onBufferReceived(buffer: ByteArray?) {
                    // Partial audio buffer
                }

                override fun onEndOfSpeech() {
                    Log.d(TAG, "Speech ended")
                }

                override fun onError(error: Int) {
                    val errorMessage = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                        SpeechRecognizer.ERROR_NETWORK -> "Network error"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                        SpeechRecognizer.ERROR_NO_MATCH -> {
                            // This is common - user didn't speak clearly or paused too long
                            Log.i(TAG, "No speech match - user may need to speak more clearly or closer to mic")
                            "No speech detected"
                        }
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                        SpeechRecognizer.ERROR_SERVER -> "Server error"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                            Log.i(TAG, "Speech timeout - no input detected")
                            "No speech detected"
                        }
                        else -> "Unknown error: $error"
                    }
                    
                    Log.d(TAG, "Recognition error: $errorMessage (code: $error)")
                    speechRecognizer.destroy()
                    
                    if (continuation.isActive) {
                        // For "no match" and timeout errors, return empty string
                        // This allows graceful handling without triggering fallback providers
                        if (error == SpeechRecognizer.ERROR_NO_MATCH || 
                            error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                            Log.d(TAG, "Returning empty string for no-speech scenario")
                            continuation.resume("")
                        } else {
                            // For actual errors, throw exception to try fallback providers
                            continuation.resumeWithException(IOException(errorMessage))
                        }
                    }
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val transcript = matches?.firstOrNull() ?: ""
                    
                    Log.d(TAG, "Recognition result: $transcript")
                    speechRecognizer.destroy()
                    
                    if (continuation.isActive) {
                        continuation.resume(transcript)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    // Partial results available (for real-time display)
                }

                override fun onEvent(eventType: Int, params: Bundle?) {
                    // Reserved for future events
                }
            }

            speechRecognizer.setRecognitionListener(recognitionListener)

            // Create recognition intent
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, targetLang)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                // Increase speech timeout to give user more time to speak
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 3000L)
            }

            // Start listening
            try {
                speechRecognizer.startListening(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start listening", e)
                speechRecognizer.destroy()
                if (continuation.isActive) {
                    continuation.resumeWithException(IOException("Failed to start speech recognition: ${e.message}"))
                }
            }

            // Handle cancellation
            continuation.invokeOnCancellation {
                Log.d(TAG, "Recognition cancelled")
                speechRecognizer.cancel()
                speechRecognizer.destroy()
            }
        }
    }
}
