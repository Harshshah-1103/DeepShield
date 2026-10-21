package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "analyses")
data class AnalysisEntity(
    @PrimaryKey val id: String,
    val mediaType: String, // "IMAGE", "VIDEO", "AUDIO", "AUDIO_VIDEO"
    val filename: String,
    val fileHash: String, // SHA-256
    val fileSizeBytes: Long,
    val dimensions: String,
    val format: String,
    val riskScore: Int, // 0 - 100
    val classification: String, // "LIKELY_AUTHENTIC", "INCONCLUSIVE", "LIKELY_MANIPULATED"
    val confidenceScore: Float, // 0.0 - 1.0
    val timestamp: Long,
    val processingDurationMs: Long,
    val heatmapPath: String? = null,
    val elaPath: String? = null,
    val noisePath: String? = null,
    val mediaUri: String? = null,
    val summaryFinding: String,
    val evidenceItemsJson: String,
    val detectorResultsJson: String,
    val technicalMetadataJson: String,
    val fusionMethod: String,
    val calibrationStatus: String,
    val limitationsText: String,
    val videoTimelineJson: String? = null,
    val audioSegmentJson: String? = null
)
