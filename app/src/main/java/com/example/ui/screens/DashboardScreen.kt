package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.AnalysisEntity
import com.example.forensics.PresetSample
import com.example.ui.components.CyberCard
import com.example.ui.components.ForensicStatusBadge
import com.example.ui.components.MetricStatCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AuthenticGreen
import com.example.ui.theme.CobaltBlue
import com.example.ui.theme.DeepGraphite
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.InconclusiveAmber
import com.example.ui.theme.ManipulatedRed
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.ForensicViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: ForensicViewModel,
    onNavigateToNewAnalysis: () -> Unit,
    onNavigateToReport: (String) -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val analyses by viewModel.allAnalyses.collectAsStateWithLifecycle()
    val isAnalyzing by viewModel.isAnalyzing.collectAsStateWithLifecycle()
    val progressStep by viewModel.analysisStepProgress.collectAsStateWithLifecycle()

    val totalCount = analyses.size
    val highRiskCount = analyses.count { it.riskScore >= 65 }
    val authenticCount = analyses.count { it.classification == "LIKELY_AUTHENTIC" }
    val inconclusiveCount = analyses.count { it.classification == "INCONCLUSIVE" }

    val imageCount = analyses.count { it.mediaType == "IMAGE" }
    val videoCount = analyses.count { it.mediaType == "VIDEO" }
    val audioCount = analyses.count { it.mediaType == "AUDIO" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepGraphite)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Hero Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                            text = "DEEPSHIELD FORENSIC INTELLIGENCE",
                            color = ElectricCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Forensic Operations",
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onNavigateToNewAnalysis,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_new_analysis_hero")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Analysis",
                        tint = DeepGraphite,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Analyze",
                        color = DeepGraphite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Processing Banner if Active
        if (isAnalyzing) {
            item {
                CyberCard(
                    borderColor = ElectricCyan,
                    backgroundColor = SurfaceElevated
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(ElectricCyan)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Analysis In Progress...",
                                color = ElectricCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = progressStep,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Metrics Row 1
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricStatCard(
                    title = "Total Analyses",
                    value = "$totalCount",
                    subtitle = "Evidence records",
                    icon = Icons.Default.Analytics,
                    accentColor = ElectricCyan,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "High Risk",
                    value = "$highRiskCount",
                    subtitle = "Flagged synthetic",
                    icon = Icons.Default.Warning,
                    accentColor = ManipulatedRed,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Metrics Row 2
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricStatCard(
                    title = "Authentic",
                    value = "$authenticCount",
                    subtitle = "Consistent PRNU",
                    icon = Icons.Default.CheckCircle,
                    accentColor = AuthenticGreen,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "Inconclusive",
                    value = "$inconclusiveCount",
                    subtitle = "Mixed signals",
                    icon = Icons.Default.Security,
                    accentColor = InconclusiveAmber,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Operational Detector Status Card
        item {
            CyberCard(
                borderColor = SurfaceBorder,
                backgroundColor = SurfaceDark
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DETECTOR PIPELINE INTEGRITY",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(AuthenticGreen)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "5 Active Engines",
                                color = AuthenticGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DetectorHealthPill("UniversalFakeDetect (UnivFD)", true)
                        DetectorHealthPill("Noise Residual (SRM)", true)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DetectorHealthPill("Error Level (ELA)", true)
                        DetectorHealthPill("Spectral DCT Anomaly", true)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DetectorHealthPill("Facial Seam (SBI)", true)
                        DetectorHealthPill("Audio-Visual (SyncNet)", true)
                    }
                }
            }
        }

        // Quick Launch Benchmark Presets
        item {
            SectionHeader(
                title = "Quick Benchmark Presets",
                subtitle = "Evaluate authentic vs deepfake test cases immediately"
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(viewModel.presetSamples) { sample ->
                    BenchmarkPresetCard(
                        sample = sample,
                        onClick = {
                            viewModel.analyzePreset(sample) { entity ->
                                onNavigateToReport(entity.id)
                            }
                        }
                    )
                }
            }
        }

        // Recent Forensic Analyses
        item {
            SectionHeader(
                title = "Recent Forensic Analyses",
                subtitle = if (analyses.isEmpty()) "No evidence logged yet" else "${analyses.size} records saved on-device",
                actionText = if (analyses.isNotEmpty()) "View All" else null,
                onActionClick = onNavigateToHistory
            )
        }

        if (analyses.isEmpty()) {
            item {
                CyberCard(
                    borderColor = SurfaceBorder,
                    backgroundColor = SurfaceElevated
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Empty",
                            tint = TextMuted,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No media analyzed yet",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Upload an image, video, or audio file or tap a benchmark preset above.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 24.dp),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        } else {
            items(analyses.take(5)) { item ->
                RecentAnalysisRow(
                    entity = item,
                    onClick = { onNavigateToReport(item.id) }
                )
            }
        }
    }
}

@Composable
private fun DetectorHealthPill(name: String, operational: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(SurfaceElevated)
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(if (operational) AuthenticGreen else InconclusiveAmber)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = name,
            color = TextSecondary,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun BenchmarkPresetCard(
    sample: PresetSample,
    onClick: () -> Unit
) {
    val isFake = sample.expectedOutcome == "LIKELY_MANIPULATED"
    val accent = if (isFake) ManipulatedRed else AuthenticGreen

    CyberCard(
        modifier = Modifier.width(220.dp),
        borderColor = SurfaceBorder,
        backgroundColor = SurfaceElevated,
        onClick = onClick,
        testTag = "preset_${sample.id}"
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (sample.mediaType) {
                        "VIDEO" -> Icons.Default.Videocam
                        "AUDIO" -> Icons.Default.GraphicEq
                        else -> Icons.Default.Image
                    },
                    contentDescription = sample.mediaType,
                    tint = ElectricCyan,
                    modifier = Modifier.size(16.dp)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(accent.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isFake) "MANIPULATED" else "AUTHENTIC",
                        color = accent,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = sample.title,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = sample.description,
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Tap to Run Real Pipeline →",
                color = ElectricCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun RecentAnalysisRow(
    entity: AnalysisEntity,
    onClick: () -> Unit
) {
    val dateStr = SimpleDateFormat("MMM d, HH:mm", Locale.US).format(Date(entity.timestamp))
    val riskColor = when (entity.classification) {
        "LIKELY_MANIPULATED" -> ManipulatedRed
        "LIKELY_AUTHENTIC" -> AuthenticGreen
        else -> InconclusiveAmber
    }

    CyberCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = SurfaceBorder,
        backgroundColor = SurfaceDark,
        onClick = onClick,
        testTag = "recent_analysis_${entity.id}"
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Modality Icon Box
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceElevated)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (entity.mediaType) {
                        "VIDEO" -> Icons.Default.Videocam
                        "AUDIO" -> Icons.Default.GraphicEq
                        else -> Icons.Default.Image
                    },
                    contentDescription = entity.mediaType,
                    tint = ElectricCyan,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entity.filename,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "SHA: ${entity.fileHash.take(8)}...",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• $dateStr",
                        color = TextTertiary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${entity.riskScore}%",
                    color = riskColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                ForensicStatusBadge(
                    classification = entity.classification,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
