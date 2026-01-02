package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.UserPreferencesRepository
import com.example.myapplication.network.ApiService
import com.example.myapplication.network.LoginRequest
import com.example.myapplication.network.RegisterRequest
import com.example.myapplication.network.RetrofitClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val apiService: ApiService, private val userPreferencesRepository: UserPreferencesRepository) : ViewModel() {

    private val _authenticationState = MutableStateFlow<AuthenticationState>(AuthenticationState.Idle)
    val authenticationState: StateFlow<AuthenticationState> = _authenticationState

    // Track if using demo mode
    private var isDemoMode = false

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authenticationState.value = AuthenticationState.Loading
            
            // Check for demo credentials
            if (email == RetrofitClient.DEMO_EMAIL && pass == RetrofitClient.DEMO_PASSWORD) {
                isDemoMode = true
                delay(1000) // Simulate network delay
                val token = RetrofitClient.DEMO_TOKEN
                userPreferencesRepository.saveAuthToken(token)
                RetrofitClient.setAuthToken(token)
                _authenticationState.value = AuthenticationState.Success
                return@launch
            }
            
            try {
                val response = apiService.login(LoginRequest(email, pass))
                userPreferencesRepository.saveAuthToken(response.token)
                RetrofitClient.setAuthToken(response.token)
                _authenticationState.value = AuthenticationState.Success
            } catch (e: Exception) {
                _authenticationState.value = AuthenticationState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _authenticationState.value = AuthenticationState.Loading
            try {
                val response = apiService.register(RegisterRequest(name, email, password))
                userPreferencesRepository.saveAuthToken(response.token)
                RetrofitClient.setAuthToken(response.token)
                _authenticationState.value = AuthenticationState.Success
            } catch (e: Exception) {
                _authenticationState.value = AuthenticationState.Error(e.message ?: "An error occurred")
            }
        }
    }
    
    fun isDemoModeEnabled(): Boolean = isDemoMode
}

sealed class AuthenticationState {
    object Idle : AuthenticationState()
    object Loading : AuthenticationState()
    object Success : AuthenticationState()
    data class Error(val message: String) : AuthenticationState()
}
