package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.UserPreferencesRepository
import com.example.myapplication.model.AbilityType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val libreTranslateService: com.example.myapplication.data.services.LibreTranslateService
) : ViewModel() {

    private val _uiTranslations = kotlinx.coroutines.flow.MutableStateFlow<Map<String, String>>(emptyMap())
    val uiTranslations = _uiTranslations.asStateFlow()

    fun saveUserPreferences(abilityType: AbilityType, language: String) {
        viewModelScope.launch {
            userPreferencesRepository.saveAbilityType(abilityType)
            userPreferencesRepository.saveLanguage(language)
            // Trigger translation update when language saves (or happens on selection change in UI)
        }
    }

    fun translateLabels(labels: List<String>) {
        viewModelScope.launch {
            val translations = mutableMapOf<String, String>()
            labels.forEach { label ->
                translations[label] = libreTranslateService.translate(label)
            }
            _uiTranslations.value = translations
        }
    }
}
