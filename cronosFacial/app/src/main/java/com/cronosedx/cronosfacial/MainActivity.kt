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
import com.cronosedx.cronosfacial.ui.CameraPreview
import com.cronosedx.cronosfacial.ui.theme.CronosFacialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material3.Button
import androidx.compose.ui.unit.dp
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cronosedx.cronosfacial.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity for the Cronos Facial Recognition app.
 * 
 * Handles camera permissions and displays the main screen with facial tracking.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
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
                    onRequestCameraPermission = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }
                )
            }
        }
    }
}

/**
 * Main screen composable.
 * 
 * Displays the camera preview, tracking controls, and current analysis results.
 */
@Composable
fun MainScreen(
    cameraPermissionGrantedState: Boolean,
    onRequestCameraPermission: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current

    // Show snackbar messages
    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar("Error: $it")
            viewModel.clearError()
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
                    viewModel.toggleTracking()
                },
                modifier = Modifier.padding(16.dp),
                enabled = (cameraPermissionGrantedState || !uiState.isTracking) && !uiState.isLoading
            ) {
                Text(if (uiState.isTracking) "Stop Tracking" else "Start Tracking")
            }
            
            // Display current emotion and engagement
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Emotion: ${uiState.latestEmotion}",
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Engagement: ${uiState.latestEngagement}",
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                if (uiState.isLoading) {
                    Text(
                        text = "Submitting data...",
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
            
            Box(modifier = Modifier.weight(1f)) {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    isTracking = uiState.isTracking && cameraPermissionGrantedState,
                    onFaceData = viewModel::onFaceData,
                    onEngagement = viewModel::onEngagement
                )
            }
        }
    }
}

