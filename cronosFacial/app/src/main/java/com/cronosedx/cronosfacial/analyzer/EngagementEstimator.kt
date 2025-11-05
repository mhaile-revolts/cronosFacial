package com.cronosedx.cronosfacial.analyzer

import android.util.Log
import com.cronosedx.cronosfacial.model.EngagementState
import com.cronosedx.cronosfacial.model.InteractionData

/**
 * Estimates user engagement based on facial analysis data.
 * 
 * Combines emotion, gaze direction, and interaction data to calculate
 * an engagement score and state.
 */
class EngagementEstimator {
    private val TAG = "EngagementEstimator"
    
    // Engagement score weights
    private val EMOTION_WEIGHT = 0.4f
    private val GAZE_WEIGHT = 0.4f
    private val INTERACTION_WEIGHT = 0.2f
    
    // Engagement thresholds
    private val HIGH_THRESHOLD = 0.7f
    private val MEDIUM_THRESHOLD = 0.4f
    
    /**
     * Estimates engagement level based on multiple factors.
     * 
     * @param emotion Detected emotion
     * @param gaze Estimated gaze direction
     * @param interaction Optional interaction data
     * @return Estimated engagement state
     */
    fun estimateEngagement(
        emotion: String, 
        gaze: String, 
        interaction: InteractionData? = null
    ): EngagementState {
        Log.d(TAG, "Estimating engagement: emotion=$emotion, gaze=$gaze, interaction=$interaction")
        
        // Calculate individual scores
        val emotionScore = calculateEmotionScore(emotion)
        val gazeScore = calculateGazeScore(gaze)
        val interactionScore = calculateInteractionScore(interaction)
        
        // Calculate weighted engagement score
        val engagementScore = (
            emotionScore * EMOTION_WEIGHT +
            gazeScore * GAZE_WEIGHT +
            interactionScore * INTERACTION_WEIGHT
        )
        
        Log.d(TAG, "Engagement scores - Emotion: $emotionScore, Gaze: $gazeScore, " +
                "Interaction: $interactionScore, Total: $engagementScore")
        
        // Map score to engagement state
        return when {
            engagementScore >= HIGH_THRESHOLD -> EngagementState.High
            engagementScore >= MEDIUM_THRESHOLD -> EngagementState.Medium
            else -> EngagementState.Low
        }
    }
    
    /**
     * Calculate engagement score from emotion (0-1).
     * 
     * Positive emotions indicate higher engagement.
     */
    private fun calculateEmotionScore(emotion: String): Float {
        return when (emotion.lowercase()) {
            "happy" -> 1.0f
            "surprise" -> 0.9f
            "neutral" -> 0.6f
            "angry" -> 0.4f
            "fear" -> 0.3f
            "sad" -> 0.2f
            "disgust" -> 0.1f
            else -> 0.5f // Unknown emotion
        }
    }
    
    /**
     * Calculate engagement score from gaze direction (0-1).
     * 
     * Center gaze indicates highest engagement.
     */
    private fun calculateGazeScore(gaze: String): Float {
        return when (gaze.lowercase()) {
            "center" -> 1.0f
            "left", "right" -> 0.6f
            "up" -> 0.5f
            "down" -> 0.3f
            "up-left", "up-right" -> 0.5f
            "down-left", "down-right" -> 0.3f
            "unknown" -> 0.4f
            else -> 0.5f
        }
    }
    
    /**
     * Calculate engagement score from interaction data (0-1).
     * 
     * Active interaction indicates higher engagement.
     */
    private fun calculateInteractionScore(interaction: InteractionData?): Float {
        if (interaction == null) {
            return 0.5f // Neutral score when no interaction data
        }
        
        // In a real implementation, analyze:
        // - Time since last interaction
        // - Interaction frequency
        // - Type of interaction
        return 0.7f // Simulate moderate interaction engagement
    }
    
    /**
     * Get detailed engagement metrics.
     * 
     * @return Map of metric names to scores
     */
    fun getEngagementMetrics(
        emotion: String,
        gaze: String,
        interaction: InteractionData? = null
    ): Map<String, Float> {
        return mapOf(
            "emotionScore" to calculateEmotionScore(emotion),
            "gazeScore" to calculateGazeScore(gaze),
            "interactionScore" to calculateInteractionScore(interaction),
            "overallScore" to (
                calculateEmotionScore(emotion) * EMOTION_WEIGHT +
                calculateGazeScore(gaze) * GAZE_WEIGHT +
                calculateInteractionScore(interaction) * INTERACTION_WEIGHT
            )
        )
    }
}
