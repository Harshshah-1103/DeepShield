package com.example.forensics

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.math.max

object ForensicHeatmapGenerator {

    /**
     * Synthesizes multiple 2D suspicion grids into a polished high-resolution heatmap bitmap.
     * Uses a forensic thermal colormap: Deep Navy -> Cyan -> Green -> Yellow -> Intense Crimson
     */
    fun generateHeatmap(
        targetWidth: Int,
        targetHeight: Int,
        grids: List<Array<FloatArray>>
    ): Bitmap {
        val w = max(32, targetWidth)
        val h = max(32, targetHeight)

        if (grids.isEmpty()) {
            val emptyBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            emptyBitmap.eraseColor(Color.TRANSPARENT)
            return emptyBitmap
        }

        // Interpolate grids to pixel resolution
        val primaryGrid = grids[0]
        val gridRows = primaryGrid.size
        val gridCols = if (gridRows > 0) primaryGrid[0].size else 0

        if (gridRows == 0 || gridCols == 0) {
            val emptyBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            emptyBitmap.eraseColor(Color.TRANSPARENT)
            return emptyBitmap
        }

        val fusedGrid = Array(gridRows) { FloatArray(gridCols) }
        for (r in 0 until gridRows) {
            for (c in 0 until gridCols) {
                var sum = 0f
                var count = 0
                for (g in grids) {
                    if (r < g.size && c < g[r].size) {
                        sum += g[r][c]
                        count++
                    }
                }
                fusedGrid[r][c] = if (count > 0) sum / count else 0f
            }
        }

        // Create low-res bitmap from fused grid, then scale up with bilinear filtering for smooth heatmap contours
        val lowRes = Bitmap.createBitmap(gridCols, gridRows, Bitmap.Config.ARGB_8888)
        val lowResPixels = IntArray(gridCols * gridRows)

        for (r in 0 until gridRows) {
            for (c in 0 until gridCols) {
                val value = fusedGrid[r][c].coerceIn(0f, 1f)
                lowResPixels[r * gridCols + c] = valueToForensicColor(value)
            }
        }
        lowRes.setPixels(lowResPixels, 0, gridCols, 0, 0, gridCols, gridRows)

        // Smooth upsample to target resolution
        val smoothHeatmap = Bitmap.createScaledBitmap(lowRes, w, h, true)
        lowRes.recycle()
        return smoothHeatmap
    }

    private fun valueToForensicColor(v: Float): Int {
        // v in [0..1]
        // 0.0 - 0.25: Deep Navy / Blue to Cyan (Authentic / baseline noise)
        // 0.25 - 0.50: Cyan to Green
        // 0.50 - 0.75: Green to Yellow (Warning / anomalous block)
        // 0.75 - 1.00: Yellow to Hot Crimson (High confidence tamper artifact)
        val alpha = (v * 190 + 35).toInt().coerceIn(35, 230) // Semi-transparent overlay

        return when {
            v < 0.25f -> {
                val t = v / 0.25f
                val r = (0 * (1 - t) + 0 * t).toInt()
                val g = (20 * (1 - t) + 229 * t).toInt()
                val b = (100 * (1 - t) + 255 * t).toInt()
                Color.argb((alpha * 0.4f).toInt(), r, g, b)
            }
            v < 0.50f -> {
                val t = (v - 0.25f) / 0.25f
                val r = (0 * (1 - t) + 0 * t).toInt()
                val g = (229 * (1 - t) + 230 * t).toInt()
                val b = (255 * (1 - t) + 118 * t).toInt()
                Color.argb((alpha * 0.7f).toInt(), r, g, b)
            }
            v < 0.75f -> {
                val t = (v - 0.50f) / 0.25f
                val r = (0 * (1 - t) + 255 * t).toInt()
                val g = (230 * (1 - t) + 179 * t).toInt()
                val b = (118 * (1 - t) + 0 * t).toInt()
                Color.argb((alpha * 0.85f).toInt(), r, g, b)
            }
            else -> {
                val t = (v - 0.75f) / 0.25f
                val r = (255 * (1 - t) + 255 * t).toInt()
                val g = (179 * (1 - t) + 23 * t).toInt()
                val b = (0 * (1 - t) + 68 * t).toInt()
                Color.argb(alpha, r, g, b)
            }
        }
    }
}
