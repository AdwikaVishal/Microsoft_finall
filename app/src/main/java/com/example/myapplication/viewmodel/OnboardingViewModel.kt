package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.UserPreferencesRepository
import com.example.myapplication.model.AbilityType
import kotlinx.coroutines.launch

class OnboardingViewModel(private val userPreferencesRepository: UserPreferencesRepository) : ViewModel() {

    fun saveUserPreferences(abilityType: AbilityType, language: String) {
        viewModelScope.launch {
            userPreferencesRepository.saveAbilityType(abilityType)
            userPreferencesRepository.saveLanguage(language)
        }
    }
}
