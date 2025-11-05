package com.cronosedx.cronosfacial.repository

import com.cronosedx.cronosfacial.common.Result
import com.cronosedx.cronosfacial.model.EngagementState
import com.cronosedx.cronosfacial.network.*
import com.cronosedx.cronosfacial.ui.FaceSessionData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing facial analysis data operations.
 * 
 * This class abstracts the data layer and provides a clean API for the ViewModel
 * to interact with both the legacy API and the AI/ML backend.
 */
@Singleton
class FacialAnalysisRepository @Inject constructor(
    private val apiService: ApiService,
    private val aiMlService: AiMlService
) {
    
    /**
     * Submit face session data to the legacy API
     * 
     * @param sessionData List of face session data points
     * @return Result indicating success or failure
     */
    suspend fun submitFaceSession(sessionData: List<FaceSessionData>): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.submitFaceSession(sessionData)
                if (response.isSuccessful) {
                    Result.Success("Session data submitted successfully")
                } else {
                    Result.Error(
                        Exception("HTTP ${response.code()}"),
                        "Failed to submit session data"
                    )
                }
            } catch (e: Exception) {
                Result.Error(e, "Network error: ${e.localizedMessage}")
            }
        }
    }
    
    /**
     * Submit engagement data to the legacy API
     * 
     * @param engagementStates List of engagement states with timestamps
     * @return Result indicating success or failure
     */
    suspend fun submitEngagement(
        engagementStates: List<Pair<Long, EngagementState>>
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val engagementData = engagementStates.map { 
                    EngagementData(it.first, it.second.name) 
                }
                val response = apiService.submitEngagement(engagementData)
                if (response.isSuccessful) {
                    Result.Success("Engagement data submitted successfully")
                } else {
                    Result.Error(
                        Exception("HTTP ${response.code()}"),
                        "Failed to submit engagement data"
                    )
                }
            } catch (e: Exception) {
                Result.Error(e, "Network error: ${e.localizedMessage}")
            }
        }
    }
    
    /**
     * Start a new AI/ML analysis session
     * 
     * @return Session ID
     */
    fun startAiMlSession(): String {
        return aiMlService.startSession()
    }
    
    /**
     * Stream facial data to AI/ML backend in real-time
     * 
     * @param landmarks List of facial landmarks
     * @param emotion Detected emotion
     * @param gaze Estimated gaze direction
     * @param engagement Current engagement state
     * @param confidence Confidence score
     */
    suspend fun streamFacialData(
        landmarks: List<FacialLandmark>,
        emotion: String,
        gaze: String,
        engagement: EngagementState,
        confidence: Float
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                aiMlService.streamFacialData(landmarks, emotion, gaze, engagement, confidence)
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e, "Failed to stream facial data")
            }
        }
    }
    
    /**
     * End the current AI/ML session and get final analysis
     * 
     * @return Result with batch analysis or error
     */
    suspend fun endAiMlSession(): Result<BatchAnalysisResult?> {
        return withContext(Dispatchers.IO) {
            try {
                val result = aiMlService.endSession()
                Result.Success(result)
            } catch (e: Exception) {
                Result.Error(e, "Failed to end AI/ML session")
            }
        }
    }
    
    /**
     * Submit all session data to both legacy and AI/ML backends
     * 
     * @param sessionData Face session data
     * @param engagementStates Engagement states
     * @return Combined result message
     */
    suspend fun submitAllSessionData(
        sessionData: List<FaceSessionData>,
        engagementStates: List<Pair<Long, EngagementState>>
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Submit to legacy API
                val sessionResult = submitFaceSession(sessionData)
                val engagementResult = submitEngagement(engagementStates)
                
                // Submit to AI/ML backend
                val aiMlResult = endAiMlSession()
                
                // Check if both succeeded
                if (sessionResult.isSuccess && engagementResult.isSuccess) {
                    val message = when (val aiResult = aiMlResult.getOrNull()) {
                        null -> "Session data submitted successfully!"
                        else -> "Session data submitted successfully! AI analysis: ${aiResult.sessionInsights.averageEngagement}% engagement"
                    }
                    Result.Success(message)
                } else {
                    Result.Error(
                        Exception("Submission failed"),
                        "Failed to submit session data"
                    )
                }
            } catch (e: Exception) {
                Result.Error(e, "Error submitting session data: ${e.localizedMessage}")
            }
        }
    }
}
