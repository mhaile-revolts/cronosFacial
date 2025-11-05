//
//  EmotionAnalyzer.swift
//  CronosFacial
//
//  Emotion detection analyzer using Vision framework
//

import Foundation
import Vision

/// Analyzes facial expressions to detect emotions
class EmotionAnalyzer {
    private var frameCount = 0
    
    // Emotion labels matching the Android version
    private let emotionLabels = [
        "Angry", "Disgust", "Fear", "Happy", "Sad", "Surprise", "Neutral"
    ]
    
    /// Predicts emotion from facial landmarks
    /// - Parameter landmarks: Array of facial landmark values
    /// - Returns: Detected emotion string
    func predictEmotion(from landmarks: [Float]) -> String {
        frameCount += 1
        print("[EmotionAnalyzer] Predicting emotion from \(landmarks.count) landmarks (frame: \(frameCount))")
        
        // Cycle through emotions (simulation - matches Android behavior)
        let emotionIndex = (frameCount / 2) % emotionLabels.count
        let emotion = emotionLabels[emotionIndex]
        print("[EmotionAnalyzer] Frame \(frameCount): Detected emotion '\(emotion)' (index: \(emotionIndex))")
        
        return emotion
    }
    
    /// Predicts emotion with confidence score
    /// - Parameter landmarks: Array of facial landmark values
    /// - Returns: Tuple of emotion and confidence
    func predictEmotionWithConfidence(from landmarks: [Float]) -> (emotion: String, confidence: Float) {
        let emotion = predictEmotion(from: landmarks)
        let confidence: Float = 0.85 // Fixed confidence for simulation
        return (emotion, confidence)
    }
    
    /// Gets all emotion scores
    /// - Parameter landmarks: Array of facial landmark values
    /// - Returns: Dictionary of emotions to scores
    func predictAllEmotionScores(from landmarks: [Float]) -> [String: Float] {
        let primaryEmotion = predictEmotion(from: landmarks)
        var scores: [String: Float] = [:]
        
        for emotion in emotionLabels {
            scores[emotion] = emotion == primaryEmotion ? 0.85 : 0.05
        }
        
        return scores
    }
    
    /// Predicts emotion from VNFaceObservation using Vision framework
    /// - Parameter faceObservation: Face observation from Vision
    /// - Returns: Detected emotion
    func predictEmotion(from faceObservation: VNFaceObservation) -> String {
        // Extract landmarks from VNFaceObservation
        guard let landmarks = faceObservation.landmarks else {
            return "Neutral"
        }
        
        // Convert landmarks to float array (simplified)
        var landmarkArray: [Float] = []
        
        // Extract key facial features
        if let leftEye = landmarks.leftEye {
            landmarkArray.append(contentsOf: leftEye.normalizedPoints.flatMap { [Float($0.x), Float($0.y)] })
        }
        if let rightEye = landmarks.rightEye {
            landmarkArray.append(contentsOf: rightEye.normalizedPoints.flatMap { [Float($0.x), Float($0.y)] })
        }
        if let outerLips = landmarks.outerLips {
            landmarkArray.append(contentsOf: outerLips.normalizedPoints.flatMap { [Float($0.x), Float($0.y)] })
        }
        
        // Analyze facial features for emotion
        // This is a simplified version - in production, use CoreML model
        return analyzeFacialFeatures(landmarks: landmarks)
    }
    
    /// Analyzes facial features to determine emotion
    private func analyzeFacialFeatures(landmarks: VNFaceLandmarks2D) -> String {
        // Simple heuristic-based emotion detection
        // In production, replace with CoreML model
        
        // Check smile (outer lips curvature)
        if let outerLips = landmarks.outerLips, outerLips.pointCount > 0 {
            let points = outerLips.normalizedPoints
            if points.count > 2 {
                let leftCorner = points[0]
                let rightCorner = points[points.count / 2]
                let center = points[points.count / 4]
                
                // If center is higher than corners, likely smiling
                if center.y > (leftCorner.y + rightCorner.y) / 2 {
                    return "Happy"
                }
            }
        }
        
        // Check eyebrows for anger/surprise
        if let leftEyebrow = landmarks.leftEyebrow,
           let rightEyebrow = landmarks.rightEyebrow {
            // Simplified: use current frame-based simulation
            return predictEmotion(from: [])
        }
        
        return "Neutral"
    }
}
