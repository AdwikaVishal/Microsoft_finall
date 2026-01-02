package com.example.myapplication.ui

import android.app.Application
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ViewModelFactory
import com.example.myapplication.accessibility.AccessibilityManager
import com.example.myapplication.model.AbilityType
import com.example.myapplication.viewmodel.OnboardingViewModel

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = viewModel(
        factory = ViewModelFactory(LocalContext.current.applicationContext as Application)
    ),
    onOnboardingComplete: () -> Unit,
    accessibilityManager: AccessibilityManager? = null
) {
    var selectedAbilityType by remember { mutableStateOf(AbilityType.NORMAL) }
    var selectedLanguage by remember { mutableStateOf("English") }
    var currentStep by remember { mutableStateOf(0) }

    // Speak welcome message when onboarding starts
    LaunchedEffect(Unit) {
        accessibilityManager?.speak("Welcome to SenseSafe. Let\'s set up your accessibility preferences.")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress Indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(2) { step ->
                Box(
                    modifier = Modifier
                        .size(if (step == currentStep) 12.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (step <= currentStep) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                )
                if (step < 1) Spacer(modifier = Modifier.width(8.dp))
            }
        }

        // Header Icon
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (currentStep) {
                    0 -> Icons.Default.Accessibility
                    else -> Icons.Default.Language
                },
                contentDescription = null,
                modifier = Modifier.size(50.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Title
        Text(
            text = when (currentStep) {
                0 -> "Choose Your Ability Profile"
                else -> "Select Your Language"
            },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        Text(
            text = when (currentStep) {
                0 -> "This helps us customize alerts and guidance for your needs"
                else -> "We\'ll use this for voice announcements"
            },
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (currentStep == 0) {
            // Ability Type Selection
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AbilityType.values().forEach { abilityType ->
                    AbilityTypeCard(
                        abilityType = abilityType,
                        isSelected = selectedAbilityType == abilityType,
                        onClick = {
                            selectedAbilityType = abilityType
                            accessibilityManager?.vibrate(AccessibilityManager.PATTERN_CONFIRM)
                            accessibilityManager?.speak("${abilityType.name} selected")
                        }
                    )
                }
            }
        } else {
            // Language Selection
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val languages = listOf("English", "Spanish", "French", "German", "Chinese", "Japanese")
                languages.forEach { language ->
                    LanguageCard(
                        language = language,
                        isSelected = selectedLanguage == language,
                        onClick = {
                            selectedLanguage = language
                            accessibilityManager?.vibrate(AccessibilityManager.PATTERN_CONFIRM)
                            accessibilityManager?.speak("$language selected")
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Navigation Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (currentStep > 0) {
                OutlinedButton(
                    onClick = {
                        currentStep -= 1
                        accessibilityManager?.speak("Going back")
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Back")
                }
            }

            Button(
                onClick = {
                    if (currentStep < 1) {
                        accessibilityManager?.vibrate(AccessibilityManager.PATTERN_CONFIRM)
                        currentStep += 1
                        accessibilityManager?.speak("Next step")
                    } else {
                        viewModel.saveUserPreferences(selectedAbilityType, selectedLanguage)
                        accessibilityManager?.speak("Setup complete. Welcome to SenseSafe.")
                        onOnboardingComplete()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (currentStep < 1) "Next" else "Get Started",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (currentStep < 1) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Skip option
        TextButton(
            onClick = {
                viewModel.saveUserPreferences(selectedAbilityType, selectedLanguage)
                accessibilityManager?.speak("Setup complete. Welcome to SenseSafe.")
                onOnboardingComplete()
            }
        ) {
            Text("Skip & Use Defaults")
        }
    }
}

@Composable
fun AbilityTypeCard(
    abilityType: AbilityType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) null else CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getAbilityIcon(abilityType),
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = abilityType.name.replace("_", " "),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = getAbilityDescription(abilityType),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun LanguageCard(
    language: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) null else CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = language,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun getAbilityIcon(abilityType: AbilityType): ImageVector {
    return when (abilityType) {
        AbilityType.NORMAL -> Icons.Default.Person
        AbilityType.BLIND -> Icons.Default.VisibilityOff
        AbilityType.LOW_VISION -> Icons.Default.RemoveRedEye
        AbilityType.DEAF -> Icons.Default.VolumeOff
        AbilityType.HARD_OF_HEARING -> Icons.Default.VolumeUp
        AbilityType.NON_VERBAL -> Icons.Default.MicOff
        AbilityType.ELDERLY -> Icons.Default.AccessibleForward
    }
}

private fun getAbilityDescription(abilityType: AbilityType): String {
    return when (abilityType) {
        AbilityType.NORMAL -> "Standard accessibility settings"
        AbilityType.BLIND -> "Enhanced audio and haptic feedback"
        AbilityType.LOW_VISION -> "Larger text and high contrast"
        AbilityType.DEAF -> "Visual and vibration alerts"
        AbilityType.HARD_OF_HEARING -> "Amplified audio and visual cues"
        AbilityType.NON_VERBAL -> "Simplified buttons and gestures"
        AbilityType.ELDERLY -> "Larger text and clearer interface"
    }
}
