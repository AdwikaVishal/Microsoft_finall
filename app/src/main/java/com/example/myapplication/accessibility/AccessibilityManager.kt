package com.example.myapplication.accessibility

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * 6️⃣ Accessibility APIs
 * Manages Vibration and Text-to-Speech for the app.
 * Located in /accessibility folder.
 */
class AccessibilityManager(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context, this)
    private var isTTSReady = false
    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    // 3️⃣ Vibration patterns
    companion object {
        val PATTERN_SOS = longArrayOf(0, 500, 200, 500, 200, 500, 500, 1000, 200, 1000, 200, 1000, 500, 500, 200, 500, 200, 500) // ... --- ...
        val PATTERN_DANGER = longArrayOf(0, 500, 100, 500, 100, 500)
        val PATTERN_CONFIRM = longArrayOf(0, 100)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.getDefault())
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isTTSReady = true
            }
        }
    }

    fun speak(text: String) {
        if (isTTSReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "sensesafe_tts")
        }
    }

    fun vibrate(pattern: LongArray) {
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }
        }
    }
    
    fun stopVibration() {
        vibrator.cancel()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
