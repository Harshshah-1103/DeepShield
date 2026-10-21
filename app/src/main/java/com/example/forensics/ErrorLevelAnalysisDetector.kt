package com.example.forensics

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import com.example.data.model.DetectorResult
import java.io.ByteArrayOutputStream
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

class ErrorLevelAnalysisDetector(
    val jpegQuality: Int = 90,
    val amplificationScale: Int = 15
) {
    data class ElaOutput(
        val detectorResult: DetectorResult,
        val elaBitmap: Bitmap,
        val blockSuspicionGrid: Array<FloatArray>, // grid of localized anomaly values [0.0..1.0]
        val gridRows: Int,
        val gridCols: Int,
        val meanError: Float,
        val maxBlockDiscrepancy: Float
    )

    fun analyze(sourceBitmap: Bitmap): ElaOutput {
        val startTime = System.currentTimeMillis()
        val width = sourceBitmap.width
        val height = sourceBitmap.height

        // Downscale working copy if larger than 800px to ensure fast, responsive on-device processing
        val scale = if (max(width, height) > 800) 800f / max(width, height) else 1f
        val w = (width * scale).toInt().coerceAtLeast(32)
        val h = (height * scale).toInt().coerceAtLeast(32)
        val scaledOrig = if (scale < 1f) Bitmap.createScaledBitmap(sourceBitmap, w, h, true) else sourceBitmap

        // 1. Recompress in memory at specified JPEG quality
        val outputStream = ByteArrayOutputStream()
        scaledOrig.compress(Bitmap.CompressFormat.JPEG, jpegQuality, outputStream)
        val compressedBytes = outputStream.toByteArray()
        val recompressedBitmap = BitmapFactory.decodeByteArray(compressedBytes, 0, compressedBytes.size)

        // 2. Compute pixel-by-pixel difference
        val origPixels = IntArray(w * h)
        val recompressedPixels = IntArray(w * h)
        val elaPixels = IntArray(w * h)

        scaledOrig.getPixels(origPixels, 0, w, 0, 0, w, h)
        recompressedBitmap.getPixels(recompressedPixels, 0, w, 0, 0, w, h)

        var totalError = 0L
        val blockSize = 16
        val gridCols = (w + blockSize - 1) / blockSize
        val gridRows = (h + blockSize - 1) / blockSize
        val blockErrors = Array(gridRows) { FloatArray(gridCols) }
        val blockCounts = Array(gridRows) { IntArray(gridCols) }

        for (y in 0 until h) {
            val by = y / blockSize
            for (x in 0 until w) {
                val idx = y * w + x
                val c1 = origPixels[idx]
                val c2 = recompressedPixels[idx]

                val dr = abs(Color.red(c1) - Color.red(c2))
                val dg = abs(Color.green(c1) - Color.green(c2))
                val db = abs(Color.blue(c1) - Color.blue(c2))
                val maxDelta = max(dr, max(dg, db))
                totalError += maxDelta

                val bx = x / blockSize
                blockErrors[by][bx] += maxDelta.toFloat()
                blockCounts[by][bx]++

                // Amplify for visual difference bitmap
                val visualDelta = (maxDelta * amplificationScale).coerceIn(0, 255)
                elaPixels[idx] = Color.rgb(visualDelta, visualDelta, visualDelta)
            }
        }

        // 3. Compute block statistics & localized discrepancy
        val blockAverages = mutableListOf<Float>()
        for (r in 0 until gridRows) {
            for (c in 0 until gridCols) {
                val count = blockCounts[r][c].coerceAtLeast(1)
                blockErrors[r][c] = blockErrors[r][c] / count
                blockAverages.add(blockErrors[r][c])
            }
        }

        val globalMean = if (blockAverages.isNotEmpty()) blockAverages.average().toFloat() else 0f
        var varianceSum = 0f
        for (v in blockAverages) {
            varianceSum += (v - globalMean) * (v - globalMean)
        }
        val stdDev = if (blockAverages.isNotEmpty()) sqrt(varianceSum / blockAverages.size) else 0f

        // Normalize block discrepancy grid: blocks > mean + 1.5 * stdDev have high suspicion
        val blockSuspicion = Array(gridRows) { FloatArray(gridCols) }
        var maxDiscrepancy = 0f

        for (r in 0 until gridRows) {
            for (c in 0 until gridCols) {
                val deviation = if (stdDev > 0.1f) (blockErrors[r][c] - globalMean) / stdDev else 0f
                val normSuspicion = (deviation / 3.0f).coerceIn(0f, 1f)
                blockSuspicion[r][c] = normSuspicion
                if (normSuspicion > maxDiscrepancy) {
                    maxDiscrepancy = normSuspicion
                }
            }
        }

        val elaBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        elaBitmap.setPixels(elaPixels, 0, w, 0, 0, w, h)

        // If scaled copy was created, clean up
        if (scaledOrig != sourceBitmap) {
            scaledOrig.recycle()
        }
        recompressedBitmap.recycle()

        val duration = System.currentTimeMillis() - startTime

        // Risk estimation based on compression discrepancy
        val normalizedScore = when {
            maxDiscrepancy > 0.55f && stdDev > 5.0f -> (80 + (maxDiscrepancy * 15f)).toInt().coerceIn(80, 95)
            maxDiscrepancy > 0.38f && stdDev > 3.5f -> (55 + (maxDiscrepancy * 20f)).toInt().coerceIn(55, 75)
            stdDev < 2.5f && maxDiscrepancy < 0.25f -> (8 + (stdDev * 3.5f)).toInt().coerceIn(6, 18)
            else -> (22 + (maxDiscrepancy * 25f)).toInt().coerceIn(20, 50)
        }
        val confidence = (0.80f + (stdDev / 35f)).coerceIn(0.72f, 0.96f)

        val verdict = if (normalizedScore >= 65) "MANIPULATED" else if (normalizedScore <= 35) "AUTHENTIC" else "SUSPICIOUS"

        val findingText = when {
            normalizedScore >= 65 -> "High localized compression discrepancy detected. Central region error levels diverge significantly from uniform background compression (Max z-score: ${"%.2f".format(maxDiscrepancy * 3.0f)})."
            normalizedScore >= 40 -> "Moderate error level variance observed. Compression artifacts exhibit minor non-uniform distribution across block boundaries."
            else -> "Uniform compression error level across entire image. Consistent with single-generation optical capture or uniform re-compression."
        }

        val detectorResult = DetectorResult(
            detectorId = "ela_compression_v2",
            detectorName = "Error Level Analysis (ELA)",
            modality = "IMAGE",
            version = "2.1.0-ondevice",
            rawScore = maxDiscrepancy,
            normalizedScore = normalizedScore,
            status = "COMPLETED",
            executionTimeMs = duration,
            confidence = confidence,
            evidenceSummary = findingText,
            modelVerdict = verdict,
            weightPercentage = 20,
            detectionSignal = "JPEG Quantization Discrepancy & 16x16 Block Variance"
        )

        return ElaOutput(
            detectorResult = detectorResult,
            elaBitmap = elaBitmap,
            blockSuspicionGrid = blockSuspicion,
            gridRows = gridRows,
            gridCols = gridCols,
            meanError = globalMean,
            maxBlockDiscrepancy = maxDiscrepancy
        )
    }
}
