package com.example.myapplication.ui

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.accessibility.AccessibilityManager
import com.example.myapplication.utils.LocationHelper
import com.example.myapplication.viewmodel.IncidentViewModel
import com.example.myapplication.viewmodel.ReportIncidentState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ReportIncidentScreen(
    viewModel: IncidentViewModel,
    onNavigateBack: () -> Unit,
    accessibilityManager: AccessibilityManager? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val locationHelper = remember { LocationHelper(context) }

    // State from ViewModel
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val description by viewModel.description.collectAsState()
    val location by viewModel.location.collectAsState()
    val reportState by viewModel.reportState.collectAsState()

    // Local state for dropdown
    var categoryExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Location permission
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    // Update error message based on report state
    LaunchedEffect(reportState) {
        if (reportState is ReportIncidentState.Error) {
            errorMessage = (reportState as ReportIncidentState.Error).message
            accessibilityManager?.vibrate(AccessibilityManager.PATTERN_DANGER)
        }
    }

    // Handle success
    LaunchedEffect(reportState) {
        if (reportState is ReportIncidentState.Success) {
            accessibilityManager?.speak("Incident reported successfully")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Report Incident",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Category Dropdown
            Text(
                text = "Incident Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { expanded ->
                    categoryExpanded = expanded
                    if (expanded) {
                        accessibilityManager?.vibrate(AccessibilityManager.PATTERN_CONFIRM)
                    }
                }
            ) {
                OutlinedTextField(
                    value = selectedCategory ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select category") },
                    placeholder = { Text("Choose incident type") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    leadingIcon = {
                        Icon(
                            imageVector = getCategoryIcon(selectedCategory),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    val categories = listOf("Fire", "Flood", "Earthquake", "Medical", "Accident", "Other")
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = getCategoryIcon(category),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(category)
                                }
                            },
                            onClick = {
                                viewModel.updateCategory(category)
                                categoryExpanded = false
                                accessibilityManager?.speak("$category selected")
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

            // Description Text Field
            Text(
                text = "Description",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            OutlinedTextField(
                value = description,
                onValueChange = { text ->
                    viewModel.updateDescription(text)
                },
                label = { Text("Describe the incident") },
                placeholder = { Text("Provide details about what happened...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 6,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Default
                ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )

            // Location Section
            Text(
                text = "Location",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = if (location != null) MaterialTheme.colorScheme.primary 
                                   else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = location ?: "Location not available",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (location != null) MaterialTheme.colorScheme.onSurface
                                       else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = {
                                accessibilityManager?.vibrate(AccessibilityManager.PATTERN_CONFIRM)
                                accessibilityManager?.speak("Refreshing location")
                                if (locationPermissionState.status.isGranted) {
                                    scope.launch {
                                        try {
                                            val androidLocation = locationHelper.getCurrentLocation()
                                            if (androidLocation != null) {
                                                viewModel.updateLocation(
                                                    androidLocation.latitude,
                                                    androidLocation.longitude,
                                                    null
                                                )
                                                accessibilityManager?.speak("Location updated")
                                            } else {
                                                accessibilityManager?.speak("Unable to get location")
                                            }
                                        } catch (e: Exception) {
                                            accessibilityManager?.speak("Location error: ${e.message}")
                                        }
                                    }
                                } else {
                                    accessibilityManager?.vibrate(AccessibilityManager.PATTERN_DANGER)
                                    accessibilityManager?.speak("Location permission required")
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh location",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Request location permission if not granted
                    if (!locationPermissionState.status.isGranted) {
                        OutlinedButton(
                            onClick = {
                                accessibilityManager?.vibrate(AccessibilityManager.PATTERN_CONFIRM)
                                locationPermissionState.launchPermissionRequest()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Enable Location")
                        }
                    }
                }
            }

            // Error Message
            if (errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { errorMessage = null }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Submit Button
            Button(
                onClick = {
                    accessibilityManager?.vibrate(AccessibilityManager.PATTERN_CONFIRM)
                    viewModel.reportIncident()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = reportState !is ReportIncidentState.Loading &&
                         selectedCategory != null &&
                         description.isNotBlank() &&
                         location != null
            ) {
                if (reportState is ReportIncidentState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Text(
                    text = if (reportState is ReportIncidentState.Loading) "Reporting..." 
                           else "Report Incident",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Helper text
            Text(
                text = "Your report will be sent to emergency responders",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // Success Dialog
    if (reportState is ReportIncidentState.Success) {
        AlertDialog(
            onDismissRequest = {
                viewModel.resetReportState()
                onNavigateBack()
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "Incident Reported",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Your incident has been reported successfully.",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Incident ID: ${(reportState as ReportIncidentState.Success).incidentId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Emergency responders have been notified.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetReportState()
                        onNavigateBack()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun getCategoryIcon(category: String?): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category?.lowercase()) {
        "fire" -> Icons.Default.LocalFireDepartment
        "flood" -> Icons.Default.Water
        "earthquake" -> Icons.Default.Vibration
        "medical" -> Icons.Default.LocalHospital
        "accident" -> Icons.Default.CarCrash
        else -> Icons.Default.Warning
    }
}
