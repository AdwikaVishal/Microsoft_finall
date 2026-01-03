package com.example.myapplication.data.services

/**
 * Interface for speech-to-text and translation providers.
 */
interface SpeechProvider {
    /**
     * Transcribes audio and translates it to the target language.
     * @param audio The audio data in byte array format.
     * @param targetLang The target language code (e.g., "en", "es").
     * @return The transcribed and translated text.
     */
    suspend fun transcribeAndTranslate(audio: ByteArray, targetLang: String): String
}
