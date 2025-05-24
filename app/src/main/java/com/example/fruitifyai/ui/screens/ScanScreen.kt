package com.example.fruitfreshdetector.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.airbnb.lottie.compose.*
import com.example.fruitifyai.R
import com.example.fruitifyai.classifier.BananaFreshnessClassifier
import com.example.fruitifyai.classifier.FruitClassifier
import com.example.fruitifyai.classifier.toBitmap1
import java.util.concurrent.Executors

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ScanScreen(
    modifier: Modifier = Modifier,
    onPrediction: (fruitName: String, freshnessStatus: String?, confidence: Float) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val freshnessClassifier = remember { BananaFreshnessClassifier(context) }
    val fruitClassifier = remember { FruitClassifier(context) }

    var isFlashOn by remember { mutableStateOf(false) }
    var latestFrameBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // Optional: handle gallery input
    }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.scan_animation))

    Scaffold(
        containerColor = Color.Black,
        contentWindowInsets = WindowInsets(0),
        content = {
            Box(modifier = modifier.fillMaxSize()) {

                // 📷 Camera Preview
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }

                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()

                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val imageAnalyzer = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .also {
                                    it.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                                        latestFrameBitmap = imageProxy.toBitmap1()
                                        imageProxy.close()
                                    }
                                }

                            try {
                                cameraProvider.unbindAll()
                                val camera = cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    imageAnalyzer
                                )
                                camera.cameraControl.enableTorch(isFlashOn)
                            } catch (e: Exception) {
                                Log.e("ScanScreen", "Camera binding failed", e)
                            }

                        }, ContextCompat.getMainExecutor(ctx))

                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // 🎞️ Center Animation
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LottieAnimation(
                        composition = composition,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier.size(300.dp)
                    )
                }

                // 🎛 Bottom Controls
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 48.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 📁 Gallery Button
                        IconButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.DarkGray)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Upload from gallery",
                                tint = Color.White
                            )
                        }

                        // 🔦 Flash Toggle
                        IconButton(
                            onClick = {
                                isFlashOn = !isFlashOn
                                val cameraProvider = ProcessCameraProvider.getInstance(context).get()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA
                                )?.cameraControl?.enableTorch(isFlashOn)
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.DarkGray)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Flash Toggle",
                                tint = if (isFlashOn) Color.Yellow else Color.White
                            )
                        }
                    }

                    // 📸 Capture Button
                    IconButton(
                        onClick = {
                            latestFrameBitmap?.let { bitmap ->
                                val (fruitName, confidence) = fruitClassifier.predictWithConfidence(bitmap)
                                val confidenceThreshold = 0.7f

                                if (confidence < confidenceThreshold) {
                                    onPrediction("Unknown", null, confidence)
                                } else {
                                    if (fruitName.equals("Banana", ignoreCase = true)) {
                                        val freshnessScore = freshnessClassifier.predict(bitmap)
                                        val freshnessStatus = if (freshnessScore < 0.5f) "Fresh" else "Rotten"
                                        onPrediction(fruitName, freshnessStatus, confidence)
                                    } else {
                                        onPrediction(fruitName, null, confidence)
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(Color.Green)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Capture Image",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    )
}