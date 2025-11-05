//
//  CronosFacialApp.swift
//  CronosFacial
//
//  iOS version of CronosFacial - Facial Recognition & Engagement Tracking
//

import SwiftUI

@main
struct CronosFacialApp: App {
    // Dependency injection container
    @StateObject private var container = DependencyContainer()
    
    var body: some Scene {
        WindowGroup {
            MainView()
                .environmentObject(container)
        }
    }
}

/// Dependency Injection Container
/// Similar to Hilt's SingletonComponent in Android
class DependencyContainer: ObservableObject {
    // Network services
    lazy var apiService: APIService = {
        APIService()
    }()
    
    lazy var aiMLService: AIMLService = {
        AIMLService(apiService: apiService)
    }()
    
    // Repository
    lazy var facialAnalysisRepository: FacialAnalysisRepository = {
        FacialAnalysisRepository(
            apiService: apiService,
            aiMLService: aiMLService
        )
    }()
    
    // Analyzers
    lazy var emotionAnalyzer: EmotionAnalyzer = {
        EmotionAnalyzer()
    }()
    
    lazy var gazeEstimator: GazeEstimator = {
        GazeEstimator()
    }()
    
    lazy var engagementEstimator: EngagementEstimator = {
        EngagementEstimator()
    }()
}
