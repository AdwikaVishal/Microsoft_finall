package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.myapplication.accessibility.AccessibilityManager
import com.example.myapplication.data.UserPreferencesRepository
import com.example.myapplication.ui.AlertScreen
import com.example.myapplication.ui.MainAppNavGraph
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewmodel.AlertViewModel
import com.example.myapplication.viewmodel.IncidentViewModel
import com.example.myapplication.viewmodel.SOSViewModel

class MainActivity : ComponentActivity() {

    private val sosViewModel: SOSViewModel by viewModels { ViewModelFactory(application) }
    private val alertViewModel: AlertViewModel by viewModels { ViewModelFactory(application) }
    private val incidentViewModel: IncidentViewModel by viewModels { ViewModelFactory(application) }
    
    // Accessibility manager for vibration and TTS
    private lateinit var accessibilityManager: AccessibilityManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize accessibility manager for vibration and TTS
        accessibilityManager = AccessibilityManager(this)
        
        // Speak welcome message
        accessibilityManager.speak("Welcome to SenseSafe. Your safety app is ready.")

        // Restore auth token to RetrofitClient
        val userPreferencesRepository = UserPreferencesRepository(applicationContext)
        lifecycleScope.launch {
            userPreferencesRepository.authToken.collect { token ->
                if (token != null) {
                    com.example.myapplication.network.RetrofitClient.setAuthToken(token)
                }
            }
        }

        setContent {
            MyApplicationTheme {
                val userPreferencesRepository = UserPreferencesRepository(applicationContext)
                val userAbilityType by userPreferencesRepository.abilityType.collectAsState(initial = null)
                val alert by alertViewModel.alertState.collectAsState()

                if (alert != null && userAbilityType != null) {
                    AlertScreen(alert = alert!!, userAbilityType = userAbilityType!!)
                } else {
                    MainAppNavGraph(
                        sosViewModel = sosViewModel,
                        alertViewModel = alertViewModel,
                        incidentViewModel = incidentViewModel,
                        accessibilityManager = accessibilityManager
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        accessibilityManager.shutdown()
    }
}
