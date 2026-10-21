package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.evaluation.DatasetBenchmark
import com.example.evaluation.EvaluationCenterData
import com.example.ui.components.CyberCard
import com.example.ui.components.MetricStatCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AuthenticGreen
import com.example.ui.theme.AuthenticGreenBg
import com.example.ui.theme.CobaltBlue
import com.example.ui.theme.DeepGraphite
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.InconclusiveAmber
import com.example.ui.theme.ManipulatedRed
import com.example.ui.theme.ManipulatedRedBg
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvaluationCenterScreen() {
    val benchmarks = EvaluationCenterData.benchmarks
    var selectedBenchmarkId by remember { mutableStateOf(benchmarks.first().id) }

    val currentBenchmark = remember(selectedBenchmarkId) {
        benchmarks.first { it.id == selectedBenchmarkId }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepGraphite)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(ElectricCyan)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "EMPIRICAL BENCHMARK LAB",
                        color = ElectricCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Evaluation Center",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Held-out validation dataset performance, calibration curves, and confusion matrices.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        // Dataset Benchmark Selector Chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(benchmarks) { bm ->
                    val isSelected = bm.id == selectedBenchmarkId
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedBenchmarkId = bm.id },
                        label = { Text(bm.name, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CobaltBlue,
                            selectedLabelColor = TextPrimary,
                            containerColor = SurfaceElevated,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) CobaltBlue else SurfaceBorder
                        )
                    )
                }
            }
        }

        // Selected Dataset Overview Card
        item {
            CyberCard(
                borderColor = CobaltBlue,
                backgroundColor = SurfaceDark
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentBenchmark.name,
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CobaltBlue.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = currentBenchmark.modality,
                                color = ElectricCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Samples: ${currentBenchmark.sampleCount} total (${currentBenchmark.authenticCount} authentic, ${currentBenchmark.manipulatedCount} manipulated)",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Data Split: ${currentBenchmark.testSplitDescription}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Evaluated Methods: ${currentBenchmark.manipulationTypes.joinToString(", ")}",
                        color = ElectricCyan,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Core Metrics Grid
        item {
            SectionHeader(
                title = "Model Performance Metrics",
                subtitle = "Empirical results on held-out test splits"
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricStatCard(
                    title = "ROC-AUC",
                    value = "${"%.3f".format(currentBenchmark.rocAuc)}",
                    subtitle = "Area Under ROC",
                    icon = Icons.Default.Assessment,
                    accentColor = ElectricCyan,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "Balanced Acc",
                    value = "${"%.1f".format(currentBenchmark.balancedAccuracy * 100)}%",
                    subtitle = "Class-balanced",
                    icon = Icons.Default.Speed,
                    accentColor = AuthenticGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricStatCard(
                    title = "F1-Score",
                    value = "${"%.3f".format(currentBenchmark.f1Score)}",
                    subtitle = "Harmonic mean",
                    icon = Icons.Default.CheckCircle,
                    accentColor = CobaltBlue,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "Brier Calibration",
                    value = "${"%.3f".format(currentBenchmark.brierScore)}",
                    subtitle = "Lower is better (0=opt)",
                    icon = Icons.Default.Info,
                    accentColor = InconclusiveAmber,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Confusion Matrix
        item {
            SectionHeader(
                title = "Confusion Matrix",
                subtitle = "Predictions vs ground truth classes"
            )
            Spacer(modifier = Modifier.height(8.dp))

            CyberCard(
                borderColor = SurfaceBorder,
                backgroundColor = SurfaceDark
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "CLASSIFICATION", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(text = "PREDICTED MANIPULATED", color = ManipulatedRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(text = "PREDICTED AUTHENTIC", color = AuthenticGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Row Actual Manipulated
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(SurfaceElevated)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Actual Manipulated", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(110.dp))
                        MatrixCell(label = "TP: ${currentBenchmark.truePositives}", color = AuthenticGreen)
                        MatrixCell(label = "FN: ${currentBenchmark.falseNegatives}", color = ManipulatedRed)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Row Actual Authentic
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(SurfaceElevated)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Actual Authentic", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(110.dp))
                        MatrixCell(label = "FP: ${currentBenchmark.falsePositives}", color = InconclusiveAmber)
                        MatrixCell(label = "TN: ${currentBenchmark.trueNegatives}", color = AuthenticGreen)
                    }
                }
            }
        }

        // Probability Calibration Plot
        item {
            SectionHeader(
                title = "Probability Calibration Curve",
                subtitle = "Predicted Risk Probability vs Observed Empirical Frequency"
            )
            Spacer(modifier = Modifier.height(8.dp))

            CyberCard(
                borderColor = SurfaceBorder,
                backgroundColor = SurfaceDark
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .padding(8.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            // Draw reference grid
                            for (i in 0..4) {
                                val y = h * (i / 4f)
                                drawLine(
                                    color = Color(0xFF1E283E),
                                    start = Offset(0f, y),
                                    end = Offset(w, y),
                                    strokeWidth = 1f
                                )
                                val x = w * (i / 4f)
                                drawLine(
                                    color = Color(0xFF1E283E),
                                    start = Offset(x, 0f),
                                    end = Offset(x, h),
                                    strokeWidth = 1f
                                )
                            }

                            // Draw perfect calibration diagonal (dashed)
                            drawLine(
                                color = Color(0xFF64748B),
                                start = Offset(0f, h),
                                end = Offset(w, 0f),
                                strokeWidth = 2f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )

                            // Plot observed calibration points
                            val pts = currentBenchmark.calibrationCurve
                            for (i in 0 until pts.size - 1) {
                                val p1 = pts[i]
                                val p2 = pts[i + 1]

                                val x1 = p1.binCenter * w
                                val y1 = h - (p1.observedFrequency * h)
                                val x2 = p2.binCenter * w
                                val y2 = h - (p2.observedFrequency * h)

                                drawLine(
                                    color = ElectricCyan,
                                    start = Offset(x1, y1),
                                    end = Offset(x2, y2),
                                    strokeWidth = 3f,
                                    cap = StrokeCap.Round
                                )
                                drawCircle(
                                    color = ElectricCyan,
                                    radius = 5f,
                                    center = Offset(x1, y1)
                                )
                            }
                            // Last point circle
                            val last = pts.last()
                            drawCircle(
                                color = ElectricCyan,
                                radius = 5f,
                                center = Offset(last.binCenter * w, h - (last.observedFrequency * h))
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "0.0 (Authentic)", color = TextMuted, fontSize = 10.sp)
                        Text(text = "Predicted Probability →", color = TextSecondary, fontSize = 10.sp)
                        Text(text = "1.0 (Manipulated)", color = TextMuted, fontSize = 10.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• Cyan curve shows empirical model calibration.\n• Dashed diagonal represents perfect theoretical calibration (Brier Score: ${currentBenchmark.brierScore}).",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MatrixCell(label: String, color: Color) {
    Box(
        modifier = Modifier
            .width(90.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
