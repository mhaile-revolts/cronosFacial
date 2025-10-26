package com.cronosedx.cronosfacial.network

import com.cronosedx.cronosfacial.ui.FaceSessionData
import com.cronosedx.cronosfacial.model.EngagementState
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Headers

interface ApiService {
    @POST("face-sessions")
    suspend fun submitFaceSession(@Body sessionData: List<FaceSessionData>): Response<Unit>
    
    @POST("engagement")
    suspend fun submitEngagement(@Body engagementData: List<EngagementData>): Response<Unit>
    
    // New endpoints for AI/ML processing
    @POST("facial-analysis/stream")
    @Headers("Content-Type: application/json")
    suspend fun streamFacialData(@Body facialData: FacialAnalysisData): Response<AnalysisResult>
    
    @POST("facial-analysis/batch")
    @Headers("Content-Type: application/json")
    suspend fun submitBatchAnalysis(@Body batchData: BatchFacialData): Response<BatchAnalysisResult>
    
    @POST("ml-predictions/engagement")
    suspend fun getEngagementPrediction(@Body predictionRequest: EngagementPredictionRequest): Response<EngagementPrediction>
}

data class EngagementData(
    val timestamp: Long,
    val state: String
)

// Enhanced data structures for AI/ML
data class FacialAnalysisData(
    val sessionId: String,
    val timestamp: Long,
    val landmarks: List<FacialLandmark>,
    val emotions: EmotionData,
    val gaze: GazeData,
    val engagement: EngagementState,
    val confidence: Float,
    val metadata: Map<String, Any>
)

data class FacialLandmark(
    val x: Float,
    val y: Float,
    val z: Float,
    val confidence: Float,
    val landmarkType: String
)

data class EmotionData(
    val primaryEmotion: String,
    val confidence: Float,
    val emotionScores: Map<String, Float>
)

data class GazeData(
    val direction: String, // "left", "right", "center", "up", "down"
    val confidence: Float,
    val eyeOpenness: Float
)

data class AnalysisResult(
    val success: Boolean,
    val insights: List<String>,
    val recommendations: List<String>,
    val nextAction: String?
)

data class BatchFacialData(
    val sessionId: String,
    val startTime: Long,
    val endTime: Long,
    val dataPoints: List<FacialAnalysisData>
)

data class BatchAnalysisResult(
    val success: Boolean,
    val sessionInsights: SessionInsights,
    val trends: List<TrendData>
)

data class SessionInsights(
    val averageEngagement: Float,
    val engagementTrend: String, // "increasing", "decreasing", "stable"
    val attentionSpans: List<Long>,
    val distractionEvents: Int
)

data class TrendData(
    val metric: String,
    val values: List<Float>,
    val timestamps: List<Long>
)

data class EngagementPredictionRequest(
    val historicalData: List<FacialAnalysisData>,
    val currentContext: String,
    val timeWindow: Long
)

data class EngagementPrediction(
    val predictedEngagement: EngagementState,
    val confidence: Float,
    val factors: List<String>,
    val recommendations: List<String>
) 