package com.cronosedx.cronosfacial.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cronosedx.cronosfacial.common.Result
import com.cronosedx.cronosfacial.model.EngagementState
import com.cronosedx.cronosfacial.network.FacialLandmark
import com.cronosedx.cronosfacial.repository.FacialAnalysisRepository
import com.cronosedx.cronosfacial.ui.FaceSessionData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the main screen
 */
data class MainUiState(
    val isTracking: Boolean = false,
    val sessionData: List<FaceSessionData> = emptyList(),
    val engagementStates: List<Pair<Long, EngagementState>> = emptyList(),
    val latestEmotion: String = "Neutral",
    val latestEngagement: EngagementState = EngagementState.Unknown,
    val message: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for the main screen.
 * 
 * Manages the state and business logic for facial analysis sessions,
 * including camera tracking, data collection, and network submissions.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: FacialAnalysisRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    /**
     * Start a new tracking session
     */
    fun startTracking() {
        _uiState.value = _uiState.value.copy(
            isTracking = true,
            sessionData = emptyList(),
            engagementStates = emptyList(),
            latestEmotion = "Neutral",
            latestEngagement = EngagementState.Unknown,
            message = null,
            error = null
        )
        
        // Start AI/ML session
        repository.startAiMlSession()
    }
    
    /**
     * Stop the current tracking session and submit data
     */
    fun stopTracking() {
        _uiState.value = _uiState.value.copy(
            isTracking = false,
            isLoading = true
        )
        
        viewModelScope.launch {
            val result = repository.submitAllSessionData(
                _uiState.value.sessionData,
                _uiState.value.engagementStates
            )
            
            when (result) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        message = result.data,
                        isLoading = false,
                        error = null
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.message ?: result.exception.localizedMessage,
                        isLoading = false
                    )
                }
                is Result.Loading -> {
                    // Already in loading state
                }
            }
        }
    }
    
    /**
     * Toggle tracking on/off
     */
    fun toggleTracking() {
        if (_uiState.value.isTracking) {
            stopTracking()
        } else {
            startTracking()
        }
    }
    
    /**
     * Handle new face data from camera
     */
    fun onFaceData(data: FaceSessionData) {
        if (!_uiState.value.isTracking) return
        
        _uiState.value = _uiState.value.copy(
            sessionData = _uiState.value.sessionData + data,
            latestEmotion = data.emotion
        )
        
        // Stream to AI/ML backend
        viewModelScope.launch {
            val landmarks = listOf(
                FacialLandmark(
                    x = 0.5f, y = 0.5f, z = 0.0f,
                    confidence = 0.8f,
                    landmarkType = "face_center"
                )
            )
            
            repository.streamFacialData(
                landmarks = landmarks,
                emotion = data.emotion,
                gaze = data.gaze,
                engagement = data.engagement,
                confidence = 0.8f
            )
        }
    }
    
    /**
     * Handle new engagement state
     */
    fun onEngagement(engagement: EngagementState) {
        if (!_uiState.value.isTracking) return
        
        val timestamp = System.currentTimeMillis()
        _uiState.value = _uiState.value.copy(
            engagementStates = _uiState.value.engagementStates + (timestamp to engagement),
            latestEngagement = engagement
        )
    }
    
    /**
     * Clear any displayed message
     */
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
    
    /**
     * Clear any displayed error
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
