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

class ViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

    private val userPreferencesRepository: UserPreferencesRepository
        get() = UserPreferencesRepository(application)

    private val apiService
        get() = RetrofitClient.instance

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(OnboardingViewModel::class.java) ->
                OnboardingViewModel(userPreferencesRepository) as T
            modelClass.isAssignableFrom(AuthViewModel::class.java) ->
                AuthViewModel(apiService, userPreferencesRepository) as T
            modelClass.isAssignableFrom(SOSViewModel::class.java) ->
                SOSViewModel(application, apiService, userPreferencesRepository) as T
            modelClass.isAssignableFrom(AlertViewModel::class.java) ->
                AlertViewModel() as T
            modelClass.isAssignableFrom(IncidentViewModel::class.java) ->
                IncidentViewModel(apiService) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
