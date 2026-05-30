package com.certforge.app.ui.screens.scanning

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanQrScreen(
    onPairingComplete: (setupToken: String) -> Unit = {},
    onBackClick: () -> Unit = {},
    viewModel: ScanQrViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var permissionDenied by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        permissionDenied = !granted
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.clearError()
            try {
                val inputImage = InputImage.fromFilePath(context, uri)
                val scanner = BarcodeScanning.getClient()
                scanner.process(inputImage)
                    .addOnSuccessListener { barcodes ->
                        barcodes.firstOrNull()?.rawValue?.let { value ->
                            viewModel.onQrScanned(value)
                        } ?: viewModel.setError("No QR code found in this image")
                    }
                    .addOnFailureListener { e ->
                        viewModel.setError("Failed to read QR code: ${e.message}")
                    }
            } catch (e: Exception) {
                viewModel.setError("Could not read image: ${e.message}")
            } catch (e: OutOfMemoryError) {
                viewModel.setError("Image too large to process")
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Navigate when QR is scanned successfully
    LaunchedEffect(state.scanned, state.setupToken) {
        if (state.scanned && state.setupToken != null) {
            try {
                onPairingComplete(state.setupToken!!)
            } catch (e: Exception) {
                android.util.Log.e("ScanQrScreen", "Navigation failed", e)
                viewModel.setError("Navigation error: ${e.message}")
            }
        }
    }

    if (!hasCameraPermission && permissionDenied) {
        NoPermissionContent(
            onBackClick = onBackClick,
            onPickFromGallery = { galleryLauncher.launch("image/*") }
        )
    } else if (hasCameraPermission) {
        CameraScannerContent(
            onQrScanned = { text -> viewModel.onQrScanned(text) },
            onPickFromGallery = { galleryLauncher.launch("image/*") },
            processing = state.processing,
            error = state.error,
            onClearError = { viewModel.clearError() },
            onBackClick = onBackClick,
            onCameraError = { viewModel.setError(it) }
        )
    } else {
        // Waiting for permission
        Box(modifier = Modifier.fillMaxSize())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CameraScannerContent(
    onQrScanned: (String) -> Unit,
    onPickFromGallery: () -> Unit,
    processing: Boolean,
    error: String?,
    onClearError: () -> Unit,
    onBackClick: () -> Unit,
    onCameraError: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Use AtomicLong for thread-safe debounce across the ML Kit background thread
    val lastScannedTime = remember { AtomicLong(0L) }

    // Capture callbacks before the factory lambda so they're in scope
    val qrCallback = onQrScanned
    val errorCallback = onCameraError

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                try {
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener(
                        {
                            try {
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }

                                val barcodeScanner = BarcodeScanning.getClient()
                                val imageAnalysis = ImageAnalysis.Builder()
                                    .setTargetResolution(Size(1280, 720))
                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                    .build()
                                    .also { analysis ->
                                        analysis.setAnalyzer(
                                            Executors.newSingleThreadExecutor()
                                        ) { imageProxy ->
                                            val mediaImage = imageProxy.image
                                            if (mediaImage != null) {
                                                @Suppress("MissingPermission")
                                                val inputImage = InputImage.fromMediaImage(
                                                    mediaImage, imageProxy.imageInfo.rotationDegrees
                                                )
                                                barcodeScanner.process(inputImage)
                                                    .addOnSuccessListener { barcodes ->
                                                        for (barcode in barcodes) {
                                                            barcode.rawValue?.let { value ->
                                                                val now = System.currentTimeMillis()
                                                                val prev = lastScannedTime.getAndSet(now)
                                                                if (prev == 0L || now - prev > 2000) {
                                                                    qrCallback(value)
                                                                }
                                                            }
                                                        }
                                                    }
                                                    .addOnCompleteListener {
                                                        try { imageProxy.close() } catch (_: Exception) {}
                                                    }
                                            } else {
                                                try { imageProxy.close() } catch (_: Exception) {}
                                            }
                                        }
                                    }

                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    imageAnalysis
                                )
                            } catch (e: Exception) {
                                errorCallback("Camera error: ${e.message}")
                            }
                        },
                        ContextCompat.getMainExecutor(ctx)
                    )
                } catch (e: Exception) {
                    errorCallback("Failed to initialize camera: ${e.message}")
                }

                previewView
            }
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopAppBar(
                title = { Text("Pair Device") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(48.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Point your camera at the QR code\nshown in the web app Settings page",
                color = Color.White, textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedButton(
                onClick = onPickFromGallery,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.6f))
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pick from Gallery")
            }
            Spacer(modifier = Modifier.weight(1f))
            if (error != null) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = { TextButton(onClick = onClearError) { Text("Dismiss") } }
                ) { Text(error) }
            }
            if (processing) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoPermissionContent(
    onBackClick: () -> Unit,
    onPickFromGallery: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Camera Permission") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Camera Permission Required", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "The app needs camera access to scan the pairing QR code.\nGrant camera access in Settings, or use the gallery option.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedButton(onClick = onPickFromGallery) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pick from Gallery Instead")
            }
        }
    }
}
