//
//  Models.swift
//  CronosFacial
//
//  Data models for facial analysis
//

import Foundation

// MARK: - Engagement State

/// Engagement level classification
enum EngagementState: String, Codable {
    case high = "High"
    case medium = "Medium"
    case low = "Low"
    case unknown = "Unknown"
}

// MARK: - Face Session Data

/// Data captured during a facial analysis session
struct FaceSessionData: Codable, Identifiable {
    let id: UUID
    let timestamp: Int64
    let emotion: String
    let gaze: String
    let engagement: EngagementState
    
    init(emotion: String, gaze: String, engagement: EngagementState) {
        self.id = UUID()
        self.timestamp = Int64(Date().timeIntervalSince1970 * 1000)
        self.emotion = emotion
        self.gaze = gaze
        self.engagement = engagement
    }
}

// MARK: - Interaction Data

/// User interaction data for engagement calculation
struct InteractionData: Codable {
    let timestamp: Int64
    let interactionType: String
    let duration: Double?
    
    init(interactionType: String, duration: Double? = nil) {
        self.timestamp = Int64(Date().timeIntervalSince1970 * 1000)
        self.interactionType = interactionType
        self.duration = duration
    }
}

// MARK: - Network Models

/// Engagement data for API submission
struct EngagementData: Codable {
    let timestamp: Int64
    let state: String
    
    init(timestamp: Int64, state: String) {
        self.timestamp = timestamp
        self.state = state
    }
}

/// Facial landmark data
struct FacialLandmark: Codable {
    let x: Float
    let y: Float
    let z: Float
    let confidence: Float
    let landmarkType: String
}

/// Emotion detection data
struct EmotionData: Codable {
    let primaryEmotion: String
    let confidence: Float
    let emotionScores: [String: Float]
}

/// Gaze detection data
struct GazeData: Codable {
    let direction: String
    let confidence: Float
    let eyeOpenness: Float
}

/// Facial analysis data for AI/ML backend
struct FacialAnalysisData: Codable {
    let sessionId: String
    let timestamp: Int64
    let landmarks: [FacialLandmark]
    let emotions: EmotionData
    let gaze: GazeData
    let engagement: EngagementState
    let confidence: Float
    let metadata: [String: String]
}

/// Batch facial data for session submission
struct BatchFacialData: Codable {
    let sessionId: String
    let startTime: Int64
    let endTime: Int64
    let dataPoints: [FacialAnalysisData]
}

/// AI/ML analysis result
struct AnalysisResult: Codable {
    let insights: [String]
    let recommendations: [String]
    let confidenceScore: Float
}

/// Session insights from AI/ML backend
struct SessionInsights: Codable {
    let averageEngagement: Float
    let emotionDistribution: [String: Float]
    let gazePatterns: [String]
    let recommendations: [String]
}

/// Batch analysis result
struct BatchAnalysisResult: Codable {
    let sessionId: String
    let sessionInsights: SessionInsights
    let overallScore: Float
    let detailedMetrics: [String: Float]
}

/// Engagement prediction request
struct EngagementPredictionRequest: Codable {
    let historicalData: [FacialAnalysisData]
    let currentContext: String
    let timeWindow: Int64
}

/// Engagement prediction response
struct EngagementPrediction: Codable {
    let predictedEngagement: EngagementState
    let confidence: Float
    let factors: [String: Float]
    let recommendations: [String]
}
