package com.example.myapplication.data

import android.content.Context
import android.content.SharedPreferences

/**
 * 2️⃣ Onboarding (Ability Profiles)
 * Stores the user's ability profile safely using SharedPreferences.
 */
class UserRepository(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("sense_safe_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ABILITY_PROFILE = "ability_profile"
        private const val KEY_USER_ID = "user_id"
    }

    // Save selected profile
    fun saveAbilityProfile(profile: AbilityProfile) {
        sharedPreferences.edit().putString(KEY_ABILITY_PROFILE, profile.name).apply()
    }

    // Retrieve profile (default to NONE)
    fun getAbilityProfile(): AbilityProfile {
        val profileName = sharedPreferences.getString(KEY_ABILITY_PROFILE, AbilityProfile.NONE.name)
        return try {
            AbilityProfile.valueOf(profileName ?: AbilityProfile.NONE.name)
        } catch (e: IllegalArgumentException) {
            AbilityProfile.NONE
        }
    }

    fun getUserId(): String {
        var userId = sharedPreferences.getString(KEY_USER_ID, null)
        if (userId == null) {
            userId = java.util.UUID.randomUUID().toString()
            sharedPreferences.edit().putString(KEY_USER_ID, userId).apply()
        }
        return userId
    }

    // Reset user data for testing purposes
    fun resetUser() {
        sharedPreferences.edit().clear().apply()
    }
}
