package com.example.myapplication

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.UserPreferencesRepository
import com.example.myapplication.network.RetrofitClient
import com.example.myapplication.viewmodel.AlertViewModel
import com.example.myapplication.viewmodel.AuthViewModel
import com.example.myapplication.viewmodel.IncidentViewModel
import com.example.myapplication.viewmodel.OnboardingViewModel
import com.example.myapplication.viewmodel.SOSViewModel
import com.example.myapplication.viewmodel.RoboflowScanViewModel

import com.example.myapplication.data.services.LibreTranslateService

class ViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

    private val userPreferencesRepository: UserPreferencesRepository
        get() = UserPreferencesRepository(application)
    
    private val libreTranslateService: LibreTranslateService
        get() = LibreTranslateService(userPreferencesRepository)

    private val apiService
        get() = RetrofitClient.instance

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(OnboardingViewModel::class.java) ->
                OnboardingViewModel(userPreferencesRepository, libreTranslateService) as T
            modelClass.isAssignableFrom(AuthViewModel::class.java) ->
                AuthViewModel(apiService, userPreferencesRepository) as T
            modelClass.isAssignableFrom(SOSViewModel::class.java) ->
                SOSViewModel(application, apiService, userPreferencesRepository) as T
            modelClass.isAssignableFrom(AlertViewModel::class.java) ->
                AlertViewModel() as T
            modelClass.isAssignableFrom(IncidentViewModel::class.java) ->
                IncidentViewModel(apiService) as T
            modelClass.isAssignableFrom(RoboflowScanViewModel::class.java) ->
                RoboflowScanViewModel(application) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
