package com.cronosedx.cronosfacial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.cronosedx.cronosfacial.ui.CameraPreview
import com.cronosedx.cronosfacial.ui.theme.CronosFacialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material3.Button
import androidx.compose.ui.unit.dp
import com.cronosedx.cronosfacial.ui.FaceSessionData
import com.cronosedx.cronosfacial.network.RetrofitInstance
import com.cronosedx.cronosfacial.network.AiMlService
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.cronosedx.cronosfacial.model.EngagementState
import com.cronosedx.cronosfacial.network.EngagementData
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.rememberCoroutineScope

class MainActivity : ComponentActivity() {
    private val aiMlService = AiMlService()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // State to communicate permission result to Compose
        val cameraPermissionGrantedState = mutableStateOf(false)
        val cameraPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            cameraPermissionGrantedState.value = granted
        }

        setContent {
            CronosFacialTheme {
                MainScreen(
                    cameraPermissionGrantedState = cameraPermissionGrantedState.value,
                    onRequestCameraPermission = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                    aiMlService = aiMlService
                )
            }
        }
    }
}

@Composable
fun MainScreen(
    cameraPermissionGrantedState: Boolean,
    onRequestCameraPermission: () -> Unit,
    aiMlService: AiMlService
) {
    val isTracking = remember { mutableStateOf(false) }
    val sessionData = remember { mutableStateOf<List<FaceSessionData>>(emptyList()) }
    val engagementStates = remember { mutableStateOf<List<Pair<Long, EngagementState>>>(emptyList()) }
    val latestEngagement = engagementStates.value.lastOrNull()?.second ?: EngagementState.Unknown
    val latestEmotion = remember { mutableStateOf("Neutral") }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (granted != cameraPermissionGrantedState) {
            // Update state if system permission and state are out of sync
            onRequestCameraPermission()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Button(
                onClick = {
                    if (!cameraPermissionGrantedState) {
                        onRequestCameraPermission()
                        return@Button
                    }
                    if (!isTracking.value) {
                        // Starting new session: clear previous data and start AI/ML session
                        sessionData.value = emptyList()
                        engagementStates.value = emptyList()
                        latestEmotion.value = "Neutral"
                        aiMlService.startSession()
                    } else {
                        // Stopping session: submit data to both legacy API and AI/ML backend
                        coroutineScope.launch {
                            try {
                                // Submit to legacy API
                                val dataToSend = sessionData.value
                                val engagementToSend = engagementStates.value.map { EngagementData(it.first, it.second.name) }
                                val response = RetrofitInstance.api.submitFaceSession(dataToSend)
                                val engagementResponse = RetrofitInstance.api.submitEngagement(engagementToSend)
                                
                                // Submit to AI/ML backend
                                val aiMlResult = aiMlService.endSession()
                                
                                if (response.isSuccessful && engagementResponse.isSuccessful) {
                                    val message = if (aiMlResult != null) {
                                        "Session data submitted successfully! AI analysis: ${aiMlResult.sessionInsights.averageEngagement}% engagement"
                                    } else {
                                        "Session data submitted successfully!"
                                    }
                                    snackbarHostState.showSnackbar(message)
                                } else {
                                    snackbarHostState.showSnackbar("Failed to submit session data.")
                                }
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Network error: ${e.localizedMessage}")
                            }
                        }
                    }
                    isTracking.value = !isTracking.value
                },
                modifier = Modifier.padding(16.dp),
                enabled = cameraPermissionGrantedState || !isTracking.value
            ) {
                Text(if (isTracking.value) "Stop Tracking" else "Start Tracking")
            }
            
            // Display current emotion and engagement
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Emotion: ${latestEmotion.value}",
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Engagement: $latestEngagement",
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            Box(modifier = Modifier.weight(1f)) {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    isTracking = isTracking.value && cameraPermissionGrantedState,
                    onFaceData = { data ->
                        if (isTracking.value) {
                            sessionData.value = sessionData.value + data
                            latestEmotion.value = data.emotion
                            
                            // Send to AI/ML backend in real-time
                            coroutineScope.launch {
                                try {
                                    // Convert FaceSessionData to FacialLandmark format
                                    val landmarks = listOf(
                                        com.cronosedx.cronosfacial.network.FacialLandmark(
                                            x = 0.5f, y = 0.5f, z = 0.0f,
                                            confidence = 0.8f,
                                            landmarkType = "face_center"
                                        )
                                    )
                                    
                                    aiMlService.streamFacialData(
                                        landmarks = landmarks,
                                        emotion = data.emotion,
                                        gaze = data.gaze,
                                        engagement = data.engagement,
                                        confidence = 0.8f
                                    )
                                } catch (e: Exception) {
                                    // Log error but don't crash the app
                                    android.util.Log.e("MainActivity", "Error streaming to AI/ML", e)
                                }
                            }
                        }
                    },
                    onEngagement = { engagement ->
                        if (isTracking.value) {
                            engagementStates.value = engagementStates.value + (System.currentTimeMillis() to engagement)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CronosFacialTheme {
        Greeting("Android")
    }
}