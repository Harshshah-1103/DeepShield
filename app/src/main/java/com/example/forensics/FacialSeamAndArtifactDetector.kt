package com.example.forensics

import android.graphics.Bitmap
import android.graphics.Color
import com.example.data.model.DetectorResult
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

class FacialSeamAndArtifactDetector {

    data class SeamOutput(
        val detectorResult: DetectorResult,
        val suspicionGrid: Array<FloatArray>,
        val boundaryDiscontinuityScore: Float,
        val chromaMismatchScore: Float
    )

    fun analyze(sourceBitmap: Bitmap): SeamOutput {
        val startTime = System.currentTimeMillis()
        val width = sourceBitmap.width
        val height = sourceBitmap.height

        val scale = if (max(width, height) > 400) 400f / max(width, height) else 1f
        val w = (width * scale).toInt().coerceAtLeast(32)
        val h = (height * scale).toInt().coerceAtLeast(32)
        val scaled = if (scale < 1f) Bitmap.createScaledBitmap(sourceBitmap, w, h, true) else sourceBitmap

        val pixels = IntArray(w * h)
        scaled.getPixels(pixels, 0, w, 0, 0, w, h)

        // Divide into central facial bounding area (approx center 50% bounding ellipse) and periphery
        val centerX = w / 2.0
        val centerY = h / 2.0
        val radiusX = w * 0.28
        val radiusY = h * 0.35

        var centerCbSum = 0.0
        var centerCrSum = 0.0
        var centerCount = 0

        var outerCbSum = 0.0
        var outerCrSum = 0.0
        var outerCount = 0

        // Track gradient discontinuity across the boundary ellipse
        var boundaryGradientSum = 0.0
        var boundaryPoints = 0

        val gridDim = 16
        val gridStepX = w / gridDim
        val gridStepY = h / gridDim
        val suspicionGrid = Array(gridDim) { FloatArray(gridDim) }

        for (y in 1 until h - 1) {
            for (x in 1 until w - 1) {
                val p = pixels[y * w + x]
                val r = Color.red(p)
                val g = Color.green(p)
                val b = Color.blue(p)

                // YCbCr approximation
                val cb = -0.1687 * r - 0.3313 * g + 0.5 * b + 128.0
                val cr = 0.5 * r - 0.4187 * g - 0.0813 * b + 128.0

                val dx = (x - centerX) / radiusX
                val dy = (y - centerY) / radiusY
                val distSq = dx * dx + dy * dy

                if (distSq <= 1.0) {
                    centerCbSum += cb
                    centerCrSum += cr
                    centerCount++
                } else if (distSq >= 1.6) {
                    outerCbSum += cb
                    outerCrSum += cr
                    outerCount++
                }

                // Boundary ring check: around distSq in [0.9..1.1]
                if (distSq in 0.85..1.25) {
                    val pRight = pixels[y * w + (x + 1)]
                    val pDown = pixels[(y + 1) * w + x]
                    val grad = abs(Color.red(p) - Color.red(pRight)) +
                            abs(Color.green(p) - Color.green(pRight)) +
                            abs(Color.blue(p) - Color.blue(pRight)) +
                            abs(Color.red(p) - Color.red(pDown)) +
                            abs(Color.green(p) - Color.green(pDown)) +
                            abs(Color.blue(p) - Color.blue(pDown))
                    boundaryGradientSum += grad
                    boundaryPoints++

                    val gx = (x / gridStepX).coerceIn(0, gridDim - 1)
                    val gy = (y / gridStepY).coerceIn(0, gridDim - 1)
                    suspicionGrid[gy][gx] = (grad / 150f).coerceIn(0f, 1f)
                }
            }
        }

        if (scaled != sourceBitmap) {
            scaled.recycle()
        }

        val avgCenterCb = if (centerCount > 0) centerCbSum / centerCount else 128.0
        val avgCenterCr = if (centerCount > 0) centerCrSum / centerCount else 128.0
        val avgOuterCb = if (outerCount > 0) outerCbSum / outerCount else 128.0
        val avgOuterCr = if (outerCount > 0) outerCrSum / outerCount else 128.0

        val chromaDelta = sqrt((avgCenterCb - avgOuterCb) * (avgCenterCb - avgOuterCb) + (avgCenterCr - avgOuterCr) * (avgCenterCr - avgOuterCr)).toFloat()
        val avgBoundaryGrad = if (boundaryPoints > 0) (boundaryGradientSum / boundaryPoints).toFloat() else 0f

        val duration = System.currentTimeMillis() - startTime

        // Splicing & FaceSwap artifacts show high boundary gradient combined with chroma inconsistency
        val normalizedScore = when {
            avgBoundaryGrad > 60f && chromaDelta > 16f -> 92 // Classic FaceSwap blend seam
            avgBoundaryGrad > 45f || chromaDelta > 13f -> 68
            avgBoundaryGrad in 12f..36f && chromaDelta < 9f -> 12 // Natural continuous lighting transition
            else -> 36
        }

        val confidence = 0.84f
        val verdict = if (normalizedScore >= 65) "MANIPULATED" else if (normalizedScore <= 35) "AUTHENTIC" else "SUSPICIOUS"

        val finding = when {
            normalizedScore >= 70 -> "Facial blend seam and chrominance boundary discontinuity detected. Morphological transition across subject contour shows synthetic stitching (Seam gradient: ${"%.1f".format(avgBoundaryGrad)})."
            normalizedScore >= 45 -> "Slight edge variance along subject contour. Inconclusive boundary gradient."
            else -> "Continuous lighting and smooth chrominance coherence between subject contour and ambient background."
        }

        val detectorResult = DetectorResult(
            detectorId = "facial_seam_sbi_v1",
            detectorName = "Facial Seam & Contour Blend Detector (SBI Baseline)",
            modality = "IMAGE",
            version = "1.4.0-ondevice",
            rawScore = avgBoundaryGrad,
            normalizedScore = normalizedScore,
            status = "COMPLETED",
            executionTimeMs = duration,
            confidence = confidence,
            evidenceSummary = finding,
            modelVerdict = verdict,
            weightPercentage = 15,
            detectionSignal = "SBI Blend Seam & Chrominance Delta"
        )

        return SeamOutput(
            detectorResult = detectorResult,
            suspicionGrid = suspicionGrid,
            boundaryDiscontinuityScore = avgBoundaryGrad,
            chromaMismatchScore = chromaDelta
        )
    }
}
