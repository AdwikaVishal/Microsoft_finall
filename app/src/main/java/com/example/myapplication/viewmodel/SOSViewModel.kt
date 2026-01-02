package com.example.myapplication.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.UserPreferencesRepository
import com.example.myapplication.model.SOS
import com.example.myapplication.model.SOSStatus
import com.example.myapplication.network.ApiService
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date

class SOSViewModel(application: Application, private val apiService: ApiService, private val userPreferencesRepository: UserPreferencesRepository) : AndroidViewModel(application) {

    private val _sosState = MutableStateFlow<SOSState>(SOSState.Idle)
    val sosState: StateFlow<SOSState> = _sosState

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    @SuppressLint("MissingPermission")
    fun sendSOS(status: SOSStatus) {
        _sosState.value = SOSState.Loading

        if (ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            _sosState.value = SOSState.Error("Location permission not granted")
            return
        }

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation ?: return
                viewModelScope.launch {
                    try {
                        val userId = userPreferencesRepository.authToken.first() ?: ""
                        val abilityType = userPreferencesRepository.abilityType.first()
                        // TODO: Get actual battery percentage
                        val batteryPercentage = 80
                        val sos = SOS(userId, location.latitude, location.longitude, Date(), status, batteryPercentage, abilityType)
                        val response = apiService.sendSOS(sos)
                        _sosState.value = SOSState.Success(response.sosId)
                    } catch (e: Exception) {
                        _sosState.value = SOSState.Error(e.message ?: "Failed to send SOS")
                    }
                }
                fusedLocationClient.removeLocationUpdates(this)
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }
}

sealed class SOSState {
    object Idle : SOSState()
    object Loading : SOSState()
    data class Success(val sosId: String) : SOSState()
    data class Error(val message: String) : SOSState()
}
