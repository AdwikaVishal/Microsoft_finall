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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.R

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
                Text(stringResource(R.string.simulate_disaster_alert))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // New Camera Button for Real-time Exit Detection
            Button(onClick = onOpenCamera) {
                Text(stringResource(R.string.scan_surroundings))
            }

            // Voice Command Button (Visible or specialized for blind users)
            if (isBlind) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onVoiceCommand) {
                    Text(stringResource(R.string.start_voice_command))
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Reset Button
            Button(onClick = onResetApp) {
                Text(stringResource(R.string.reset_app))
            }
        }
    }
}
