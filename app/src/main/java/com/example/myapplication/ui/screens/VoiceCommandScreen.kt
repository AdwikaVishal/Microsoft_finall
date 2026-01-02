package com.example.myapplication.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.accessibility.AccessibilityManager
import com.example.myapplication.utils.VoiceCommandManager

/**
 * Voice Command Screen for user speech input.
 * Designed for accessibility with visual feedback for blind users.
 * Ready for Azure Speech to Text and Azure OpenAI integration.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceCommandScreen(
    accessibilityManager: AccessibilityManager? = null,
    onNavigateBack: () -> Unit,
    onProcessWithAzure: (String) -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val voiceManager = remember { VoiceCommandManager(context, accessibilityManager) }
    
    val spokenText by voiceManager.spokenText.collectAsState()
    var isListening by remember { mutableStateOf(false) }
    var manualText by remember { mutableStateOf("") }
    var showProcessing by remember { mutableStateOf(false) }
    
    // Animation for microphone pulse
    val pulseScale by animateFloatAsState(
        targetValue = if (isListening) 1.2f else 1f,
        animationSpec = androidx.compose.animation.core.tween(500),
        label = "pulse"
    )
    
    val micColor by animateColorAsState(
        targetValue = if (isListening) Color.Red else MaterialTheme.colorScheme.primary,
        animationSpec = androidx.compose.animation.core.tween(300),
        label = "micColor"
    )

    // Handle speech results
    LaunchedEffect(spokenText) {
        if (spokenText.isNotEmpty() && !isListening) {
            manualText = spokenText
            accessibilityManager?.speak("You said: $spokenText")
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            voiceManager.destroy()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voice Command") },
                navigationIcon = {
                    IconButton(onClick = {
                        voiceManager.stopListening()
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Instructions
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Speak your command",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Try: \"I need help\", \"Where is the exit\", or describe your situation",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Microphone Button with Animation
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Pulse effect
                if (isListening) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(Color.Red.copy(alpha = 0.3f))
                    )
                }
                
                // Main mic button
                IconButton(
                    onClick = {
                        if (isListening) {
                            voiceManager.stopListening()
                            isListening = false
                        } else {
                            manualText = ""
                            voiceManager.startListening()
                            isListening = true
                        }
                    },
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(micColor)
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = if (isListening) "Stop listening" else "Start listening",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Text(
                text = if (isListening) "Listening..." else "Tap to speak",
                style = MaterialTheme.typography.bodyLarge,
                color = if (isListening) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Speech Result Display
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Your command:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (manualText.isNotEmpty()) {
                        Text(
                            text = manualText,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            text = "Waiting for input...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Manual Text Input (fallback)
            OutlinedTextField(
                value = manualText,
                onValueChange = { manualText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Or type your command here...") },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Process Button (for Azure integration)
            Button(
                onClick = {
                    if (manualText.isNotBlank()) {
                        showProcessing = true
                        accessibilityManager?.speak("Processing with Azure AI")
                        onProcessWithAzure(manualText)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = manualText.isNotBlank() && !showProcessing,
                shape = RoundedCornerShape(16.dp)
            ) {
                if (showProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Process with AI")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Azure Integration Placeholder
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Azure Integration (Coming Soon)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "This will connect to Azure Speech to Text and Azure OpenAI for advanced command processing.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

