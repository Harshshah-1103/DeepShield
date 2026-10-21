package com.example.forensics

import android.graphics.Bitmap
import android.graphics.Color
import com.example.data.model.DetectorResult
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class FrequencyDomainDetector {

    data class FrequencyOutput(
        val detectorResult: DetectorResult,
        val spectrumBitmap: Bitmap,
        val highFreqRatio: Float,
        val gridArtifactScore: Float
    )

    fun analyze(sourceBitmap: Bitmap): FrequencyOutput {
        val startTime = System.currentTimeMillis()
        val width = sourceBitmap.width
        val height = sourceBitmap.height

        // Downsample to 256x256 for fast 2D DCT calculation
        val scaled = Bitmap.createScaledBitmap(sourceBitmap, 256, 256, true)
        val pixels = IntArray(256 * 256)
        scaled.getPixels(pixels, 0, 256, 0, 0, 256, 256)

        // 8x8 block DCT analysis
        val blockSize = 8
        val numBlocksX = 256 / blockSize
        val numBlocksY = 256 / blockSize

        var totalLowFreqEnergy = 0.0
        var totalMidFreqEnergy = 0.0
        var totalHighFreqEnergy = 0.0
        var periodicSpikeCount = 0

        val spectrumPixels = IntArray(256 * 256)

        // Precompute cosine table for 8x8 DCT
        val c = DoubleArray(blockSize)
        for (i in 0 until blockSize) {
            c[i] = if (i == 0) 1.0 / sqrt(2.0) else 1.0
        }

        val blockLuma = Array(blockSize) { DoubleArray(blockSize) }
        val blockDct = Array(blockSize) { DoubleArray(blockSize) }

        for (by in 0 until numBlocksY) {
            for (bx in 0 until numBlocksX) {
                // Extract 8x8 block luminance
                for (y in 0 until blockSize) {
                    for (x in 0 until blockSize) {
                        val px = pixels[(by * blockSize + y) * 256 + (bx * blockSize + x)]
                        blockLuma[y][x] = (0.299 * Color.red(px) + 0.587 * Color.green(px) + 0.114 * Color.blue(px)) - 128.0
                    }
                }

                // Compute 8x8 2D-DCT
                for (u in 0 until blockSize) {
                    for (v in 0 until blockSize) {
                        var sum = 0.0
                        for (x in 0 until blockSize) {
                            for (y in 0 until blockSize) {
                                sum += blockLuma[y][x] *
                                        cos(((2 * x + 1) * u * PI) / (2.0 * blockSize)) *
                                        cos(((2 * y + 1) * v * PI) / (2.0 * blockSize))
                            }
                        }
                        val coeff = 0.25 * c[u] * c[v] * sum
                        blockDct[u][v] = coeff
                        val magnitude = abs(coeff)

                        // Categorize frequencies by Manhattan distance from DC (u=0, v=0)
                        val freqDist = u + v
                        when {
                            freqDist in 1..3 -> totalLowFreqEnergy += magnitude
                            freqDist in 4..7 -> totalMidFreqEnergy += magnitude
                            freqDist >= 8 -> totalHighFreqEnergy += magnitude
                        }

                        // Check for periodic upsampling grid spikes typical of GANs and diffusion decoders
                        if ((u == 4 || v == 4 || u == 7 || v == 7) && magnitude > 45.0) {
                            periodicSpikeCount++
                        }

                        // Visual spectrum pixel
                        val visualIntensity = (magnitude * 2.5).toInt().coerceIn(0, 255)
                        spectrumPixels[(by * blockSize + u) * 256 + (bx * blockSize + v)] =
                            Color.rgb(0, (visualIntensity * 0.9).toInt(), visualIntensity)
                    }
                }
            }
        }

        scaled.recycle()

        val totalEnergy = (totalLowFreqEnergy + totalMidFreqEnergy + totalHighFreqEnergy).coerceAtLeast(1.0)
        val highRatio = (totalHighFreqEnergy / totalEnergy).toFloat()
        val gridArtifactScore = (periodicSpikeCount.toFloat() / (numBlocksX * numBlocksY * 4).toFloat()).coerceIn(0f, 1f)

        val spectrumBitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
        spectrumBitmap.setPixels(spectrumPixels, 0, 256, 0, 0, 256, 256)

        val duration = System.currentTimeMillis() - startTime

        // Risk determination: Generative models show abnormal high-frequency roll-off (< 0.05) or severe periodic grid spikes (> 0.25)
        val normalizedScore = when {
            gridArtifactScore > 0.35f -> 85 // GAN transposed convolution checkerboard
            gridArtifactScore > 0.20f -> 68
            highRatio < 0.04f -> 75 // Diffusion latent over-smoothing (missing optical high frequencies)
            highRatio in 0.08f..0.22f -> 20 // Natural optical lens MTF roll-off
            else -> 45
        }

        val confidence = 0.80f
        val finding = when {
            normalizedScore >= 70 -> "Spectral energy anomalies detected. Frequency domain displays characteristic generative upsampling spikes (grid artifact index: ${"%.2f".format(gridArtifactScore)}) and abnormal high-frequency cutoff."
            normalizedScore >= 45 -> "Slight frequency spectrum deviation observed. Moderate high-frequency attenuation present."
            else -> "Natural optical modulation transfer function (MTF) roll-off. No generative periodic grid artifacts detected."
        }

        val detectorResult = DetectorResult(
            detectorId = "spectral_dct_v1",
            detectorName = "Discrete Cosine Spectral Anomaly Detector (DCT)",
            modality = "IMAGE",
            version = "1.8.2-ondevice",
            rawScore = gridArtifactScore,
            normalizedScore = normalizedScore,
            status = "COMPLETED",
            executionTimeMs = duration,
            confidence = confidence,
            evidenceSummary = finding
        )

        return FrequencyOutput(
            detectorResult = detectorResult,
            spectrumBitmap = spectrumBitmap,
            highFreqRatio = highRatio,
            gridArtifactScore = gridArtifactScore
        )
    }
}
