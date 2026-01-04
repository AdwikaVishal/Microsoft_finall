package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.Incident
import com.example.myapplication.model.IncidentRequest
import com.example.myapplication.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class IncidentViewModel(private val apiService: ApiService) : ViewModel() {

    private val _incidents = MutableStateFlow<List<Incident>>(emptyList())
    val incidents: StateFlow<List<Incident>> = _incidents

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // Report incident state
    private val _reportState = MutableStateFlow<ReportIncidentState>(ReportIncidentState.Idle)
    val reportState: StateFlow<ReportIncidentState> = _reportState

    // Form fields
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description

    private val _location = MutableStateFlow<String?>(null)
    val location: StateFlow<String?> = _location

    private val _latitude = MutableStateFlow<Double?>(null)
    val latitude: StateFlow<Double?> = _latitude

    private val _longitude = MutableStateFlow<Double?>(null)
    val longitude: StateFlow<Double?> = _longitude

    fun fetchIncidents() {
        viewModelScope.launch {
            try {
                val response = apiService.getMyIncidents()
                _incidents.value = response.incidents
                _errorMessage.value = null
                android.util.Log.d("IncidentViewModel", "Fetched ${response.incidents.size} incidents")
            } catch (e: Exception) {
                // Handle error
                _incidents.value = emptyList()
                _errorMessage.value = e.message ?: "Failed to fetch incidents"
                android.util.Log.e("IncidentViewModel", "Error fetching incidents", e)
            }
        }
    }

    fun updateCategory(category: String) {
        _selectedCategory.value = category
    }

    fun updateDescription(text: String) {
        _description.value = text
    }

    fun updateLocation(latitude: Double, longitude: Double, address: String?) {
        _latitude.value = latitude
        _longitude.value = longitude
        _location.value = address ?: formatCoordinates(latitude, longitude)
    }

    private fun formatCoordinates(lat: Double, lon: Double): String {
        return "%.6f, %.6f".format(lat, lon)
    }

    fun reportIncident() {
        val category = _selectedCategory.value
        val desc = _description.value
        val lat = _latitude.value
        val lon = _longitude.value

        if (category == null || desc.isBlank() || lat == null || lon == null) {
            _reportState.value = ReportIncidentState.Error("Please fill in all fields and get your location")
            return
        }

        viewModelScope.launch {
            _reportState.value = ReportIncidentState.Loading
            try {
                val request = IncidentRequest(
                    type = category,
                    description = desc,
                    lat = lat,
                    lng = lon,
                    image_url = null
                )
                val result = apiService.reportIncident(request)
                _reportState.value = ReportIncidentState.Success(result.id ?: "Unknown")
                // Reset form
                resetForm()
            } catch (e: Exception) {
                _reportState.value = ReportIncidentState.Error(e.message ?: "Failed to report incident")
            }
        }
    }

    fun resetForm() {
        _selectedCategory.value = null
        _description.value = ""
        _location.value = null
        _latitude.value = null
        _longitude.value = null
        _reportState.value = ReportIncidentState.Idle
    }

    fun resetReportState() {
        _reportState.value = ReportIncidentState.Idle
    }
}

sealed class ReportIncidentState {
    data object Idle : ReportIncidentState()
    data object Loading : ReportIncidentState()
    data class Success(val incidentId: String) : ReportIncidentState()
    data class Error(val message: String) : ReportIncidentState()
}
