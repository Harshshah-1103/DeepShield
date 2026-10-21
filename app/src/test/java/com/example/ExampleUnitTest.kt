package com.example

import com.example.data.model.DetectorResult
import com.example.data.model.EvidenceItem
import com.example.forensics.EvidenceFusionEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testEvidenceFusionScoreBounds() {
        val detectors = listOf(
            DetectorResult(
                detectorId = "srm_noise_residual_v2",
                detectorName = "Spatial Noise & PRNU Residual (SRM)",
                modality = "IMAGE",
                version = "2.0.4",
                rawScore = 3.5f,
                normalizedScore = 80,
                status = "COMPLETED",
                executionTimeMs = 15L,
                confidence = 0.85f,
                evidenceSummary = "Smoothing detected"
            ),
            DetectorResult(
                detectorId = "ela_compression_v2",
                detectorName = "Error Level Analysis (ELA)",
                modality = "IMAGE",
                version = "2.1.0",
                rawScore = 0.65f,
                normalizedScore = 75,
                status = "COMPLETED",
                executionTimeMs = 22L,
                confidence = 0.82f,
                evidenceSummary = "Compression discrepancy"
            )
        )

        val evidence = listOf(
            EvidenceItem(
                id = "EVD-1",
                category = "COMPRESSION",
                detectorSource = "ELA",
                finding = "Non-uniform compression",
                score = 75,
                confidence = 0.82f,
                explanation = "ELA discrepancy",
                limitations = "Recompressed"
            )
        )

        val result = EvidenceFusionEngine.fuse(
            mediaType = "IMAGE",
            filename = "test_sample.jpg",
            fileHash = "abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789",
            detectorResults = detectors,
            evidenceItems = evidence
        )

        assertTrue("Fused risk score must be between 0 and 100", result.overallRiskScore in 0..100)
        assertTrue("Confidence score must be between 0 and 1", result.confidenceScore in 0.0f..1.0f)
        assertTrue("Uncertainty score must be between 0 and 1", result.uncertaintyScore in 0.0f..1.0f)
        assertEquals("LIKELY_MANIPULATED", result.classification)
    }

    @Test
    fun testDetectorDisagreementTriggersInconclusive() {
        val detectors = listOf(
            DetectorResult(
                detectorId = "srm_noise_residual_v2",
                detectorName = "Spatial Noise & PRNU Residual (SRM)",
                modality = "IMAGE",
                version = "2.0.4",
                rawScore = 1.0f,
                normalizedScore = 90, // Claims manipulated
                status = "COMPLETED",
                executionTimeMs = 15L,
                confidence = 0.85f,
                evidenceSummary = "Synthetic smoothing"
            ),
            DetectorResult(
                detectorId = "ela_compression_v2",
                detectorName = "Error Level Analysis (ELA)",
                modality = "IMAGE",
                version = "2.1.0",
                rawScore = 0.05f,
                normalizedScore = 15, // Claims authentic
                status = "COMPLETED",
                executionTimeMs = 22L,
                confidence = 0.82f,
                evidenceSummary = "Uniform compression"
            )
        )

        val result = EvidenceFusionEngine.fuse(
            mediaType = "IMAGE",
            filename = "conflicting_sample.jpg",
            fileHash = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef",
            detectorResults = detectors,
            evidenceItems = emptyList()
        )

        assertTrue("Disagreement must be flagged", result.disagreementDetected)
        assertEquals("Conflicting evidence must result in INCONCLUSIVE classification", "INCONCLUSIVE", result.classification)
        assertTrue("Uncertainty must be elevated", result.uncertaintyScore > 0.3f)
    }
}
