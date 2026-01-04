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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
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
    onNavigateToTrackLocation: () -> Unit,
    onNavigateToLanguageSettings: () -> Unit = {},
    accessibilityManager: AccessibilityManager? = null,
    initialScanResult: String? = null
) {
    val sosState by sosViewModel.sosState.collectAsState()
    var showSosDialog by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    
    // ✅ FIX: Handle scan result from navigation
    var scanResult by remember { mutableStateOf(initialScanResult) }
    var showScanResultDialog by remember { mutableStateOf(initialScanResult != null) }

    // Trigger vibration feedback when SOS dialog opens
    LaunchedEffect(showSosDialog) {
        if (showSosDialog) {
            accessibilityManager?.vibrate(AccessibilityManager.PATTERN_CONFIRM)
        }
    }
    
    // ✅ FIX: Announce scan result when available
    LaunchedEffect(initialScanResult) {
        if (initialScanResult != null) {
            accessibilityManager?.speak(initialScanResult)
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
                    IconButton(onClick = { 
                        // Navigate to language settings
                        onNavigateToLanguageSettings()
                    }) {
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
                text = stringResource(R.string.quick_actions),
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
                    title = stringResource(R.string.scan_area),
                    description = stringResource(R.string.detect_exits_hazards),
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
                    title = stringResource(R.string.voice_command),
                    description = stringResource(R.string.speak_for_help),
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
                text = stringResource(R.string.your_status),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            StatusCard(
                title = stringResource(R.string.active_alerts),
                status = if (alertViewModel.hasActiveAlert()) stringResource(R.string.alert_active) else stringResource(R.string.no_active_alerts),
                icon = if (alertViewModel.hasActiveAlert()) Icons.Default.Notifications else Icons.Default.CheckCircle,
                statusColor = if (alertViewModel.hasActiveAlert()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            StatusCard(
                title = stringResource(R.string.sos_status),
                status = when (sosState) {
                    is SOSState.Idle -> stringResource(R.string.ready)
                    is SOSState.Loading -> stringResource(R.string.sending)
                    is SOSState.Success -> stringResource(R.string.sent)
                    is SOSState.Error -> stringResource(R.string.failed)
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
                text = stringResource(R.string.features),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            FeatureCard(
                icon = Icons.AutoMirrored.Filled.List,
                title = stringResource(R.string.incident_timeline),
                description = stringResource(R.string.view_reported_incidents),
                onClick = {
                    accessibilityManager?.vibrate(AccessibilityManager.PATTERN_CONFIRM)
                    accessibilityManager?.speak("Opening incident timeline")
                    onNavigateToTimeline()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            FeatureCard(
                icon = Icons.Default.Description,
                title = stringResource(R.string.report_incident),
                description = stringResource(R.string.report_new_incident),
                onClick = {
                    accessibilityManager?.vibrate(AccessibilityManager.PATTERN_CONFIRM)
                    accessibilityManager?.speak("Opening incident report")
                    onNavigateToReportIncident()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            FeatureCard(
                icon = Icons.Default.LocationOn,
                title = stringResource(R.string.track_location),
                description = stringResource(R.string.share_location_responders),
                onClick = {
                    accessibilityManager?.vibrate(AccessibilityManager.PATTERN_CONFIRM)
                    accessibilityManager?.speak("Opening location tracker")
                    onNavigateToTrackLocation()
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
                Text(stringResource(R.string.test_alert_dev))
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
                    text = stringResource(R.string.send_sos),
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
                        text = stringResource(R.string.select_current_status),
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
                    Text(stringResource(R.string.cancel))
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
                    title = { Text(stringResource(R.string.sending_sos)) },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(stringResource(R.string.stay_calm_help_coming))
                        }
                    },
                    confirmButton = {}
                )
            }
            is SOSState.Success -> {
                AlertDialog(
                    onDismissRequest = { 
                        showConfirmationDialog = false
                        sosViewModel.resetState() // Reset state to allow new SOS requests
                    },
                    icon = {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                    },
                    title = { Text(stringResource(R.string.sos_sent_successfully)) },
                    text = {
                        Column {
                            Text(stringResource(R.string.sos_sent_to_responders))
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
                            sosViewModel.resetState() // Reset state to allow new SOS requests
                            accessibilityManager?.speak("SOS confirmation closed")
                        }) {
                            Text(stringResource(R.string.ok))
                        }
                    }
                )
            }
            is SOSState.Error -> {
                AlertDialog(
                    onDismissRequest = { 
                        showConfirmationDialog = false
                        sosViewModel.resetState() // Reset state to allow new SOS requests
                    },
                    icon = {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                    },
                    title = { Text(stringResource(R.string.sos_failed)) },
                    text = { Text((sosState as SOSState.Error).message) },
                    confirmButton = {
                        TextButton(onClick = { 
                            showConfirmationDialog = false
                            sosViewModel.resetState() // Reset state to allow new SOS requests
                        }) {
                            Text(stringResource(R.string.ok))
                        }
                    }
                )
            }
            else -> {}
        }
    }
    
    // ✅ FIX: Scan Result Dialog
    if (showScanResultDialog && scanResult != null) {
        AlertDialog(
            onDismissRequest = { 
                showScanResultDialog = false
                scanResult = null
            },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { 
                Text(
                    text = stringResource(R.string.exit_detection_complete),
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = { 
                Text(scanResult ?: stringResource(R.string.scan_completed)) 
            },
            confirmButton = {
                TextButton(onClick = { 
                    showScanResultDialog = false
                    scanResult = null
                    accessibilityManager?.speak("Scan result dismissed")
                }) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showScanResultDialog = false
                    scanResult = null
                    onNavigateToCamera() // Scan again
                    accessibilityManager?.speak("Opening camera to scan again")
                }) {
                    Text(stringResource(R.string.scan_again))
                }
            }
        )
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
        SOSStatus.TRAPPED -> Icons.Default.Block
        SOSStatus.INJURED -> Icons.Default.LocalHospital
        SOSStatus.NEED_HELP -> Icons.AutoMirrored.Filled.Help
        SOSStatus.SAFE -> Icons.Default.CheckCircle
    }
}
@Composable
private fun getStatusColor(status: SOSStatus): androidx.compose.ui.graphics.Color {
    return when (status) {
        SOSStatus.TRAPPED -> MaterialTheme.colorScheme.error
        SOSStatus.INJURED -> MaterialTheme.colorScheme.error
        SOSStatus.NEED_HELP -> MaterialTheme.colorScheme.secondary
        SOSStatus.SAFE -> MaterialTheme.colorScheme.primary
    }
}
