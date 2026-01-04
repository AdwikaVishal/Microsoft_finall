package com.example.myapplication.viewmodel

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.BackoffPolicy
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.example.myapplication.alerts.AlertManager
import com.example.myapplication.data.AbilityProfile
import com.example.myapplication.data.DisasterPayload
import com.example.myapplication.data.RetrofitClient
import com.example.myapplication.data.UserRepository
import com.example.myapplication.data.UserStatus
import com.example.myapplication.model.AbilityType
import com.example.myapplication.model.SOS
import com.example.myapplication.model.SOSStatus
import com.example.myapplication.utils.DeviceUtils
import com.example.myapplication.utils.LocationHelper
import com.example.myapplication.utils.OfflineManager
import com.example.myapplication.workers.RescueWorker
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.concurrent.TimeUnit

// 🔁 Mapper: AbilityProfile -> AbilityType
private fun AbilityProfile.toAbilityType(): AbilityType =
    when (this) {
        AbilityProfile.BLIND -> AbilityType.BLIND
        AbilityProfile.DEAF -> AbilityType.DEAF
        AbilityProfile.NON_VERBAL -> AbilityType.NON_VERBAL
        AbilityProfile.ELDERLY -> AbilityType.ELDERLY
        AbilityProfile.OTHER -> AbilityType.OTHER
        AbilityProfile.NONE -> AbilityType.NONE
    }

class SafetyViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository(application)
    private val locationHelper = LocationHelper(application)

    private val _abilityProfile = MutableStateFlow(userRepository.getAbilityProfile())
    val abilityProfile: StateFlow<AbilityProfile> = _abilityProfile

    private val _isDisasterActive = MutableStateFlow(false)
    val isDisasterActive: StateFlow<Boolean> = _isDisasterActive

    val lastKnownLocation = MutableStateFlow<Location?>(null)

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline

    fun setAbilityProfile(profile: AbilityProfile) {
        userRepository.saveAbilityProfile(profile)
        _abilityProfile.value = profile
    }

    fun triggerFakeAlert() {
        _isDisasterActive.value = true
        AlertManager.showDisasterAlert(getApplication())
    }

    fun sendRescueSignal(status: UserStatus) {
        viewModelScope.launch {
            val location = locationHelper.getCurrentLocation()
            lastKnownLocation.value = location
            val battery = DeviceUtils.getBatteryLevel(getApplication())

            val payload = DisasterPayload(
                sos = SOS(
                    userId = userRepository.getUserId(),
                    latitude = location?.latitude ?: 0.0,
                    longitude = location?.longitude ?: 0.0,
                    timestamp = Date(),
                    status = SOSStatus.valueOf(status.name),
                    batteryPercentage = battery,
                    abilityType = _abilityProfile.value.toAbilityType()   // ✅ FIXED
                )
            )

            val isOnline = OfflineManager.isOnline(getApplication())
            _isOffline.value = !isOnline

            if (isOnline) {
                launch {
                    try {
                        val response = RetrofitClient.instance.sendAlert(payload)
                        if (!response.isSuccessful) enqueueRescueWorker(payload)
                    } catch (e: Exception) {
                        enqueueRescueWorker(payload)
                    }
                }
            } else {
                enqueueRescueWorker(payload)
            }
        }
    }

    fun resetApp() {
        userRepository.resetUser()
        _abilityProfile.value = AbilityProfile.NONE
        _isDisasterActive.value = false
        AlertManager.cancelAlert(getApplication())
    }

    private fun enqueueRescueWorker(payload: DisasterPayload) {
        val payloadJson = Gson().toJson(payload)
        val data = Data.Builder()
            .putString(RescueWorker.KEY_PAYLOAD, payloadJson)
            .build()

        val request = OneTimeWorkRequestBuilder<RescueWorker>()
            .setInputData(data)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(getApplication()).enqueue(request)
    }
}
