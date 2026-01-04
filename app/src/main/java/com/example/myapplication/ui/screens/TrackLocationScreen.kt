package com.example.myapplication.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.BuildConfig
import com.example.myapplication.viewmodel.LocationState
import com.example.myapplication.viewmodel.TrackLocationViewModel
import com.example.myapplication.viewmodel.TrackLocationViewModelFactory
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.location

/**
 * Track Location Screen - Mapbox + MapTiler Integration
 * 
 * Features:
 * - MapTiler outdoor-v4 style for terrain visualization
 * - Mapbox SDK for vector-based maps
 * - Runtime permission handling
 * - User location marker with name
 * - Loading states and error handling
 * - Smooth camera animations
 * - Battery-optimized location fetching
 * 
 * @param onNavigateBack Callback to navigate back
 */
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TrackLocationScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    
    // ViewModel with factory
    val viewModel: TrackLocationViewModel = viewModel(
        factory = TrackLocationViewModelFactory(
            context.applicationContext as android.app.Application
        )
    )
    
    // State
    val locationState by viewModel.locationState.collectAsState()
    val userName by viewModel.userName.collectAsState()
    
    // Location permission
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    
    // MapView state
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var isMapReady by remember { mutableStateOf(false) }
    
    // Fetch location when permission granted and map ready
    LaunchedEffect(locationPermissionState.status.isGranted, isMapReady) {
        if (locationPermissionState.status.isGranted && isMapReady) {
            viewModel.fetchCurrentLocation()
        }
    }
    
    // Update map when location changes
    LaunchedEffect(locationState) {
        if (locationState is LocationState.Success && mapView != null) {
            val success = locationState as LocationState.Success
            updateMapLocation(
                mapView = mapView!!,
                latitude = success.latitude,
                longitude = success.longitude,
                userName = userName ?: "You",
                accuracy = success.accuracy
            )
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Track Location") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                !locationPermissionState.status.isGranted -> {
                    // Permission not granted
                    PermissionRequestContent(
                        onRequestPermission = { locationPermissionState.launchPermissionRequest() },
                        shouldShowRationale = locationPermissionState.status.shouldShowRationale,
                        onNavigateBack = onNavigateBack,
                        onOpenSettings = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }
                    )
                }
                else -> {
                    // Permission granted - show map
                    MapboxMapView(
                        onMapReady = { 
                            mapView = it
                            isMapReady = true
                        }
                    )
                    
                    // Loading overlay
                    if (locationState is LocationState.Loading) {
                        LoadingOverlay()
                    }
                    
                    // Error overlay
                    if (locationState is LocationState.Error) {
                        TrackLocationErrorOverlay(
                            message = (locationState as LocationState.Error).message,
                            onRetry = { viewModel.retryLocationFetch() },
                            onOpenSettings = {
                                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            mapView?.onDestroy()
        }
    }
}

/**
 * Mapbox MapView with MapTiler outdoor style
 */
@Composable
fun MapboxMapView(
    onMapReady: (MapView) -> Unit
) {
    val context = LocalContext.current
    val mapTilerKey = BuildConfig.MAPTILER_API_KEY
    val mapTilerStyleUrl = "https://api.maptiler.com/maps/outdoor-v4/style.json?key=$mapTilerKey"
    
    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                // Load MapTiler outdoor style
                mapboxMap.loadStyle(mapTilerStyleUrl) { style ->
                    // Style loaded successfully
                    onMapReady(this)
                }
                
                // Configure gestures
                gestures.pitchEnabled = false // Disable 3D tilt for simplicity
                gestures.rotateEnabled = true
                gestures.scrollEnabled = true
                gestures.doubleTapToZoomInEnabled = true
                gestures.pinchToZoomEnabled = true
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { view ->
            // Update logic if needed
        }
    )
}

/**
 * Update map with user location marker
 */
private fun updateMapLocation(
    mapView: MapView,
    latitude: Double,
    longitude: Double,
    userName: String,
    accuracy: Float
) {
    val point = Point.fromLngLat(longitude, latitude)
    
    // Animate camera to location
    mapView.mapboxMap.flyTo(
        CameraOptions.Builder()
            .center(point)
            .zoom(16.0)
            .build(),
        com.mapbox.maps.plugin.animation.MapAnimationOptions.Builder()
            .duration(1000L)
            .build()
    )
    
    // Add marker annotation
    val annotationApi = mapView.annotations
    val pointAnnotationManager = annotationApi.createPointAnnotationManager()
    
    // Clear existing annotations
    pointAnnotationManager.deleteAll()
    
    // Create marker with user name
    val pointAnnotationOptions = PointAnnotationOptions()
        .withPoint(point)
        .withTextField("$userName\nYou are here 📍")
        .withTextSize(12.0)
        .withTextColor("#000000")
        .withTextHaloColor("#FFFFFF")
        .withTextHaloWidth(2.0)
        .withTextOffset(listOf(0.0, -2.5))
    
    pointAnnotationManager.create(pointAnnotationOptions)
    
    // Optional: Add accuracy circle (requires CircleAnnotationManager)
    // For simplicity, omitted here - can be added with CircleAnnotationOptions
}

/**
 * Permission request UI
 */
@Composable
fun PermissionRequestContent(
    onRequestPermission: () -> Unit,
    shouldShowRationale: Boolean,
    onNavigateBack: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.MyLocation,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Location Permission Required",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = if (shouldShowRationale) {
                "This app needs location access to show your current position on the map. " +
                "Your location data is only used for display and is not stored or shared without your consent."
            } else {
                "Location access is required to track your position on the map. " +
                "If you previously denied permission, please enable it in app settings."
            },
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (!shouldShowRationale) {
            // Permission permanently denied - show settings button
            Button(
                onClick = onOpenSettings,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open Settings")
            }
        } else {
            Button(
                onClick = onRequestPermission,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Grant Permission")
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedButton(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Go Back")
        }
    }
}

/**
 * Loading overlay
 */
@Composable
fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Fetching your location...",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Error overlay for Track Location screen
 */
@Composable
fun TrackLocationErrorOverlay(
    message: String,
    onRetry: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Location Error",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Retry")
                    }
                    
                    OutlinedButton(
                        onClick = onOpenSettings,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("GPS Settings")
                    }
                }
            }
        }
    }
}
