package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.LocationRepository
import com.example.myapplication.data.UserPreferencesRepository

/**
 * Factory for creating TrackLocationViewModel with dependencies
 */
class TrackLocationViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrackLocationViewModel::class.java)) {
            val locationRepository = LocationRepository(application)
            val userPreferencesRepository = UserPreferencesRepository(application)
            
            return TrackLocationViewModel(
                application,
                locationRepository,
                userPreferencesRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
