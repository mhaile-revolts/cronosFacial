//
//  GazeEstimator.swift
//  CronosFacial
//
//  Gaze direction estimation using eye landmarks
//

import Foundation
import Vision

/// Estimates gaze direction from facial landmarks
class GazeEstimator {
    // Threshold values for gaze detection
    private let gazeThresholdX: Float = 0.15
    private let gazeThresholdY: Float = 0.15
    
    /// Estimates gaze direction from landmark array
    /// - Parameter landmarks: Array of facial landmarks
    /// - Returns: Gaze direction string
    func estimateGaze(from landmarks: [Float]) -> String {
        guard !landmarks.isEmpty else {
            print("[GazeEstimator] No landmarks provided")
            return "unknown"
        }
        
        print("[GazeEstimator] Estimating gaze from \(landmarks.count) landmarks")
        
        let gazeOffsetX = calculateHorizontalGaze(from: landmarks)
        let gazeOffsetY = calculateVerticalGaze(from: landmarks)
        
        print("[GazeEstimator] Gaze offset - X: \(gazeOffsetX), Y: \(gazeOffsetY)")
        
        return determineGazeDirection(offsetX: gazeOffsetX, offsetY: gazeOffsetY)
    }
    
    /// Estimates gaze from VNFaceObservation
    /// - Parameter faceObservation: Face observation from Vision
    /// - Returns: Gaze direction string
    func estimateGaze(from faceObservation: VNFaceObservation) -> String {
        guard let landmarks = faceObservation.landmarks else {
            return "center"
        }
        
        // Extract eye positions
        guard let leftPupil = landmarks.leftPupil,
              let rightPupil = landmarks.rightPupil else {
            return "center"
        }
        
        // Calculate gaze based on pupil position relative to eye center
        let leftPupilPoints = leftPupil.normalizedPoints
        let rightPupilPoints = rightPupil.normalizedPoints
        
        guard !leftPupilPoints.isEmpty, !rightPupilPoints.isEmpty else {
            return "center"
        }
        
        // Average pupil positions
        let avgPupilX = (leftPupilPoints[0].x + rightPupilPoints[0].x) / 2
        let avgPupilY = (leftPupilPoints[0].y + rightPupilPoints[0].y) / 2
        
        // Determine gaze based on pupil position
        // Values are normalized, so 0.5 is center
        let offsetX = Float(avgPupilX - 0.5)
        let offsetY = Float(avgPupilY - 0.5)
        
        return determineGazeDirection(offsetX: offsetX * 2, offsetY: offsetY * 2)
    }
    
    /// Calculate horizontal gaze direction
    private func calculateHorizontalGaze(from landmarks: [Float]) -> Float {
        // Simulate gaze calculation using landmark variation
        let variation = (landmarks.reduce(0, +).truncatingRemainder(dividingBy: 1.0)) - 0.5
        return variation * 0.6 // Scale to reasonable range
    }
    
    /// Calculate vertical gaze direction
    private func calculateVerticalGaze(from landmarks: [Float]) -> Float {
        // Simulate vertical gaze using landmark data
        let variation = ((landmarks.reduce(0, +) * 1.3).truncatingRemainder(dividingBy: 1.0)) - 0.5
        return variation * 0.5 // Scale to reasonable range
    }
    
    /// Determine gaze direction string from X/Y offsets
    private func determineGazeDirection(offsetX: Float, offsetY: Float) -> String {
        let horizontal: String? = {
            if offsetX > gazeThresholdX {
                return "right"
            } else if offsetX < -gazeThresholdX {
                return "left"
            }
            return nil
        }()
        
        let vertical: String? = {
            if offsetY > gazeThresholdY {
                return "up"
            } else if offsetY < -gazeThresholdY {
                return "down"
            }
            return nil
        }()
        
        // Combine directions
        if let h = horizontal, let v = vertical {
            return "\(v)-\(h)"
        } else if let h = horizontal {
            return h
        } else if let v = vertical {
            return v
        } else {
            return "center"
        }
    }
    
    /// Get confidence score for gaze estimation
    /// - Parameter landmarks: Array of facial landmarks
    /// - Returns: Confidence score (0-1)
    func getGazeConfidence(from landmarks: [Float]) -> Float {
        guard !landmarks.isEmpty else {
            return 0.0
        }
        
        // In real implementation, confidence would be based on:
        // - Face detection confidence
        // - Eye openness
        // - Landmark quality
        return 0.85
    }
}
