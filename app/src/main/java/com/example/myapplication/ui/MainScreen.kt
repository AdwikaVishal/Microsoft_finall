package com.example.myapplication.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.accessibility.AccessibilityManager
import com.example.myapplication.model.SOSStatus
import com.example.myapplication.viewmodel.AlertViewModel
import com.example.myapplication.viewmodel.SOSState
import com.example.myapplication.viewmodel.SOSViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    sosViewModel: SOSViewModel,
    alertViewModel: AlertViewModel,
    onNavigateToTimeline: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToVoiceCommand: () -> Unit,
    onNavigateToReportIncident: () -> Unit,
    accessibilityManager: AccessibilityManager? = null
) {
    val sosState by sosViewModel.sosState.collectAsState()
    var showSosDialog by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }

    // Trigger vibration feedback when SOS dialog opens
    LaunchedEffect(showSosDialog) {
        if (showSosDialog) {
            accessibilityManager?.vibrate(AccessibilityManager.PATTERN_CONFIRM)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "SenseSafe",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = { /* Open settings */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { 
                    accessibilityManager?.vibrate(AccessibilityManager.PATTERN_CONFIRM)
                    accessibilityManager?.speak("SOS button pressed. Select your status.")
                    showSosDialog = true 
                },
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                },
                text = {
                    Text(
                        text = "SOS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                expanded = true
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Quick Actions Section
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    icon = Icons.Default.CameraAlt,
                    title = "Scan Area",
                    description = "Detect exits & hazards",
                    onClick = { 
                        accessibilityManager?.vibrate(AccessibilityManager.PATTERN_CONFIRM)
                        accessibilityManager?.speak("Opening camera to scan surroundings")
                        onNavigateToCamera()
                    },
                    modifier = Modifier.weight(1f),
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer
                )

                QuickActionCard(
                    icon = Icons.Default.RecordVoiceOver,
                    title = "Voice Command",
                    description = "Speak for help",
                    onClick = { 
                        accessibilityManager?.vibrate(AccessibilityManager.PATTERN_CONFIRM)
                        accessibilityManager?.speak("Opening voice command")
                        onNavigateToVoiceCommand()
                    },
                    modifier = Modifier.weight(1f),
                    backgroundColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Status Section
            Text(
                text = "Your Status",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            StatusCard(
                title = "Active Alerts",
                status = if (alertViewModel.hasActiveAlert()) "Alert Active" else "No Active Alerts",
                icon = if (alertViewModel.hasActiveAlert()) Icons.Default.Notifications else Icons.Default.CheckCircle,
                statusColor = if (alertViewModel.hasActiveAlert()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            StatusCard(
                title = "SOS Status",
                status = when (sosState) {
                    is SOSState.Idle -> "Ready"
                    is SOSState.Loading -> "Sending..."
                    is SOSState.Success -> "Sent"
                    is SOSState.Error -> "Failed"
                },
                icon = when (sosState) {
                    is SOSState.Idle -> Icons.Default.Shield
                    is SOSState.Loading -> Icons.Default.HourglassEmpty
                    is SOSState.Success -> Icons.Default.CheckCircle
                    is SOSState.Error -> Icons.Default.Error
                },
                statusColor = when (sosState) {
                    is SOSState.Error -> MaterialTheme.colorScheme.error
                    is SOSState.Success -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.tertiary
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation Cards
            Text(
                text = "Features",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            FeatureCard(
                icon = Icons.AutoMirrored.Filled.List,
                title = "Incident Timeline",
                description = "View your reported incidents",
                onClick = {
                    accessibilityManager?.vibrate(AccessibilityManager.PATTERN_CONFIRM)
                    accessibilityManager?.speak("Opening incident timeline")
                    onNavigateToTimeline()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            FeatureCard(
                icon = Icons.Default.Description,
                title = "Report Incident",
                description = "Report a new incident",
                onClick = {
                    accessibilityManager?.vibrate(AccessibilityManager.PATTERN_CONFIRM)
                    accessibilityManager?.speak("Opening incident report")
                    onNavigateToReportIncident()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            FeatureCard(
                icon = Icons.Default.LocationOn,
                title = "Track Location",
                description = "Share your location with responders",
                onClick = {
                    accessibilityManager?.vibrate(AccessibilityManager.PATTERN_CONFIRM)
                    accessibilityManager?.speak("Location tracking enabled")
                }
            )

            // Test Alert Button (for development)
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedButton(
                onClick = { 
                    accessibilityManager?.vibrate(AccessibilityManager.PATTERN_DANGER)
                    alertViewModel.showTestAlert()
                    accessibilityManager?.speak("Test alert triggered")
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.BugReport, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Test Alert (Dev)")
            }
        }
    }

    // SOS Status Selection Dialog
    if (showSosDialog) {
        AlertDialog(
            onDismissRequest = { showSosDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "Send SOS",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Select your current status:",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SOSStatus.values().forEach { status ->
                        Button(
                            onClick = {
                                accessibilityManager?.vibrate(AccessibilityManager.PATTERN_SOS)
                                accessibilityManager?.speak("Sending ${status.name} alert")
                                sosViewModel.sendSOS(status)
                                showSosDialog = false
                                showConfirmationDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = getStatusColor(status)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = getStatusIcon(status),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(status.name.replace("_", " "))
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { 
                    showSosDialog = false
                    accessibilityManager?.speak("SOS cancelled")
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Confirmation Dialog
    if (showConfirmationDialog) {
        when (sosState) {
            is SOSState.Loading -> {
                AlertDialog(
                    onDismissRequest = { showConfirmationDialog = false },
                    title = { Text("Sending SOS") },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Please stay calm. Help is on the way.")
                        }
                    },
                    confirmButton = {}
                )
            }
            is SOSState.Success -> {
                AlertDialog(
                    onDismissRequest = { showConfirmationDialog = false },
                    icon = {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                    },
                    title = { Text("SOS Sent Successfully") },
                    text = {
                        Column {
                            Text("Your SOS has been sent to emergency responders.")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "SOS ID: ${(sosState as SOSState.Success).sosId}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { 
                            showConfirmationDialog = false
                            accessibilityManager?.speak("SOS confirmation closed")
                        }) {
                            Text("OK")
                        }
                    }
                )
            }
            is SOSState.Error -> {
                AlertDialog(
                    onDismissRequest = { showConfirmationDialog = false },
                    icon = {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                    },
                    title = { Text("SOS Failed") },
                    text = { Text((sosState as SOSState.Error).message) },
                    confirmButton = {
                        TextButton(onClick = { showConfirmationDialog = false }) {
                            Text("OK")
                        }
                    }
                )
            }
            else -> {}
        }
    }
}

@Composable
fun QuickActionCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun StatusCard(
    title: String,
    status: String,
    icon: ImageVector,
    statusColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
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
                    .background(statusColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = status,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun FeatureCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun getStatusIcon(status: SOSStatus): ImageVector {
    return when (status) {
        SOSStatus.IM_TRAPPED -> Icons.Default.Block
        SOSStatus.IM_INJURED -> Icons.Default.LocalHospital
        SOSStatus.I_NEED_HELP -> Icons.AutoMirrored.Filled.Help
        SOSStatus.IM_SAFE -> Icons.Default.CheckCircle
    }
}
@Composable
private fun getStatusColor(status: SOSStatus): androidx.compose.ui.graphics.Color {
    return when (status) {
        SOSStatus.IM_TRAPPED -> MaterialTheme.colorScheme.error
        SOSStatus.IM_INJURED -> MaterialTheme.colorScheme.error
        SOSStatus.I_NEED_HELP -> MaterialTheme.colorScheme.secondary
        SOSStatus.IM_SAFE -> MaterialTheme.colorScheme.primary
    }
}
