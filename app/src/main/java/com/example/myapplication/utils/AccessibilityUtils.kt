package com.example.myapplication.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import java.util.Locale

/**
 * 6️⃣ Accessibility APIs
 * Utilities for Vibration and Text-to-Speech.
 */
object AccessibilityUtils {

    // 3️⃣ Vibration patterns based on danger level
    fun vibrate(context: Context, pattern: LongArray) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Amplitude -1 means default strength
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }
        }
    }

    // Vibration Patterns (Wait, Vibrate, Wait, Vibrate...)
    val PATTERN_DANGER = longArrayOf(0, 500, 100, 500, 100, 500) // SOS-like
    val PATTERN_WARNING = longArrayOf(0, 300, 200, 300)
    val PATTERN_CONFIRM = longArrayOf(0, 100)

    // TextToSpeech Helper
    class TTSHelper(context: Context) : TextToSpeech.OnInitListener {
        private var tts: TextToSpeech? = TextToSpeech(context, this)
        private var isReady = false

        override fun onInit(status: Int) {
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.getDefault())
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Handle language missing
                } else {
                    isReady = true
                }
            }
        }

        fun speak(text: String) {
            if (isReady) {
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "sense_safe_utterance")
            }
        }

        fun shutdown() {
            tts?.stop()
            tts?.shutdown()
        }
    }
}
