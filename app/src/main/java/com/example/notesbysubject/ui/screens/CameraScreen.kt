package com.example.notesbysubject.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.notesbysubject.ui.camera.LevelIndicator
import com.example.notesbysubject.appContainer
import com.example.notesbysubject.data.settings.AppSettings
import com.example.notesbysubject.ui.camera.PhotoStorage
import com.example.notesbysubject.ui.camera.ScanResultSheet
import com.example.notesbysubject.ui.camera.ScanState
import com.example.notesbysubject.ui.camera.TextScanner
import com.example.notesbysubject.ui.camera.applyLowPassFilter
import com.example.notesbysubject.ui.camera.calculateTiltAngle
import com.example.notesbysubject.ui.camera.filterRecognizedLines
import com.example.notesbysubject.ui.camera.isWithinTolerance
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun CameraScreen(
    modifier: Modifier = Modifier,
    projectId: Long? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasPermission) {
        CameraPreviewContent(
            context = context,
            lifecycleOwner = lifecycleOwner,
            projectId = projectId,
            modifier = modifier
        )
    } else {
        CameraPermissionDeniedContent(
            onRequestPermission = {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            },
            onOpenSettings = {
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", context.packageName, null)
                )
                context.startActivity(intent)
            },
            modifier = modifier
        )
    }
}

@Composable
private fun CameraPreviewContent(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    projectId: Long?,
    modifier: Modifier = Modifier
) {
    val imageCapture = remember { ImageCapture.Builder().build() }
    var savedMessage by remember { mutableStateOf<String?>(null) }
    var scanState by remember { mutableStateOf<ScanState?>(null) }
    val scope = rememberCoroutineScope()
    var capturing by remember { mutableStateOf(false) }

    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    }
    val accelerometer = remember(sensorManager) {
        sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    var currentAngle by remember { mutableFloatStateOf(0f) }
    val settings by context.appContainer.settings.settings.collectAsState(initial = AppSettings())
    val isLevel = isWithinTolerance(currentAngle, settings.levelTolerance)

    DisposableEffect(sensorManager, accelerometer) {
        if (sensorManager == null || accelerometer == null) {
            onDispose { }
        } else {
            var smoothedAngle = 0f
            var isFirstReading = true

            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                        val x = event.values[0]
                        val y = event.values[1]
                        val rawAngle = calculateTiltAngle(x, y)
                        if (isFirstReading) {
                            smoothedAngle = rawAngle
                            isFirstReading = false
                        } else {
                            smoothedAngle = applyLowPassFilter(rawAngle, smoothedAngle)
                        }
                        currentAngle = smoothedAngle
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            sensorManager.registerListener(
                listener,
                accelerometer,
                SensorManager.SENSOR_DELAY_UI
            )

            onDispose {
                sensorManager.unregisterListener(listener)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Level Indicator or missing accelerometer message over camera preview
        if (accelerometer != null) {
            LevelIndicator(
                angleDegrees = currentAngle,
                isLevel = isLevel,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Level not available on this device",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
        }

        // Shutter Button & Scan Button Controls
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shutter Button
            IconButton(
                enabled = !capturing,
                onClick = capture@{
                    val photoFile = try { PhotoStorage.createPhotoFile(context, projectId) }
                    catch (_: Exception) {
                        Toast.makeText(context, "Could not create a photo file. Check available storage.", Toast.LENGTH_LONG).show()
                        return@capture
                    }
                    capturing = true
                    val outputOptions = PhotoStorage.createOutputFileOptions(photoFile)
                    imageCapture.takePicture(
                        outputOptions,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                capturing = false
                                registerPhoto(context, photoFile, projectId)
                                val msg = "Saved ${photoFile.name}"
                                savedMessage = msg
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }

                            override fun onError(exception: ImageCaptureException) {
                                capturing = false
                                photoFile.delete()
                                Toast.makeText(
                                    context,
                                    "Error taking photo: ${exception.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                },
                modifier = Modifier
                    .size(72.dp)
                    .background(Color.White, shape = CircleShape)
                    .border(4.dp, MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                )
            }

            // Scan Button
            Button(
                enabled = !capturing,
                onClick = scan@{
                    val photoFile = try { PhotoStorage.createPhotoFile(context, projectId) }
                    catch (_: Exception) {
                        scanState = ScanState.Error
                        return@scan
                    }
                    capturing = true
                    scanState = ScanState.Loading
                    val outputOptions = PhotoStorage.createOutputFileOptions(photoFile)
                    imageCapture.takePicture(
                        outputOptions,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                capturing = false
                                registerPhoto(context, photoFile, projectId)
                                val msg = "Saved ${photoFile.name}"
                                savedMessage = msg
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

                                scope.launch {
                                    try {
                                        val rawLines = TextScanner.scanImage(context, photoFile)
                                        if (rawLines.isEmpty()) {
                                            scanState = ScanState.NoTextFound
                                        } else {
                                            val filteredLines = filterRecognizedLines(rawLines)
                                            if (filteredLines.isEmpty()) {
                                                scanState = ScanState.TextBelowThreshold
                                            } else {
                                                scanState = ScanState.Success(filteredLines)
                                            }
                                        }
                                    } catch (_: Exception) {
                                        scanState = ScanState.Error
                                    }
                                }
                            }

                            override fun onError(exception: ImageCaptureException) {
                                capturing = false
                                photoFile.delete()
                                scanState = ScanState.Error
                                Toast.makeText(
                                    context,
                                    "Error taking photo: ${exception.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                },
                modifier = Modifier.height(48.dp)
            ) {
                Text("Scan")
            }
        }

        // Overlay message when photo is saved
        savedMessage?.let { msg ->
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 32.dp, start = 16.dp, end = 16.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Bottom Sheet for Scan Results
        scanState?.let { state ->
            ScanResultSheet(
                state = state,
                onDismiss = { scanState = null }
            )
        }
    }
}

@Composable
private fun CameraPermissionDeniedContent(
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Camera Permission Required",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "The camera is needed to capture photos for your projects and notes.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRequestPermission,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Grant Permission")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onOpenSettings,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Open App Settings")
        }
    }
}

@Composable
fun Camera(
    modifier: Modifier = Modifier
) {
    CameraScreen(modifier = modifier)
}

private fun registerPhoto(context: Context, photo: File, projectId: Long?) {
    val app = context.appContainer
    app.applicationScope.launch {
        try { app.projects.addPhoto(photo, projectId, app.notesDirectory) }
        catch (_: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Photo saved. Reload saved photos to add it to the list.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
