package com.example.forensics

import com.example.data.entity.AnalysisEntity
import com.example.data.model.AudioSegmentEvidence
import com.example.data.model.DetectorResult
import com.example.data.model.EvidenceItem
import com.example.data.model.TechnicalMetadata
import com.example.data.model.VideoFrameEvidence
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ForensicReportParser {

    fun parseEvidenceItems(json: String?): List<EvidenceItem> {
        if (json.isNullOrEmpty()) return emptyList()
        val list = mutableListOf<EvidenceItem>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    EvidenceItem(
                        id = obj.optString("id", "EVD-$i"),
                        category = obj.optString("category", "GENERAL"),
                        detectorSource = obj.optString("detectorSource", "Unknown"),
                        finding = obj.optString("finding", ""),
                        score = obj.optInt("score", 0),
                        confidence = obj.optDouble("confidence", 0.0).toFloat(),
                        explanation = obj.optString("explanation", ""),
                        limitations = obj.optString("limitations", ""),
                        locationOrTimestamp = if (obj.has("locationOrTimestamp")) obj.optString("locationOrTimestamp") else null
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    fun parseDetectorResults(json: String?): List<DetectorResult> {
        if (json.isNullOrEmpty()) return emptyList()
        val list = mutableListOf<DetectorResult>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    DetectorResult(
                        detectorId = obj.optString("detectorId", "det_$i"),
                        detectorName = obj.optString("detectorName", "Detector"),
                        modality = obj.optString("modality", "IMAGE"),
                        version = obj.optString("version", "1.0.0"),
                        rawScore = obj.optDouble("rawScore", 0.0).toFloat(),
                        normalizedScore = obj.optInt("normalizedScore", 0),
                        status = obj.optString("status", "COMPLETED"),
                        executionTimeMs = obj.optLong("executionTimeMs", 0L),
                        confidence = obj.optDouble("confidence", 0.0).toFloat(),
                        evidenceSummary = obj.optString("evidenceSummary", ""),
                        errorDetails = if (obj.has("errorDetails")) obj.optString("errorDetails") else null,
                        modelVerdict = obj.optString("modelVerdict", if (obj.optInt("normalizedScore", 0) >= 65) "MANIPULATED" else if (obj.optInt("normalizedScore", 0) <= 35) "AUTHENTIC" else "SUSPICIOUS"),
                        weightPercentage = obj.optInt("weightPercentage", 20),
                        detectionSignal = obj.optString("detectionSignal", "Forensic Signal")
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    fun parseTechnicalMetadata(json: String?): TechnicalMetadata? {
        if (json.isNullOrEmpty()) return null
        return try {
            val obj = JSONObject(json)
            val exifObj = obj.optJSONObject("exifData")
            val exifMap = mutableMapOf<String, String>()
            exifObj?.let {
                val keys = it.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    exifMap[k] = it.getString(k)
                }
            }
            TechnicalMetadata(
                sha256Hash = obj.optString("sha256Hash", ""),
                mimeType = obj.optString("mimeType", ""),
                dimensions = obj.optString("dimensions", ""),
                colorSpace = obj.optString("colorSpace", ""),
                compressionType = obj.optString("compressionType", ""),
                exifData = exifMap,
                headerValidationStatus = obj.optString("headerValidationStatus", ""),
                estimatedBitrate = if (obj.has("estimatedBitrate")) obj.optString("estimatedBitrate") else null,
                frameRate = if (obj.has("frameRate")) obj.optString("frameRate") else null,
                audioSampleRate = if (obj.has("audioSampleRate")) obj.optString("audioSampleRate") else null
            )
        } catch (_: Exception) {
            null
        }
    }

    fun parseVideoTimeline(json: String?): List<VideoFrameEvidence> {
        if (json.isNullOrEmpty()) return emptyList()
        val list = mutableListOf<VideoFrameEvidence>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    VideoFrameEvidence(
                        frameIndex = obj.optInt("frameIndex", i + 1),
                        timestampSeconds = obj.optDouble("timestampSeconds", 0.0),
                        frameRiskScore = obj.optInt("frameRiskScore", 0),
                        anomalyDetected = obj.optBoolean("anomalyDetected", false),
                        temporalInconsistency = obj.optDouble("temporalInconsistency", 0.0).toFloat()
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    fun parseAudioSegments(json: String?): List<AudioSegmentEvidence> {
        if (json.isNullOrEmpty()) return emptyList()
        val list = mutableListOf<AudioSegmentEvidence>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    AudioSegmentEvidence(
                        startSeconds = obj.optDouble("startSeconds", 0.0),
                        endSeconds = obj.optDouble("endSeconds", 0.0),
                        segmentRiskScore = obj.optInt("segmentRiskScore", 0),
                        vocoderAnomalyScore = obj.optDouble("vocoderAnomalyScore", 0.0).toFloat(),
                        spectralContinuityScore = obj.optDouble("spectralContinuityScore", 0.0).toFloat()
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    fun generateFormattedForensicReport(entity: AnalysisEntity): String {
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.US).format(Date(entity.timestamp))
        val detectors = parseDetectorResults(entity.detectorResultsJson)
        val evidence = parseEvidenceItems(entity.evidenceItemsJson)
        val meta = parseTechnicalMetadata(entity.technicalMetadataJson)

        val sb = StringBuilder()
        sb.append("================================================================================\n")
        sb.append("                       DEEPSHIELD AI FORENSIC INTELLIGENCE                     \n")
        sb.append("                    MULTIMODAL DEEPFAKE & AI MANIPULATION REPORT                \n")
        sb.append("================================================================================\n\n")

        sb.append("[1. EVIDENCE IDENTIFICATION]\n")
        sb.append("Report ID:             ${entity.id}\n")
        sb.append("Analyzed File:         ${entity.filename}\n")
        sb.append("Media Modality:        ${entity.mediaType}\n")
        sb.append("Format:                ${entity.format}\n")
        sb.append("File Size:             ${entity.fileSizeBytes} bytes (${"%.2f".format(entity.fileSizeBytes / 1024.0 / 1024.0)} MB)\n")
        sb.append("Dimensions / Specs:    ${entity.dimensions}\n")
        sb.append("Cryptographic SHA-256: ${entity.fileHash}\n")
        sb.append("Analysis Timestamp:    $dateStr\n")
        sb.append("Processing Latency:    ${entity.processingDurationMs} ms\n\n")

        sb.append("[2. CALIBRATED FORENSIC ASSESSMENT]\n")
        sb.append("OVERALL RISK SCORE:    ${entity.riskScore} / 100\n")
        sb.append("CLASSIFICATION:        ${entity.classification}\n")
        sb.append("CONFIDENCE INTERVAL:   ${"%.1f".format(entity.confidenceScore * 100)}% (Uncertainty: ±${"%.1f".format((1f - entity.confidenceScore) * 100)}%)\n")
        sb.append("CALIBRATION STATUS:    ${entity.calibrationStatus}\n")
        sb.append("FUSION METHODOLOGY:    ${entity.fusionMethod}\n")
        sb.append("SUMMARY FINDING:       ${entity.summaryFinding}\n\n")

        sb.append("[3. COMPLEMENTARY DETECTOR RESULTS]\n")
        for (d in detectors) {
            sb.append("  • ${d.detectorName} (v${d.version})\n")
            sb.append("    - Modality:   ${d.modality}\n")
            sb.append("    - Risk Score: ${d.normalizedScore}/100 (Raw: ${"%.3f".format(d.rawScore)})\n")
            sb.append("    - Confidence: ${"%.1f".format(d.confidence * 100)}%\n")
            sb.append("    - Time:       ${d.executionTimeMs} ms\n")
            sb.append("    - Evidence:   ${d.evidenceSummary}\n\n")
        }

        sb.append("[4. STRUCTURED FORENSIC EVIDENCE LOG]\n")
        for (e in evidence) {
            sb.append("  [${e.id}] Category: ${e.category} | Source: ${e.detectorSource}\n")
            sb.append("    Finding:     ${e.finding}\n")
            sb.append("    Risk Score:  ${e.score}/100\n")
            sb.append("    Explanation: ${e.explanation}\n")
            sb.append("    Limitations: ${e.limitations}\n")
            if (e.locationOrTimestamp != null) {
                sb.append("    Location:    ${e.locationOrTimestamp}\n")
            }
            sb.append("\n")
        }

        sb.append("[5. TECHNICAL METADATA]\n")
        if (meta != null) {
            sb.append("  MIME Type:          ${meta.mimeType}\n")
            sb.append("  Header Validation:  ${meta.headerValidationStatus}\n")
            sb.append("  Color Space:        ${meta.colorSpace}\n")
            if (meta.exifData.isNotEmpty()) {
                sb.append("  EXIF Tags:\n")
                for ((k, v) in meta.exifData) {
                    sb.append("    $k: $v\n")
                }
            } else {
                sb.append("  EXIF: No camera metadata tags present.\n")
            }
        }
        sb.append("\n")

        sb.append("[6. SCIENTIFIC LIMITATIONS & REPRODUCIBILITY]\n")
        sb.append(entity.limitationsText).append("\n\n")
        sb.append("Notice: This analysis report is an analytical assessment, not definitive\n")
        sb.append("legal proof of authenticity or manipulation.\n")
        sb.append("================================================================================\n")

        return sb.toString()
    }

    fun exportJsonReport(entity: AnalysisEntity): String {
        val root = JSONObject()
        root.put("reportVersion", "1.0.0")
        root.put("analysisId", entity.id)
        root.put("timestamp", entity.timestamp)
        root.put("filename", entity.filename)
        root.put("fileHashSha256", entity.fileHash)
        root.put("fileSizeBytes", entity.fileSizeBytes)
        root.put("mediaType", entity.mediaType)
        root.put("format", entity.format)
        root.put("dimensions", entity.dimensions)
        root.put("riskScore", entity.riskScore)
        root.put("classification", entity.classification)
        root.put("confidenceScore", entity.confidenceScore.toDouble())
        root.put("fusionMethod", entity.fusionMethod)
        root.put("calibrationStatus", entity.calibrationStatus)
        root.put("summaryFinding", entity.summaryFinding)
        root.put("limitationsText", entity.limitationsText)

        root.put("detectors", JSONArray(entity.detectorResultsJson))
        root.put("evidence", JSONArray(entity.evidenceItemsJson))
        root.put("technicalMetadata", JSONObject(entity.technicalMetadataJson))
        if (!entity.videoTimelineJson.isNullOrEmpty()) {
            root.put("videoTimeline", JSONArray(entity.videoTimelineJson))
        }
        if (!entity.audioSegmentJson.isNullOrEmpty()) {
            root.put("audioSegments", JSONArray(entity.audioSegmentJson))
        }

        return root.toString(2)
    }
}
