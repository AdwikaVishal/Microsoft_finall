package com.example.myapplication.data

/**
 * Defines the user's primary accessibility need.
 */
enum class AbilityProfile {
    BLIND,      // Vision impaired: relies on TTS + Vibration
    DEAF,       // Hearing impaired: relies on Visuals + Vibration
    NON_VERBAL, // Speech impaired: relies on One-Tap buttons
    ELDERLY,    // Simplified UI, larger text
    OTHER,      // General
    NONE        // Not yet selected
}
