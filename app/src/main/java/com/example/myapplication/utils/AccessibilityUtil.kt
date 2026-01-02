package com.example.myapplication.utils

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.tts.TextToSpeech
import com.example.myapplication.model.AbilityType
import java.util.*

class AccessibilityUtil(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(context, this)
    }

    fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun vibrate(pattern: LongArray) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
    }

    fun handleAccessibility(abilityType: AbilityType, title: String, message: String) {
        when (abilityType) {
            AbilityType.BLIND, AbilityType.LOW_VISION -> {
                speak("$title. $message")
            }
            AbilityType.DEAF, AbilityType.HARD_OF_HEARING -> {
                vibrate(longArrayOf(0, 500, 200, 500)) // Example pattern
            }
            else -> {
                // For other users, a subtle vibration might be appropriate
                vibrate(longArrayOf(0, 100))
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
