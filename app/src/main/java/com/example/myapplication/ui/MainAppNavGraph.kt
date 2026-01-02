package com.example.myapplication.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.accessibility.AccessibilityManager
import com.example.myapplication.data.AbilityProfile
import com.example.myapplication.ui.screens.CameraScreen
import com.example.myapplication.ui.screens.VoiceCommandScreen
import com.example.myapplication.viewmodel.AlertViewModel
import com.example.myapplication.viewmodel.IncidentViewModel
import com.example.myapplication.viewmodel.SOSViewModel

@Composable
fun MainAppNavGraph(
    sosViewModel: SOSViewModel,
    alertViewModel: AlertViewModel,
    incidentViewModel: IncidentViewModel,
    accessibilityManager: AccessibilityManager? = null
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(
                sosViewModel = sosViewModel,
                alertViewModel = alertViewModel,
                onNavigateToTimeline = { navController.navigate("timeline") },
                onNavigateToCamera = { navController.navigate("camera") },
                onNavigateToVoiceCommand = { navController.navigate("voiceCommand") },
                onNavigateToReportIncident = { navController.navigate("reportIncident") },
                accessibilityManager = accessibilityManager
            )
        }
        composable("timeline") {
            MyIncidentTimelineScreen(
                viewModel = incidentViewModel,
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
                    // Navigate back with the detected result
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
                onProcessWithAzure = { command ->
                    // Store the processed command and navigate back
                    navController.previousBackStackEntry?.savedStateHandle?.set("voiceCommandResult", command)
                    navController.popBackStack()
                }
            )
        }
        composable("reportIncident") {
            ReportIncidentScreen(
                viewModel = incidentViewModel,
                onNavigateBack = { navController.popBackStack() },
                accessibilityManager = accessibilityManager
            )
        }
    }
}

