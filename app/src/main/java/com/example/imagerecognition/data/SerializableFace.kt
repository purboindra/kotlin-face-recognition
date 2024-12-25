package com.example.imagerecognition.data

import kotlinx.serialization.Serializable

@Serializable
data class SerializableFace(
    val boundingBox: String,
    val trackingId: Int? = null,
    val smileProbability: Float? = null,
    val leftEyeOpenProbability: Float? = null,
    val rightEyeOpenProbability: Float? = null
)