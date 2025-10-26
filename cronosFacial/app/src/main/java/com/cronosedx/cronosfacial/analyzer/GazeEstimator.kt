package com.cronosedx.cronosfacial.analyzer

import android.util.Log

class GazeEstimator {
    private val TAG = "GazeEstimator"

    fun estimateGaze(landmarks: FloatArray): String {
        // Simplified gaze estimation - just return a default gaze
        Log.d(TAG, "Estimating gaze from ${landmarks.size} landmarks")
        return "center"
    }
} 