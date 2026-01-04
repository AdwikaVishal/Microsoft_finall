package com.example.myapplication.ui

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.MainActivity
import com.example.myapplication.ViewModelFactory
import com.example.myapplication.data.AbilityProfile
import com.example.myapplication.model.AbilityType
import com.example.myapplication.model.Alert
import com.example.myapplication.ui.screens.CameraScreen
import com.example.myapplication.ui.screens.VoiceCommandScreen
import com.example.myapplication.ui.screens.LanguageSettingsScreen
import com.example.myapplication.model.SOSStatus
import com.example.myapplication.viewmodel.AlertViewModel
import com.example.myapplication.viewmodel.IncidentViewModel
import com.example.myapplication.viewmodel.SOSViewModel
import com.example.myapplication.accessibility.AccessibilityManager

@Composable
fun NavGraph(
    startDestination: String,
    factory: ViewModelProvider.Factory,
    accessibilityManager: AccessibilityManager? = null
) {
    val context = LocalContext.current
    val navFactory = factory
    val sosViewModel: SOSViewModel = viewModel(factory = navFactory)
    val alertViewModel: AlertViewModel = viewModel(factory = navFactory)
    val incidentViewModel: IncidentViewModel = viewModel(factory = navFactory)
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = startDestination) {
        composable("onboarding") {
            OnboardingScreen(
                onOnboardingComplete = {
                    // Navigate directly to MainActivity after onboarding
                    val intent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    context.startActivity(intent)
                }
            )
        }
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
                onNavigateToReportIncident = { navController.navigate("report_incident") },
                onNavigateToTrackLocation = { navController.navigate("trackLocation") },
                onNavigateToLanguageSettings = { navController.navigate("languageSettings") },
                accessibilityManager = accessibilityManager,
                initialScanResult = scanResult
            ) 
        }
        composable("alert") { AlertScreen(Alert("", ""), AbilityType.NONE) }
        composable("guidance") { GuidanceScreen() }
        composable("sos") { SOSScreen() }
        composable("status") { StatusScreen() }
        composable("report_incident") { 
            ReportIncidentScreen(
                viewModel = incidentViewModel,
                onNavigateBack = { navController.popBackStack() },
                accessibilityManager = accessibilityManager
            ) 
        }
        composable("timeline") { 
            MyIncidentTimelineScreen(
                viewModel = incidentViewModel,
                onNavigateBack = { navController.popBackStack() }
            ) 
        }
        composable("trackLocation") {
            com.example.myapplication.ui.screens.TrackLocationScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "camera?profile={profile}",
            arguments = listOf(
                navArgument("profile") {
                    type = NavType.StringType
                    defaultValue = AbilityProfile.NONE.name
                }
            )
        ) { backStackEntry ->
            val profileName = backStackEntry.arguments?.getString("profile") ?: AbilityProfile.NONE.name
            val profile = try {
                AbilityProfile.valueOf(profileName)
            } catch (e: IllegalArgumentException) {
                AbilityProfile.NONE
            }
            CameraScreen(
                profile = profile,
                onExitDetected = { result ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("scanResult", result)
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("voiceCommand") {
            VoiceCommandScreen(
                accessibilityManager = accessibilityManager,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToScan = { navController.navigate("camera") },
                onTriggerSOS = { status ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("sosStatus", status.name)
                    navController.navigate("sos")
                },
                onShowAlerts = { navController.navigate("alert") },
                onNavigateToHome = { navController.navigate("main") },
                onReportIncident = { description ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("voiceIncidentDescription", description)
                    navController.navigate("report_incident")
                }
            )
        }
        
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
