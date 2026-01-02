package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import com.example.myapplication.model.Alert
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AlertViewModel : ViewModel() {

    private val _alertState = MutableStateFlow<Alert?>(null)
    val alertState: StateFlow<Alert?> = _alertState

    fun showTestAlert() {
        _alertState.value = Alert("Earthquake Detected", "Move away from windows.")
    }

    fun dismissAlert() {
        _alertState.value = null
    }

    fun hasActiveAlert(): Boolean {
        return _alertState.value != null
    }
}
