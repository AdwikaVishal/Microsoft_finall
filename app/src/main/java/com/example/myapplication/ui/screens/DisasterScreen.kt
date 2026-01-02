package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.AbilityProfile
import com.example.myapplication.data.UserStatus

/**
 * 3️⃣ & 4️⃣ Disaster Warning System & Guidance Mode
 * Adapts UI based on ability profile.
 */
@Composable
fun DisasterScreen(
    profile: AbilityProfile,
    onStatusUpdate: (UserStatus) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Red) // High contrast alert background
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // High priority header
        Text(
            text = "EMERGENCY ALERT!",
            color = Color.White,
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.semantics { contentDescription = "Emergency Alert! Danger detected." }
        )

        // 4️⃣ Guidance Mode: Adaptive UI
        when (profile) {
            AbilityProfile.BLIND -> {
                // Minimal UI, rely on TTS/Vibration (handled in Activity/ViewModel)
                Text(
                    text = "Listen for voice instructions.",
                    color = Color.White,
                    fontSize = 24.sp
                )
            }
            AbilityProfile.DEAF -> {
                // Visual cues
                Text(
                    text = "Follow Arrows -> EXIT",
                    color = Color.Yellow,
                    fontSize = 32.sp,
                    modifier = Modifier.background(Color.Black)
                )
            }
            AbilityProfile.NON_VERBAL, AbilityProfile.ELDERLY, AbilityProfile.OTHER, AbilityProfile.NONE -> {
                // Large buttons for quick status
                RescueButtons(onStatusUpdate)
            }
        }

        // Always show for everyone if possible
        if (profile == AbilityProfile.BLIND) {
             // Blind users might use volume keys or gestures, but screen buttons still accessible
             RescueButtons(onStatusUpdate)
        }
    }
}

@Composable
fun RescueButtons(onStatusUpdate: (UserStatus) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatusButton("I AM SAFE", Color.Green, UserStatus.SAFE, onStatusUpdate)
        StatusButton("I AM TRAPPED", Color.Black, UserStatus.TRAPPED, onStatusUpdate)
        StatusButton("I NEED HELP", Color.Blue, UserStatus.NEED_HELP, onStatusUpdate)
    }
}

@Composable
fun StatusButton(
    text: String,
    bgColor: Color,
    status: UserStatus,
    onClick: (UserStatus) -> Unit
) {
    Button(
        onClick = { onClick(status) },
        colors = ButtonDefaults.buttonColors(containerColor = bgColor),
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .semantics { contentDescription = "Send status: $text" }
    ) {
        Text(text = text, fontSize = 24.sp, color = Color.White)
    }
}
