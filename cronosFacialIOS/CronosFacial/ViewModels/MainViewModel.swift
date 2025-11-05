//
//  MainViewModel.swift
//  CronosFacial
//
//  MVVM ViewModel for main screen (equivalent to Android MainViewModel)
//

import Foundation
import Combine
import AVFoundation

/// UI State for the main screen
struct MainUiState {
    var isTracking: Bool = false
    var sessionData: [FaceSessionData] = []
    var engagementStates: [(timestamp: Int64, state: EngagementState)] = []
    var latestEmotion: String = "Neutral"
    var latestEngagement: EngagementState = .unknown
    var message: String? = nil
    var isLoading: Bool = false
    var error: String? = nil
    var hasCameraPermission: Bool = false
}

/// Main ViewModel managing facial analysis session state
/// Equivalent to Android's MainViewModel with StateFlow
class MainViewModel: ObservableObject {
    @Published private(set) var uiState = MainUiState()
    
    private let repository: FacialAnalysisRepository
    private var cancellables = Set<AnyCancellable>()
    
    init(repository: FacialAnalysisRepository) {
        self.repository = repository
        checkCameraPermission()
    }
    
    // MARK: - Camera Permission
    
    func checkCameraPermission() {
        switch AVCaptureDevice.authorizationStatus(for: .video) {
        case .authorized:
            uiState.hasCameraPermission = true
        case .notDetermined:
            requestCameraPermission()
        case .denied, .restricted:
            uiState.hasCameraPermission = false
        @unknown default:
            uiState.hasCameraPermission = false
        }
    }
    
    func requestCameraPermission() {
        AVCaptureDevice.requestAccess(for: .video) { [weak self] granted in
            DispatchQueue.main.async {
                self?.uiState.hasCameraPermission = granted
            }
        }
    }
    
    // MARK: - Tracking Session
    
    /// Start a new tracking session
    func startTracking() {
        uiState.isTracking = true
        uiState.sessionData = []
        uiState.engagementStates = []
        uiState.latestEmotion = "Neutral"
        uiState.latestEngagement = .unknown
        uiState.message = nil
        uiState.error = nil
        
        // Start AI/ML session
        repository.startAiMlSession()
    }
    
    /// Stop the current tracking session and submit data
    func stopTracking() {
        uiState.isTracking = false
        uiState.isLoading = true
        
        Task {
            let result = await repository.submitAllSessionData(
                sessionData: uiState.sessionData,
                engagementStates: uiState.engagementStates
            )
            
            await MainActor.run {
                switch result {
                case .success(let message):
                    uiState.message = message
                    uiState.isLoading = false
                    uiState.error = nil
                    
                case .failure(let error):
                    uiState.error = error.localizedDescription
                    uiState.isLoading = false
                    
                case .loading:
                    break
                }
            }
        }
    }
    
    /// Toggle tracking on/off
    func toggleTracking() {
        if uiState.isTracking {
            stopTracking()
        } else {
            startTracking()
        }
    }
    
    // MARK: - Data Handling
    
    /// Handle new face data from camera
    func onFaceData(_ data: FaceSessionData) {
        guard uiState.isTracking else { return }
        
        uiState.sessionData.append(data)
        uiState.latestEmotion = data.emotion
        
        // Stream to AI/ML backend
        Task {
            let landmarks = [
                FacialLandmark(
                    x: 0.5, y: 0.5, z: 0.0,
                    confidence: 0.8,
                    landmarkType: "face_center"
                )
            ]
            
            _ = await repository.streamFacialData(
                landmarks: landmarks,
                emotion: data.emotion,
                gaze: data.gaze,
                engagement: data.engagement,
                confidence: 0.8
            )
        }
    }
    
    /// Handle new engagement state
    func onEngagement(_ engagement: EngagementState) {
        guard uiState.isTracking else { return }
        
        let timestamp = Int64(Date().timeIntervalSince1970 * 1000)
        uiState.engagementStates.append((timestamp, engagement))
        uiState.latestEngagement = engagement
    }
    
    // MARK: - Message Handling
    
    /// Clear displayed message
    func clearMessage() {
        uiState.message = nil
    }
    
    /// Clear displayed error
    func clearError() {
        uiState.error = nil
    }
}
