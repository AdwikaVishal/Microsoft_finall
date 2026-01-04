package com.example.myapplication.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.LocationRepository
import com.example.myapplication.data.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel for Track Location feature
 * 
 * Manages:
 * - User location fetching
 * - Location state (loading, success, error)
 * - User name retrieval for marker
 * - Live location updates (optional)
 * 
 * Architecture: MVVM pattern with Repository
 */
class TrackLocationViewModel(
    application: Application,
    private val locationRepository: LocationRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : AndroidViewModel(application) {

    private val _locationState = MutableStateFlow<LocationState>(LocationState.Idle)
    val locationState: StateFlow<LocationState> = _locationState.asStateFlow()

    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName.asStateFlow()

    init {
        loadUserName()
    }

    /**
     * Load user name from preferences for marker display
     */
    private fun loadUserName() {
        viewModelScope.launch {
            try {
                // Get user name from preferences
                userPreferencesRepository.userName.collect { name ->
                    _userName.value = name ?: "You"
                    Log.d(TAG, "User name loaded: ${_userName.value}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load user name", e)
                _userName.value = "You" // Fallback
            }
        }
    }

    /**
     * Fetch current user location
     * 
     * Flow:
     * 1. Set loading state
     * 2. Try to get last known location (fast)
     * 3. If null, request fresh location update
     * 4. Update state with result or error
     */
    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation() {
        if (_locationState.value is LocationState.Loading) {
            Log.w(TAG, "Location fetch already in progress")
            return
        }

        _locationState.value = LocationState.Loading
        Log.d(TAG, "Fetching current location...")

        viewModelScope.launch {
            try {
                // Try last known location first (fast)
                val location = locationRepository.getLastKnownLocation()

                if (location != null) {
                    Log.d(TAG, "Got last known location: ${location.latitude}, ${location.longitude}")
                    _locationState.value = LocationState.Success(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracy = location.accuracy
                    )
                } else {
                    // Request fresh location
                    Log.d(TAG, "Last known location null, requesting fresh location...")
                    requestFreshLocation()
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "Location permission not granted", e)
                _locationState.value = LocationState.Error("Location permission not granted")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch location", e)
                _locationState.value = LocationState.Error(e.message ?: "Failed to fetch location")
            }
        }
    }

    /**
     * Request a fresh location update (when last known is null)
     */
    @SuppressLint("MissingPermission")
    private suspend fun requestFreshLocation() {
        try {
            val location = locationRepository.getCurrentLocation()

            if (location != null) {
                Log.d(TAG, "Got fresh location: ${location.latitude}, ${location.longitude}")
                _locationState.value = LocationState.Success(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy
                )
            } else {
                Log.e(TAG, "Fresh location request returned null")
                _locationState.value = LocationState.Error("Unable to get location. Please check GPS settings.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fresh location request failed", e)
            _locationState.value = LocationState.Error(e.message ?: "Location request failed")
        }
    }

    /**
     * Retry location fetch after error
     */
    fun retryLocationFetch() {
        Log.d(TAG, "Retrying location fetch...")
        fetchCurrentLocation()
    }

    /**
     * Reset state to idle
     */
    fun resetState() {
        _locationState.value = LocationState.Idle
    }

    companion object {
        private const val TAG = "TrackLocationViewModel"
    }
}

/**
 * Location state sealed class
 */
sealed class LocationState {
    object Idle : LocationState()
    object Loading : LocationState()
    data class Success(
        val latitude: Double,
        val longitude: Double,
        val accuracy: Float
    ) : LocationState()
    data class Error(val message: String) : LocationState()
}

