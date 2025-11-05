package com.cronosedx.cronosfacial.analyzer

import org.junit.Assert.assertEquals
import org.junit.Test

class GazeEstimatorTest {
    @Test
    fun testGazeEstimation() {
        // Test with sample landmarks (468 landmarks for face mesh)
        val landmarks = FloatArray(468) { 0.5f }
        val estimator = GazeEstimator()
        val result = estimator.estimateGaze(landmarks)
        
        // The current implementation returns "center"
        assertEquals("center", result)
    }

    @Test
    fun testEmptyLandmarks() {
        val landmarks = FloatArray(0)
        val estimator = GazeEstimator()
        val result = estimator.estimateGaze(landmarks)
        
        // Should still return a valid gaze direction
        assertEquals("center", result)
    }
}
