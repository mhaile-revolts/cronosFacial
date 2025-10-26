package com.cronosedx.cronosfacial.model

data class InteractionData(
    val timestamp: Long,
    val interactionType: String,
    val confidence: Float
) 