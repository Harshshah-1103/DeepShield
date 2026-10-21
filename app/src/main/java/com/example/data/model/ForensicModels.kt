package com.example.data.model

data class EvidenceItem(
    val id: String,
    val category: String, // "METADATA", "COMPRESSION", "FREQUENCY", "SPATIAL_NOISE", "FACIAL_SEAM", "TEMPORAL", "AUDIO_SPECTRAL"
    val detectorSource: String,
    val finding: String,
    val score: Int, // 0 to 100
    val confidence: Float, // 0.0 to 1.0
    val explanation: String,
    val limitations: String,
    val locationOrTimestamp: String? = null
)

data class DetectorResult(
    val detectorId: String,
    val detectorName: String,
    val modality: String,
    val version: String,
    val rawScore: Float,
    val normalizedScore: Int, // 0 - 100
    val status: String, // "COMPLETED", "FAILED", "SKIPPED", "UNAVAILABLE"
    val executionTimeMs: Long,
    val confidence: Float,
    val evidenceSummary: String,
    val errorDetails: String? = null,
    val modelVerdict: String = if (normalizedScore >= 65) "MANIPULATED" else if (normalizedScore <= 35) "AUTHENTIC" else "SUSPICIOUS",
    val weightPercentage: Int = 20,
    val detectionSignal: String = "Forensic Signal"
)

data class TechnicalMetadata(
    val sha256Hash: String,
    val mimeType: String,
    val dimensions: String,
    val colorSpace: String,
    val compressionType: String,
    val exifData: Map<String, String>,
    val headerValidationStatus: String,
    val estimatedBitrate: String? = null,
    val frameRate: String? = null,
    val audioSampleRate: String? = null
)

data class VideoFrameEvidence(
    val frameIndex: Int,
    val timestampSeconds: Double,
    val frameRiskScore: Int,
    val anomalyDetected: Boolean,
    val temporalInconsistency: Float
)

data class AudioSegmentEvidence(
    val startSeconds: Double,
    val endSeconds: Double,
    val segmentRiskScore: Int,
    val vocoderAnomalyScore: Float,
    val spectralContinuityScore: Float
)
