package com.cronosedx.cronosfacial.ui

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.cronosedx.cronosfacial.analyzer.EmotionAnalyzer
import com.cronosedx.cronosfacial.analyzer.GazeEstimator
import com.cronosedx.cronosfacial.analyzer.EngagementEstimator
import com.cronosedx.cronosfacial.model.EngagementState
import com.cronosedx.cronosfacial.model.InteractionData
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

data class FaceSessionData(
    val timestamp: Long,
    val emotion: String,
    val gaze: String,
    val engagement: EngagementState
)

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    isTracking: Boolean = false,
    onFaceData: (FaceSessionData) -> Unit = {},
    onEngagement: (EngagementState) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val handler = remember { android.os.Handler(android.os.Looper.getMainLooper()) }
    val isRunning = remember { mutableStateOf(false) }

    DisposableEffect(isTracking) {
        if (isTracking && !isRunning.value) {
            isRunning.value = true
            val runnable = object : Runnable {
                override fun run() {
                    if (isRunning.value) {
                        val bitmap = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888)
                        processImage(bitmap, context, onFaceData, onEngagement)
                        bitmap.recycle()
                        handler.postDelayed(this, 500) // 500ms interval
                    }
                }
            }
            handler.post(runnable)
        } else if (!isTracking && isRunning.value) {
            isRunning.value = false
            handler.removeCallbacksAndMessages(null)
        }
        onDispose {
            isRunning.value = false
            handler.removeCallbacksAndMessages(null)
            cameraExecutor.shutdown()
        }
    }

    Box(modifier = modifier) {
        if (isTracking) {
            // Show a placeholder or camera preview if needed
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Simulated Camera Preview - Tracking...")
            }
        } else {
            // Show placeholder when not tracking
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Camera Preview - Click 'Start Tracking' to begin")
            }
        }
    }
}

private fun startCamera(
    context: Context,
    previewView: PreviewView,
    lifecycleOwner: LifecycleOwner,
    cameraExecutor: ExecutorService,
    isTracking: Boolean,
    onFaceData: (FaceSessionData) -> Unit,
    onEngagement: (EngagementState) -> Unit
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    
    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()
        
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        
        val imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { analysis ->
                analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    if (isTracking) {
                        // Simplified analysis - just create a mock bitmap and process it
                        val bitmap = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888)
                        processImage(bitmap, context, onFaceData, onEngagement)
                        bitmap.recycle()
                    }
                    imageProxy.close()
                }
            }
        
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_FRONT_CAMERA,
                preview,
                imageAnalyzer
            )
        } catch (e: Exception) {
            Log.e("CameraPreview", "Camera binding failed", e)
        }
    }, ContextCompat.getMainExecutor(context))
}

private fun processImage(
    bitmap: Bitmap,
    context: Context,
    onFaceData: (FaceSessionData) -> Unit,
    onEngagement: (EngagementState) -> Unit
) {
    val emotionAnalyzer = EmotionAnalyzer()
    val gazeEstimator = GazeEstimator()
    val engagementEstimator = EngagementEstimator()
    val faceMeshAnalyzer = FaceMeshAnalyzer(context, emotionAnalyzer, gazeEstimator, engagementEstimator)
    
    val engagement = faceMeshAnalyzer.processFrame(bitmap)
    engagement?.let { state ->
        onEngagement(state)
        
        // Use real emotion detection instead of hardcoded "neutral"
        val landmarks = FloatArray(468) { 0.5f } // Simulated landmarks for now
        val detectedEmotion = emotionAnalyzer.predictEmotion(landmarks)
        val detectedGaze = gazeEstimator.estimateGaze(landmarks)
        
        val faceData = FaceSessionData(
            timestamp = System.currentTimeMillis(),
            emotion = detectedEmotion, // Use detected emotion
            gaze = detectedGaze, // Use detected gaze
            engagement = state
        )
        onFaceData(faceData)
        
        // Log for debugging
        android.util.Log.d("CameraPreview", "Detected emotion: $detectedEmotion, gaze: $detectedGaze, engagement: $state")
    }
} 