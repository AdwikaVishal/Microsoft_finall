package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onSimulateAlert: () -> Unit,
    onResetApp: () -> Unit,
    onOpenCamera: () -> Unit,
    isBlind: Boolean,
    onVoiceCommand: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = onSimulateAlert) {
                Text("SIMULATE DISASTER ALERT")
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // New Camera Button for Real-time Exit Detection
            Button(onClick = onOpenCamera) {
                Text("SCAN SURROUNDINGS (EXIT)")
            }

            // Voice Command Button (Visible or specialized for blind users)
            if (isBlind) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onVoiceCommand) {
                    Text("START VOICE COMMAND")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Reset Button
            Button(onClick = onResetApp) {
                Text("RESET APP (CLEAR USER DATA)")
            }
        }
    }
}
