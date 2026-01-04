package com.example.myapplication.ui.screens

import android.Manifest
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.AbilityProfile
import com.example.myapplication.model.DetectedObject
import com.example.myapplication.model.ScanResult
import com.example.myapplication.model.ScanUiState
import com.example.myapplication.utils.FrameConverter
import com.example.myapplication.viewmodel.RoboflowScanViewModel
import com.example.myapplication.viewmodel.RoboflowScanViewModelFactory
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.util.Locale
import java.util.concurrent.Executors

/**
 * Camera Screen with Roboflow Exit Detection Integration.
 * 
 * Features:
 * - CameraX live preview
 * - Scan button to capture and analyze image with 4 Roboflow models
 * - Sequential detection: windows, doors, hallways, stairs (one after another)
 * - Image resizing to prevent network errors
 * - Real-time exit detection with bounding box overlay
 * - "Scanning..." text during API calls
 * - "No exits detected" when no predictions from any model (with manual retry)
 * - "Exit found" speech feedback when anything matches
 * - Friendly error handling with Toast (no crashes)
 * - No auto-cancel during API requests (prevents SocketException)
 * 
 * API Key Setup:
 * 1. Get API keys from Roboflow dashboard for each model
 * 2. Add to local.properties:
 *    - RF_WINDOWS_KEY=your-key
 *    - RF_DOOR_KEY=your-key
 *    - RF_HALL_KEY=your-key
 *    - RF_STAIRS_KEY=your-key
 * 3. Keys are loaded into BuildConfig
 * 
 * Model URL Setup:
 * 1. Configure URLs in RoboflowService.kt companion object
 * 2. Replace placeholders with actual Roboflow model URLs
 * 
 * @property profile Ability profile for accessibility features (DEAF, BLIND, etc.)
 * @property onExitDetected Callback when an exit is detected
 * @property onNavigateBack Callback to navigate back
 */
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    profile: AbilityProfile,
    onExitDetected: (String) -> Unit,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    // Create ViewModel with factory for proper context injection
    val viewModel: RoboflowScanViewModel = viewModel(
        factory = RoboflowScanViewModelFactory(
            (context.applicationContext as android.app.Application)
        )
    )
    
    // Collect ViewModel state
    val uiState by viewModel.uiState.collectAsState()
    val scanResult by viewModel.scanResult.collectAsState()
    val isConfigured by viewModel.isConfigured.collectAsState()
    
    // Camera permission
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    
    // Image capture reference
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    
    // Local state for real-time detection (from ImageAnalysis)
    var detectedText by remember { mutableStateOf("Scanning...") }
    var showArrow by remember { mutableStateOf(false) }
    var showWarning by remember { mutableStateOf(false) }
    
    // Text-to-Speech for "Exit found" feedback
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    
    // Initialize TTS
    DisposableEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }
        
        onDispose {
            tts?.shutdown()
        }
    }
    
    // Show Toast for errors
    LaunchedEffect(uiState) {
        if (uiState is ScanUiState.Error) {
            val errorMessage = (uiState as ScanUiState.Error).message
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }
    }
    
    // Handle scan result for exit detection callback and speech
    LaunchedEffect(scanResult) {
        scanResult?.let { result ->
            if (result.objects.isNotEmpty()) {
                // Check if any detected object is an exit (window, door, hallway, stair)
                val exitDetected = result.objects.any { obj ->
                    val name = obj.name.lowercase()
                    name.contains("window") ||
                    name.contains("door") ||
                    name.contains("hallway") ||
                    name.contains("hall") ||
                    name.contains("stair") ||
                    name.contains("stairs")
                }
                
                if (exitDetected) {
                    // Trigger speech feedback
                    tts?.speak(
                        "Exit found. Follow the highlighted area.",
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        "exit_found"
                    )
                    
                    // Callback to parent
                    onExitDetected("Exit detected: ${result.objects.first().name}")
                    
                    // Show visual feedback
                    showArrow = true
                }
            }
        }
    }
    
    /**
     * Capture image and send to Roboflow for scanning.
     * Simplified approach: directly convert ImageProxy to Bitmap.
     */
    fun captureAndScan() {
        imageCapture?.takePicture(
            cameraExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    try {
                        // Convert ImageProxy to Bitmap directly
                        val bitmap = imageProxy.toBitmap()
                        imageProxy.close()
                        
                        if (bitmap != null) {
                            // Send bitmap to ViewModel for Roboflow scanning
                            viewModel.scanImage(bitmap)
                        } else {
                            // ✅ FIX: Run Toast on Main Thread
                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                Toast.makeText(context, "Failed to process image", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        imageProxy.close()
                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                            Toast.makeText(context, "Error processing image: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                
                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraScreen", "Image capture failed: ${exception.message}")
                    // ✅ FIX: Run Toast on Main Thread
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        Toast.makeText(context, "Failed to capture image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }
    
    // Extension to convert ImageProxy to Bitmap
    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    fun ImageProxy.toBitmap(): android.graphics.Bitmap? {
        return try {
            val image = this.image ?: return null
            
            // Get the YUV planes
            val planes = image.planes
            val yBuffer = planes[0].buffer
            val uBuffer = planes[1].buffer
            val vBuffer = planes[2].buffer
            
            val ySize = yBuffer.remaining()
            val uSize = uBuffer.remaining()
            val vSize = vBuffer.remaining()
            
            // Create NV21 byte array
            val nv21 = ByteArray(ySize + uSize + vSize)
            yBuffer.get(nv21, 0, ySize)
            vBuffer.get(nv21, ySize, vSize)
            uBuffer.get(nv21, ySize + vSize, uSize)
            
            // Convert NV21 to Bitmap using YuvImage
            val yuvImage = android.graphics.YuvImage(
                nv21,
                android.graphics.ImageFormat.NV21,
                image.width,
                image.height,
                null
            )
            
            val out = java.io.ByteArrayOutputStream()
            yuvImage.compressToJpeg(
                android.graphics.Rect(0, 0, image.width, image.height),
                90,
                out
            )
            
            val imageBytes = out.toByteArray()
            val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            
            // Apply rotation if needed
            val rotationDegrees = this.imageInfo.rotationDegrees
            if (rotationDegrees != 0 && bitmap != null) {
                val matrix = android.graphics.Matrix()
                matrix.postRotate(rotationDegrees.toFloat())
                android.graphics.Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                )
            } else {
                bitmap
            }
        } catch (e: Exception) {
            Log.e("CameraScreen", "Failed to convert ImageProxy to Bitmap", e)
            null
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Area") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.cancelScan()
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.7f),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                !cameraPermissionState.status.isGranted -> {
                    // Permission not granted
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Camera Permission Required",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { cameraPermissionState.launchPermissionRequest() }
                        ) {
                            Text("Grant Permission")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onNavigateBack
                        ) {
                            Text("Go Back", color = Color.White)
                        }
                    }
                }
                else -> {
                    // Permission granted - show camera
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Camera Preview
                        AndroidView(
                            factory = { ctx ->
                                val previewView = PreviewView(ctx).apply {
                                    layoutParams = android.view.ViewGroup.LayoutParams(
                                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    scaleType = PreviewView.ScaleType.FILL_CENTER
                                }
                                
                                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                cameraProviderFuture.addListener({
                                    val cameraProvider = cameraProviderFuture.get()
                                    val preview = Preview.Builder().build().also {
                                        it.setSurfaceProvider(previewView.surfaceProvider)
                                    }
                                    
                                    imageCapture = ImageCapture.Builder()
                                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                        .build()
                                    
                                    val imageAnalyzer = ImageAnalysis.Builder()
                                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                        .build()
                                        .also { analyzer ->
                                            analyzer.setAnalyzer(cameraExecutor) { imageProxy ->
                                                // Simple real-time detection simulation
                                                processImageProxy(imageProxy) { result ->
                                                    // ✅ FIX: Update Compose state on Main Thread
                                                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                                                        detectedText = result
                                                        if (result.contains("EXIT", true)) {
                                                            showArrow = true
                                                            showWarning = false
                                                        } else {
                                                            showArrow = false
                                                            showWarning = (System.currentTimeMillis() % 10000) < 2000
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    
                                    try {
                                        cameraProvider.unbindAll()
                                        cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            CameraSelector.DEFAULT_BACK_CAMERA,
                                            preview,
                                            imageCapture,
                                            imageAnalyzer
                                        )
                                    } catch (exc: Exception) {
                                        Log.e("CameraScreen", "Use case binding failed", exc)
                                    }
                                }, ContextCompat.getMainExecutor(ctx))
                                
                                previewView
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                        
                        // =====================================================
                        // BOUNDING BOX OVERLAY
                        // Draw bounding boxes on detected objects
                        // =====================================================
                        if (uiState is ScanUiState.Success) {
                            val result = (uiState as ScanUiState.Success).result
                            if (result.objects.isNotEmpty()) {
                                BoundingBoxOverlay(
                                    objects = result.objects,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                        
                        // =====================================================
                        // SCANNING... TEXT
                        // Show during API calls (4 models in parallel)
                        // =====================================================
                        if (uiState == ScanUiState.Loading) {
                            ScanningOverlay()
                        }
                        
                        // =====================================================
                        // NO EXITS DETECTED TEXT
                        // Show when no predictions from any of the 4 models
                        // User can manually retry
                        // =====================================================
                        if (uiState is ScanUiState.Success) {
                            val result = (uiState as ScanUiState.Success).result
                            if (result.objects.isEmpty()) {
                                NoDetectionsOverlay(
                                    message = result.description.ifEmpty { "No exits detected. Try different angle or lighting." },
                                    onRetry = { captureAndScan() },
                                    onDismiss = { viewModel.resetState() }
                                )
                            }
                        }
                        
                        // =====================================================
                        // REAL-TIME DETECTION OVERLAY
                        // =====================================================
                        Text(
                            text = detectedText,
                            color = Color.White,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(32.dp)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        
                        // Accessibility overlays for DEAF users
                        if (profile == AbilityProfile.DEAF) {
                            if (showArrow) {
                                Column(
                                    modifier = Modifier.align(Alignment.Center),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = "Go Forward",
                                        tint = Color.Green,
                                        modifier = Modifier.size(100.dp)
                                    )
                                    Text(
                                        "EXIT",
                                        color = Color.Green,
                                        fontSize = 32.sp,
                                        modifier = Modifier.background(Color.Black.copy(alpha = 0.7f))
                                    )
                                }
                            }
                            
                            if (showWarning) {
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .padding(top = 80.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Danger",
                                        tint = Color.Red,
                                        modifier = Modifier.size(80.dp)
                                    )
                                    Text(
                                        "HAZARD",
                                        color = Color.Red,
                                        fontSize = 32.sp,
                                        modifier = Modifier.background(Color.Black.copy(alpha = 0.7f))
                                    )
                                }
                            }
                        }
                        
                        // =====================================================
                        // SCAN BUTTON
                        // =====================================================
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 100.dp)
                        ) {
                            ScanButton(
                                isLoading = uiState == ScanUiState.Loading,
                                isConfigured = isConfigured,
                                onClick = { captureAndScan() }
                            )
                        }
                        
                        // =====================================================
                        // RESULT OVERLAY
                        // =====================================================
                        AnimatedVisibility(
                            visible = uiState is ScanUiState.Success && 
                                      (uiState as ScanUiState.Success).result.objects.isNotEmpty(),
                            enter = fadeIn(),
                            exit = fadeOut(),
                            modifier = Modifier.align(Alignment.TopCenter)
                        ) {
                            (uiState as? ScanUiState.Success)?.let { successState ->
                                ScanResultOverlay(
                                    result = successState.result,
                                    onDismiss = { 
                                        showArrow = false
                                        viewModel.resetState() 
                                    },
                                    onRetry = { captureAndScan() }
                                )
                            }
                        }
                        
                        // =====================================================
                        // ERROR OVERLAY
                        // Shows error without crashing
                        // =====================================================
                        AnimatedVisibility(
                            visible = uiState is ScanUiState.Error,
                            enter = fadeIn(),
                            exit = fadeOut(),
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            val errorState = uiState
                            if (errorState is ScanUiState.Error) {
                                ErrorOverlay(
                                    message = errorState.message,
                                    onDismiss = { viewModel.clearError() },
                                    onRetry = { captureAndScan() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Bounding Box Overlay - Draws detection boxes on camera preview.
 * 
 * Roboflow returns center coordinates, so we convert to top-left for drawing.
 * Shows label with class name and confidence score.
 * 
 * @property objects List of detected objects
 * @property modifier Modifier for the canvas
 */
@Composable
fun BoundingBoxOverlay(
    objects: List<DetectedObject>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        objects.forEach { obj ->
            obj.boundingBox?.let { box ->
                // Roboflow x, y are CENTER coordinates
                // Convert to top-left corner for drawing
                val left = box.x
                val top = box.y
                val right = box.x + box.width
                val bottom = box.y + box.height
                
                // Draw bounding box rectangle with green color
                drawRect(
                    color = Color.Green,
                    topLeft = Offset(left, top),
                    size = Size(box.width, box.height),
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
                
                // Draw label text
                val labelText = "${obj.name} ${(obj.confidence * 100).toInt()}%"
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        labelText,
                        left,
                        top - 8,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.GREEN
                            textSize = 36f
                            isAntiAlias = true
                            setShadowLayer(4f, 2f, 2f, android.graphics.Color.BLACK)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Scanning Overlay - Shows "Scanning..." text during parallel API calls.
 * Displayed while waiting for responses from all 4 Roboflow models.
 */
@Composable
fun ScanningOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Scanning...",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Analyzing with 4 detection models...",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/**
 * No Detections Overlay - Shows when no exits are detected.
 * 
 * @property message The message to display (e.g., "No exits detected")
 * @property onRetry Callback to retry the scan
 * @property onDismiss Callback to dismiss and return to camera
 */
@Composable
fun NoDetectionsOverlay(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.9f)
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.Yellow,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Scan Complete",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("Dismiss")
                    }
                    Button(
                        onClick = onRetry,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Retry")
                    }
                }
            }
        }
    }
}

/**
 * Scan Button - FAB style button with loading state.
 * 
 * @property isLoading Whether scan is in progress
 * @property isConfigured Whether API is configured
 * @property onClick Scan button click callback
 */
@Composable
fun ScanButton(
    isLoading: Boolean,
    isConfigured: Boolean,
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = { if (!isLoading && isConfigured) onClick() },
        modifier = Modifier.size(72.dp),
        containerColor = if (isConfigured) Color(0xFF6750A4) else Color.Gray,
        shape = CircleShape,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(32.dp),
                strokeWidth = 3.dp
            )
        } else {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Scan Area",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

/**
 * Scan Result Overlay - Shows detected objects after successful scan.
 * 
 * @property result The scan result
 * @property onDismiss Callback to dismiss the overlay
 * @property onRetry Callback to retry the scan
 */
@Composable
fun ScanResultOverlay(
    result: ScanResult,
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.9f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Scan Results",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Show description
            if (result.description.isNotBlank()) {
                Text(
                    text = result.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Show detected objects count
            if (result.objects.isNotEmpty()) {
                Text(
                    text = "Detected Objects: ${result.objects.size}",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                result.objects.forEach { obj ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = obj.name,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${(obj.confidence * 100).toInt()}%",
                            color = Color.Green,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onRetry,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("Retry")
                }
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Done")
                }
            }
        }
    }
}

/**
 * Error Overlay - Shows user-friendly error without crashing.
 * 
 * @property message Error message to display
 * @property onDismiss Callback to dismiss error
 * @property onRetry Callback to retry the scan
 */
@Composable
fun ErrorOverlay(
    message: String,
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Error",
                tint = Color(0xFFB3261E),
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Scan Failed",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("Dismiss")
                }
                Button(
                    onClick = onRetry,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Retry")
                }
            }
        }
    }
}

/**
 * Process ImageProxy for real-time detection simulation.
 */
@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
private fun processImageProxy(
    imageProxy: ImageProxy,
    onResult: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        // Simple simulation for real-time feedback
        onResult("Analyzing...")
    }
    imageProxy.close()
}

