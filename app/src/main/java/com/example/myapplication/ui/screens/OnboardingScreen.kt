package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.data.AbilityProfile

/**
 * 2️⃣ Onboarding (Ability Profiles)
 * Screen to select: Blind, Deaf, Non‑verbal, Elderly, Other.
 * Stores safely using SharedPreferences (via ViewModel).
 */
@Composable
fun OnboardingScreen(
    currentProfile: AbilityProfile,
    onProfileSelected: (AbilityProfile) -> Unit,
    onContinue: () -> Unit
) {
    val continueContentDescription = stringResource(R.string.continue_to_main_app)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.select_your_profile),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        AbilityProfile.values().forEach { profile ->
            if (profile != AbilityProfile.NONE) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .selectable(
                            selected = (profile == currentProfile),
                            onClick = { onProfileSelected(profile) }
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (profile == currentProfile),
                        onClick = null // null recommended for accessibility with selectable
                    )
                    Text(
                        text = profile.name.replace("_", " "), // Format name nicely
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .semantics { contentDescription = continueContentDescription }
        ) {
            Text(stringResource(R.string.continue_button))
        }
    }
}
