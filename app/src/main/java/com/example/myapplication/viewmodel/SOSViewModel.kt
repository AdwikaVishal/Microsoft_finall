package com.example.myapplication.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.UserPreferencesRepository
import com.example.myapplication.model.AbilityType
import com.example.myapplication.model.SOS
import com.example.myapplication.model.SOSRequest
import com.example.myapplication.model.SOSStatus
import com.example.myapplication.network.ApiService
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date

/**
 * SOSViewModel - Handles SOS alert creation and sending
 * 
 * Key fixes applied:
 * 1. Idempotency guard (isSendingSOS flag) to prevent duplicate API calls
 * 2. JSON logging of outgoing requests for debugging
 * 3. Immediate location callback cleanup after first successful location fix
 * 4. Proper state management to prevent multiple concurrent requests
 */
class SOSViewModel(application: Application, private val apiService: ApiService, private val userPreferencesRepository: UserPreferencesRepository) : AndroidViewModel(application) {

    private val _sosState = MutableStateFlow<SOSState>(SOSState.Idle)
    val sosState: StateFlow<SOSState> = _sosState

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
    
    // Idempotency guard - prevents duplicate SOS requests
    private var isSendingSOS = false
    
    // Gson instance for JSON logging
    private val gson = Gson()

    /**
     * Send SOS alert with current location and status.
     * 
     * This method is now idempotent - calling it multiple times will only
     * send one SOS request due to the isSendingSOS guard flag.
     * 
     * @param status The SOS status (TRAPPED, INJURED, NEED_HELP, SAFE)
     */
    @SuppressLint("MissingPermission")
    fun sendSOS(status: SOSStatus) {
        // IDEMPOTENCY GUARD: Prevent duplicate calls
        if (isSendingSOS) {
            Log.w("SOSViewModel", "SOS already in progress, ignoring duplicate call")
            return
        }
        
        _sosState.value = SOSState.Loading
        isSendingSOS = true
        
        Log.d("SOSViewModel", "Starting SOS request with status: ${status.name}")

        if (ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("SOSViewModel", "Location permission not granted")
            _sosState.value = SOSState.Error("Location permission not granted")
            isSendingSOS = false
            return
        }

        val locationRequest = LocationRequest.Builder(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, 5000L).apply {
            // Use the new API - setWaitForAccurateLocation is not available in API 21.0.1
            // Use fastest interval as the min update interval
            setMinUpdateIntervalMillis(500)
            // Note: setMaxUpdates() is not available in the new API
            // We handle max updates manually in the callback by removing updates after first location
        }.build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                // CLEANUP: Immediately remove updates to prevent multiple calls
                fusedLocationClient.removeLocationUpdates(this)
                
                val location = locationResult.lastLocation
                if (location == null) {
                    Log.e("SOSViewModel", "Location result was null")
                    _sosState.value = SOSState.Error("Could not get current location")
                    isSendingSOS = false
                    return
                }
                
                Log.d("SOSViewModel", "Got location: ${location.latitude}, ${location.longitude}")
                
                viewModelScope.launch {
                    try {
                        val userId = userPreferencesRepository.authToken.first() ?: ""
                        val abilityType = userPreferencesRepository.abilityType.first() ?: AbilityType.NONE
                        // TODO: Get actual battery percentage
                        val batteryPercentage = 80

                        // Create the internal SOS object (for logging/debugging)
                        val sos = SOS(userId, location.latitude, location.longitude, Date(), status, batteryPercentage, abilityType)

                        // Convert to SOSRequest for API call (backend format)
                        val sosRequest = SOSRequest(
                            ability = abilityType.name,  // Enum to string (e.g., "LOW_VISION")
                            lat = location.latitude,
                            lng = location.longitude,
                            battery = batteryPercentage,
                            status = status.name  // Enum to string (e.g., "NEED_HELP")
                        )

                        // JSON LOGGING: Log the outgoing request payload
                        val jsonPayload = gson.toJson(sosRequest)
                        Log.d("SOS_REQUEST", "=== OUTGOING SOS REQUEST ===")
                        Log.d("SOS_REQUEST", "JSON Payload: $jsonPayload")
                        Log.d("SOS_REQUEST", "ability: ${sosRequest.ability}")
                        Log.d("SOS_REQUEST", "lat: ${sosRequest.lat}")
                        Log.d("SOS_REQUEST", "lng: ${sosRequest.lng}")
                        Log.d("SOS_REQUEST", "battery: ${sosRequest.battery}")
                        Log.d("SOS_REQUEST", "status: ${sosRequest.status}")
                        Log.d("SOS_REQUEST", "============================")

                        val response = apiService.sendSOS(sosRequest)
                        
                        Log.d("SOSViewModel", "SOS sent successfully! ID: ${response.sosId}")
                        _sosState.value = SOSState.Success(response.sosId)
                    } catch (e: Exception) {
                        Log.e("SOSViewModel", "Failed to send SOS", e)
                        _sosState.value = SOSState.Error(e.message ?: "Failed to send SOS")
                    } finally {
                        // RESET IDEMPOTENCY GUARD after completion (success or failure)
                        isSendingSOS = false
                    }
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        
        // Safety timeout - reset guard if no location received within 30 seconds
        viewModelScope.launch {
            kotlinx.coroutines.delay(30000)
            if (isSendingSOS) {
                Log.w("SOSViewModel", "Safety timeout reached, resetting SOS guard")
                isSendingSOS = false
                if (_sosState.value is SOSState.Loading) {
                    _sosState.value = SOSState.Error("Location request timed out")
                }
            }
        }
    }
    
    /**
     * Reset the SOS state to Idle.
     * Call this after showing the success/error dialog to allow new SOS requests.
     */
    fun resetState() {
        _sosState.value = SOSState.Idle
    }
}

sealed class SOSState {
    object Idle : SOSState()
    object Loading : SOSState()
    data class Success(val sosId: String) : SOSState()
    data class Error(val message: String) : SOSState()
}

