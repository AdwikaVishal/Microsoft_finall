package com.example.myapplication.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.ViewModelFactory
import com.example.myapplication.data.AbilityProfile
import com.example.myapplication.model.AbilityType
import com.example.myapplication.model.Alert
import com.example.myapplication.model.SOSStatus
import com.example.myapplication.ui.screens.CameraScreen
import com.example.myapplication.ui.screens.VoiceCommandScreen
import com.example.myapplication.ui.screens.LanguageSettingsScreen
import com.example.myapplication.viewmodel.AlertViewModel
import com.example.myapplication.viewmodel.IncidentViewModel
import com.example.myapplication.viewmodel.SOSViewModel
import com.example.myapplication.viewmodel.ScanViewModel
import com.example.myapplication.viewmodel.VoiceViewModel
import com.example.myapplication.accessibility.AccessibilityManager
import kotlinx.coroutines.launch

/**
 * Main App Navigation Graph
 * 
 * Handles all navigation within the application including:
 * - Main screen (Home)
 * - Voice command screen
 * - Camera/Scan screen with Azure Vision integration
 * - Incident reporting
 * - Timeline view
 * - Alert display
 * 
 * File Location: app/src/main/java/com/example/myapplication/ui/MainAppNavGraph.kt
 */
@Composable
fun MainAppNavGraph(
    sosViewModel: SOSViewModel,
    alertViewModel: AlertViewModel,
    incidentViewModel: IncidentViewModel,
    scanViewModel: ScanViewModel,
    accessibilityManager: AccessibilityManager? = null
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    NavHost(navController = navController, startDestination = "main") {
        // ============================================================
        // MAIN SCREEN (Home)
        // ============================================================
        composable("main") { backStackEntry ->
            // ✅ FIX: Retrieve scan result from savedStateHandle
            val scanResult = backStackEntry.savedStateHandle.get<String>("scanResult")
            
            // Clear the result after reading
            LaunchedEffect(scanResult) {
                if (scanResult != null) {
                    backStackEntry.savedStateHandle.remove<String>("scanResult")
                }
            }
            
            MainScreen(
                sosViewModel = sosViewModel,
                alertViewModel = alertViewModel,
                onNavigateToTimeline = { navController.navigate("timeline") },
                onNavigateToCamera = { navController.navigate("camera") },
                onNavigateToVoiceCommand = { navController.navigate("voiceCommand") },
                onNavigateToReportIncident = { navController.navigate("reportIncident") },
                onNavigateToTrackLocation = { navController.navigate("trackLocation") },
                onNavigateToLanguageSettings = { navController.navigate("languageSettings") },
                accessibilityManager = accessibilityManager,
                initialScanResult = scanResult
            )
        }
        
        // ============================================================
        // TRACK LOCATION SCREEN
        // ============================================================
        composable("trackLocation") {
            com.example.myapplication.ui.screens.TrackLocationScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ============================================================
        // VOICE COMMAND SCREEN
        // ============================================================
        composable("voiceCommand") {
            VoiceCommandScreen(
                accessibilityManager = accessibilityManager,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToScan = {
                    navController.navigate("camera") {
                        popUpTo("voiceCommand") { inclusive = true }
                    }
                },
                onTriggerSOS = { status ->
                    // Navigate back to main and trigger SOS
                    scope.launch {
                        navController.popBackStack()
                        sosViewModel.sendSOS(status)
                    }
                },
                onShowAlerts = {
                    // Navigate to alerts screen
                    scope.launch {
                        navController.navigate("alert")
                    }
                },
                onNavigateToHome = {
                    navController.navigate("main") {
                        popUpTo("main") { inclusive = true }
                    }
                },
                onReportIncident = { description ->
                    // Navigate to report incident screen with pre-filled description
                    scope.launch {
                        navController.previousBackStackEntry?.savedStateHandle?.set("voiceIncidentDescription", description)
                        navController.navigate("reportIncident") {
                            popUpTo("voiceCommand") { inclusive = true }
                        }
                    }
                }
            )
        }

        // ============================================================
        // TIMELINE SCREEN
        // ============================================================
        composable("timeline") {
            MyIncidentTimelineScreen(
                viewModel = incidentViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ============================================================
        // CAMERA/SCAN SCREEN
        // ============================================================
        composable(
            route = "camera?profile={profile}",
            arguments = listOf(
                navArgument("profile") {
                    type = NavType.StringType
                    defaultValue = AbilityProfile.NONE.name
                }
            )
        ) { backStackEntry ->
            val profileName: String = backStackEntry.arguments?.getString("profile") ?: AbilityProfile.NONE.name
            val profile: AbilityProfile = try {
                AbilityProfile.valueOf(profileName)
            } catch (e: IllegalArgumentException) {
                AbilityProfile.NONE
            }
            CameraScreen(
                profile = profile,
                onExitDetected = { result ->
                    // Navigate back with the detected result
                    navController.previousBackStackEntry?.savedStateHandle?.set("scanResult", result)
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ============================================================
        // REPORT INCIDENT SCREEN
        // ============================================================
        composable("reportIncident") {
            ReportIncidentScreen(
                viewModel = incidentViewModel,
                onNavigateBack = { navController.popBackStack() },
                accessibilityManager = accessibilityManager
            )
        }

        // ============================================================
        // ALERT SCREEN (for showing active alerts)
        // ============================================================
        composable("alert") {
            // Create a placeholder alert for demonstration
            // In production, this should come from alertViewModel
            AlertScreen(
                alert = Alert(
                    title = "Active Alert",
                    message = "There is an active alert in your area. Please stay safe."
                ),
                userAbilityType = AbilityType.NONE
            )
        }

        // ============================================================
        // GUIDANCE SCREEN
        // ============================================================
        composable("guidance") {
            GuidanceScreen()
        }

        // ============================================================
        // SOS SCREEN (legacy)
        // ============================================================
        composable("sos") {
            SOSScreen()
        }

        // ============================================================
        // STATUS SCREEN (legacy)
        // ============================================================
        composable("status") {
            StatusScreen()
        }
        
        // ============================================================
        // LANGUAGE SETTINGS SCREEN
        // ============================================================
        composable("languageSettings") {
            LanguageSettingsScreen(
                onLanguageSelected = { languageCode ->
                    // Language change will trigger app restart
                    // No additional action needed here
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}

