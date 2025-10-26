package com.cronosedx.cronosfacial.analyzer

import com.google.mediapipe.formats.landmark.proto.LandmarkProto
import org.junit.Assert.assertEquals
import org.junit.Test

class GazeEstimatorTest {
    private fun makeLandmark(x: Float): LandmarkProto.NormalizedLandmark =
        LandmarkProto.NormalizedLandmark.newBuilder().setX(x).setY(0f).setZ(0f).build()

    private fun makeLandmarkList(
        left: Float, iris: Float, right: Float,
        leftR: Float, irisR: Float, rightR: Float
    ): LandmarkProto.NormalizedLandmarkList {
        val builder = LandmarkProto.NormalizedLandmarkList.newBuilder()
        // Fill with dummy landmarks up to 474
        repeat(474) { builder.addLandmark(makeLandmark(0f)) }
        builder.setLandmark(33, makeLandmark(left))
        builder.setLandmark(133, makeLandmark(right))
        builder.setLandmark(468, makeLandmark(iris))
        builder.setLandmark(362, makeLandmark(leftR))
        builder.setLandmark(263, makeLandmark(rightR))
        builder.setLandmark(473, makeLandmark(irisR))
        return builder.build()
    }

    @Test
    fun testCenterGaze() {
        val landmarks = makeLandmarkList(0f, 0.5f, 1f, 0f, 0.5f, 1f)
        val estimator = GazeEstimator()
        assertEquals("Center", estimator.estimateGaze(landmarks))
    }

    @Test
    fun testLeftGaze() {
        val landmarks = makeLandmarkList(0f, 0.2f, 1f, 0f, 0.2f, 1f)
        val estimator = GazeEstimator()
        assertEquals("Left", estimator.estimateGaze(landmarks))
    }

    @Test
    fun testRightGaze() {
        val landmarks = makeLandmarkList(0f, 0.8f, 1f, 0f, 0.8f, 1f)
        val estimator = GazeEstimator()
        assertEquals("Right", estimator.estimateGaze(landmarks))
    }

    @Test
    fun testUnknownGaze() {
        val landmarks = makeLandmarkList(0f, 0.2f, 1f, 0f, 0.8f, 1f)
        val estimator = GazeEstimator()
        assertEquals("Unknown", estimator.estimateGaze(landmarks))
    }
} 