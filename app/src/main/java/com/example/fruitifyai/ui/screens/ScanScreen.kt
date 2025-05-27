package com.example.fruitfreshdetector.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import com.example.fruitifyai.data.DatabaseProvider
import com.example.fruitifyai.data.ScanResultEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.example.fruitifyai.R
import com.example.fruitifyai.classifier.BananaFreshnessClassifier
import com.example.fruitifyai.classifier.FruitClassifier
import com.example.fruitifyai.classifier.toBitmap1
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ScanScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    onPrediction: (fruitName: String, freshnessStatus: String?, confidence: Float) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val freshnessClassifier = remember { BananaFreshnessClassifier(context) }
    val fruitClassifier = remember { FruitClassifier(context) }

    var isFlashOn by remember { mutableStateOf(false) }
    var latestFrameBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // Optional: handle selected gallery image
    }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.scan_animation))

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                modifier = Modifier.statusBarsPadding()
            )
        },
        contentWindowInsets = WindowInsets(0),
        content = {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                // Camera Preview
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
                                cameraControl = camera.cameraControl
                                cameraControl?.enableTorch(isFlashOn)
                            } catch (e: Exception) {
                                Log.e("ScanScreen", "Camera binding failed", e)
                            }

                        }, ContextCompat.getMainExecutor(ctx))

                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Scan animation in center
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

                // Bottom controls
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 16.dp)
                        .navigationBarsPadding(),
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
                        // Gallery button
                        IconButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.DarkGray)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.gallary ,  ),
                                contentDescription = "Upload from gallery",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        // Flash toggle
                        IconButton(
                            onClick = {
                                isFlashOn = !isFlashOn
                                cameraControl?.enableTorch(isFlashOn)
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.DarkGray)
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = if (isFlashOn) R.drawable.flash_of else R.drawable.flash_on
                                ),
                                contentDescription = "Flash Toggle",
                                tint = if (isFlashOn) Color.Yellow else Color.White,
                                modifier = Modifier.size(24.dp) // optional: set icon size
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Capture button
                    IconButton(
                        onClick = {
                            latestFrameBitmap?.let { bitmap ->
                                val (fruitName, confidence) = fruitClassifier.predictWithConfidence(bitmap)
                                val confidenceThreshold = 0.7f

                                val db = DatabaseProvider.getDatabase(context)
                                val dao = db.scanResultDao()

                                if (confidence < confidenceThreshold) {
                                    // Insert unknown result
                                    CoroutineScope(Dispatchers.IO).launch {
                                        dao.insertScanResult(
                                            ScanResultEntity(
                                                fruitName = "Unknown",
                                                freshness = null,
                                                confidence = confidence,
                                                timestamp = System.currentTimeMillis()
                                            )
                                        )
                                    }

                                    onPrediction("Unknown", null, confidence)
                                } else {
                                    if (fruitName.equals("Banana", ignoreCase = true)) {
                                        val freshnessScore = freshnessClassifier.predict(bitmap)
                                        val freshnessStatus = if (freshnessScore < 0.5f) "Fresh" else "Rotten"

                                        // Insert banana result
                                        CoroutineScope(Dispatchers.IO).launch {
                                            dao.insertScanResult(
                                                ScanResultEntity(
                                                    fruitName = "Banana",
                                                    freshness = freshnessStatus,
                                                    confidence = confidence,
                                                    timestamp = System.currentTimeMillis()
                                                )
                                            )
                                        }

                                        onPrediction(fruitName, freshnessStatus, confidence)
                                    } else {
                                        // Insert non-banana fruit
                                        CoroutineScope(Dispatchers.IO).launch {
                                            dao.insertScanResult(
                                                ScanResultEntity(
                                                    fruitName = fruitName,
                                                    freshness = null,
                                                    confidence = confidence,
                                                    timestamp = System.currentTimeMillis()
                                                )
                                            )
                                        }

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
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Capture Image",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    )
}