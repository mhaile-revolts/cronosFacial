//
//  Result.swift
//  CronosFacial
//
//  Type-safe error handling (equivalent to Android's Result sealed class)
//

import Foundation

/// A type representing either a success with a value or a failure with an error.
/// Equivalent to the Android Result sealed class.
enum Result<Value> {
    case success(Value)
    case failure(Error)
    case loading
    
    /// Returns true if this is a success result
    var isSuccess: Bool {
        if case .success = self {
            return true
        }
        return false
    }
    
    /// Returns true if this is a failure result
    var isFailure: Bool {
        if case .failure = self {
            return true
        }
        return false
    }
    
    /// Returns true if this is a loading result
    var isLoading: Bool {
        if case .loading = self {
            return true
        }
        return false
    }
    
    /// Returns the success value or nil
    var value: Value? {
        if case .success(let value) = self {
            return value
        }
        return nil
    }
    
    /// Returns the error or nil
    var error: Error? {
        if case .failure(let error) = self {
            return error
        }
        return nil
    }
    
    /// Map the success value to another type
    func map<NewValue>(_ transform: (Value) -> NewValue) -> Result<NewValue> {
        switch self {
        case .success(let value):
            return .success(transform(value))
        case .failure(let error):
            return .failure(error)
        case .loading:
            return .loading
        }
    }
    
    /// Perform an action on success
    func onSuccess(_ action: (Value) -> Void) -> Result<Value> {
        if case .success(let value) = self {
            action(value)
        }
        return self
    }
    
    /// Perform an action on failure
    func onFailure(_ action: (Error) -> Void) -> Result<Value> {
        if case .failure(let error) = self {
            action(error)
        }
        return self
    }
}

/// Custom errors for the app
enum CronosFacialError: LocalizedError {
    case networkError(String)
    case cameraError(String)
    case analysisError(String)
    case permissionDenied
    
    var errorDescription: String? {
        switch self {
        case .networkError(let message):
            return "Network error: \(message)"
        case .cameraError(let message):
            return "Camera error: \(message)"
        case .analysisError(let message):
            return "Analysis error: \(message)"
        case .permissionDenied:
            return "Camera permission denied"
        }
    }
}
