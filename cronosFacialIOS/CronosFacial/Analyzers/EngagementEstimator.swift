//
//  EngagementEstimator.swift
//  CronosFacial
//
//  Engagement level estimation from emotion and gaze data
//

import Foundation

/// Estimates user engagement based on facial analysis
class EngagementEstimator {
    // Engagement score weights
    private let emotionWeight: Float = 0.4
    private let gazeWeight: Float = 0.4
    private let interactionWeight: Float = 0.2
    
    // Engagement thresholds
    private let highThreshold: Float = 0.7
    private let mediumThreshold: Float = 0.4
    
    /// Estimates engagement level from emotion, gaze, and interaction data
    /// - Parameters:
    ///   - emotion: Detected emotion
    ///   - gaze: Estimated gaze direction
    ///   - interaction: Optional interaction data
    /// - Returns: Estimated engagement state
    func estimateEngagement(
        emotion: String,
        gaze: String,
        interaction: InteractionData? = nil
    ) -> EngagementState {
        print("[EngagementEstimator] Estimating engagement: emotion=\(emotion), gaze=\(gaze), interaction=\(String(describing: interaction))")
        
        // Calculate individual scores
        let emotionScore = calculateEmotionScore(emotion: emotion)
        let gazeScore = calculateGazeScore(gaze: gaze)
        let interactionScore = calculateInteractionScore(interaction: interaction)
        
        // Calculate weighted engagement score
        let engagementScore = (
            emotionScore * emotionWeight +
            gazeScore * gazeWeight +
            interactionScore * interactionWeight
        )
        
        print("[EngagementEstimator] Engagement scores - Emotion: \(emotionScore), Gaze: \(gazeScore), Interaction: \(interactionScore), Total: \(engagementScore)")
        
        // Map score to engagement state
        if engagementScore >= highThreshold {
            return .high
        } else if engagementScore >= mediumThreshold {
            return .medium
        } else {
            return .low
        }
    }
    
    /// Calculate engagement score from emotion (0-1)
    private func calculateEmotionScore(emotion: String) -> Float {
        switch emotion.lowercased() {
        case "happy":
            return 1.0
        case "surprise":
            return 0.9
        case "neutral":
            return 0.6
        case "angry":
            return 0.4
        case "fear":
            return 0.3
        case "sad":
            return 0.2
        case "disgust":
            return 0.1
        default:
            return 0.5 // Unknown emotion
        }
    }
    
    /// Calculate engagement score from gaze direction (0-1)
    private func calculateGazeScore(gaze: String) -> Float {
        switch gaze.lowercased() {
        case "center":
            return 1.0
        case "left", "right":
            return 0.6
        case "up":
            return 0.5
        case "down":
            return 0.3
        case "up-left", "up-right":
            return 0.5
        case "down-left", "down-right":
            return 0.3
        case "unknown":
            return 0.4
        default:
            return 0.5
        }
    }
    
    /// Calculate engagement score from interaction data (0-1)
    private func calculateInteractionScore(interaction: InteractionData?) -> Float {
        guard let interaction = interaction else {
            return 0.5 // Neutral score when no interaction data
        }
        
        // In a real implementation, analyze:
        // - Time since last interaction
        // - Interaction frequency
        // - Type of interaction
        return 0.7 // Simulate moderate interaction engagement
    }
    
    /// Get detailed engagement metrics
    /// - Parameters:
    ///   - emotion: Detected emotion
    ///   - gaze: Estimated gaze direction
    ///   - interaction: Optional interaction data
    /// - Returns: Dictionary of metric names to scores
    func getEngagementMetrics(
        emotion: String,
        gaze: String,
        interaction: InteractionData? = nil
    ) -> [String: Float] {
        let emotionScore = calculateEmotionScore(emotion: emotion)
        let gazeScore = calculateGazeScore(gaze: gaze)
        let interactionScore = calculateInteractionScore(interaction: interaction)
        
        return [
            "emotionScore": emotionScore,
            "gazeScore": gazeScore,
            "interactionScore": interactionScore,
            "overallScore": (
                emotionScore * emotionWeight +
                gazeScore * gazeWeight +
                interactionScore * interactionWeight
            )
        ]
    }
}
