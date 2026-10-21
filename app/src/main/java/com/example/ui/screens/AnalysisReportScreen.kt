package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.entity.AnalysisEntity
import com.example.data.model.AudioSegmentEvidence
import com.example.data.model.DetectorResult
import com.example.data.model.EvidenceItem
import com.example.data.model.TechnicalMetadata
import com.example.data.model.VideoFrameEvidence
import com.example.forensics.ForensicReportParser
import com.example.ui.components.CyberCard
import com.example.ui.components.ForensicStatusBadge
import com.example.ui.components.RiskGauge
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AuthenticGreen
import com.example.ui.theme.AuthenticGreenBg
import com.example.ui.theme.CobaltBlue
import com.example.ui.theme.DeepGraphite
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.InconclusiveAmber
import com.example.ui.theme.InconclusiveAmberBg
import com.example.ui.theme.ManipulatedRed
import com.example.ui.theme.ManipulatedRedBg
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.ForensicViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AnalysisReportScreen(
    analysisId: String,
    viewModel: ForensicViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    BackHandler {
        onNavigateBack()
    }

    LaunchedEffect(analysisId) {
        viewModel.selectAnalysisById(analysisId)
    }

    val selectedEntity by viewModel.selectedAnalysis.collectAsStateWithLifecycle()

    if (selectedEntity == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DeepGraphite),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Loading forensic record...",
                color = TextSecondary,
                fontSize = 14.sp
            )
        }
        return
    }

    val entity = selectedEntity!!
    val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(entity.timestamp))
    val detectors = remember(entity.detectorResultsJson) {
        ForensicReportParser.parseDetectorResults(entity.detectorResultsJson)
    }
    val evidence = remember(entity.evidenceItemsJson) {
        ForensicReportParser.parseEvidenceItems(entity.evidenceItemsJson)
    }
    val metadata = remember(entity.technicalMetadataJson) {
        ForensicReportParser.parseTechnicalMetadata(entity.technicalMetadataJson)
    }
    val videoTimeline = remember(entity.videoTimelineJson) {
        ForensicReportParser.parseVideoTimeline(entity.videoTimelineJson)
    }
    val audioSegments = remember(entity.audioSegmentJson) {
        ForensicReportParser.parseAudioSegments(entity.audioSegmentJson)
    }

    var selectedVisualTab by remember { mutableIntStateOf(0) }
    var heatmapOpacity by remember { mutableFloatStateOf(0.65f) }
    var showFullJsonDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepGraphite)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top App Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("btn_report_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ElectricCyan
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "FORENSIC DOSSIER",
                            color = ElectricCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = entity.id,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Row {
                    IconButton(
                        onClick = {
                            val text = viewModel.getExportText(entity)
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Forensic Report", text))
                            Toast.makeText(context, "Full Forensic Report copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("btn_copy_report")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Report",
                            tint = TextSecondary
                        )
                    }

                    IconButton(
                        onClick = {
                            val text = viewModel.getExportText(entity)
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, text)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Export Forensic Report"))
                        },
                        modifier = Modifier.testTag("btn_share_report")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Report",
                            tint = ElectricCyan
                        )
                    }
                }
            }
        }

        // Primary Risk Gauge & Verdict Card
        item {
            CyberCard(
                borderColor = when (entity.classification) {
                    "LIKELY_MANIPULATED" -> ManipulatedRed.copy(alpha = 0.5f)
                    "LIKELY_AUTHENTIC" -> AuthenticGreen.copy(alpha = 0.5f)
                    else -> InconclusiveAmber.copy(alpha = 0.5f)
                },
                backgroundColor = SurfaceDark
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    RiskGauge(
                        score = entity.riskScore,
                        classification = entity.classification
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ForensicStatusBadge(classification = entity.classification)

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Confidence: ${(entity.confidenceScore * 100).toInt()}%  •  Uncertainty: ±${((1f - entity.confidenceScore) * 100).toInt()}%",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = entity.summaryFinding,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(SurfaceElevated)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Calibration: ${entity.calibrationStatus}",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Visual Forensics Studio (Heatmap / ELA / Noise / Original)
        if (entity.mediaType == "IMAGE") {
            item {
                SectionHeader(
                    title = "Visual Forensics Studio",
                    subtitle = "Inspect localized compression anomalies and sensor noise grain"
                )
                Spacer(modifier = Modifier.height(8.dp))

                CyberCard(
                    borderColor = SurfaceBorder,
                    backgroundColor = SurfaceDark
                ) {
                    Column {
                        // Visual Mode Tabs
                        val tabTitles = listOf("Heatmap", "ELA Difference", "Spatial Noise", "Original")
                        TabRow(
                            selectedTabIndex = selectedVisualTab,
                            containerColor = SurfaceElevated,
                            contentColor = ElectricCyan,
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedVisualTab]),
                                    color = ElectricCyan
                                )
                            }
                        ) {
                            tabTitles.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedVisualTab == index,
                                    onClick = { selectedVisualTab = index },
                                    text = {
                                        Text(
                                            text = title,
                                            fontSize = 11.sp,
                                            fontWeight = if (selectedVisualTab == index) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selectedVisualTab == index) ElectricCyan else TextSecondary
                                        )
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Visual Viewer Box
                        val origFile = File(context.filesDir, "forensic_artifacts/${entity.id}_orig.jpg")
                        val heatmapFile = entity.heatmapPath?.let { File(it) }
                        val elaFile = entity.elaPath?.let { File(it) }
                        val noiseFile = entity.noisePath?.let { File(it) }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            when (selectedVisualTab) {
                                0 -> {
                                    // Heatmap Overlay Mode: Base original + Heatmap layer with variable opacity
                                    if (origFile.exists()) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context).data(origFile).build(),
                                            contentDescription = "Original Base",
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    if (heatmapFile != null && heatmapFile.exists()) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context).data(heatmapFile).build(),
                                            contentDescription = "Heatmap Layer",
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .alpha(heatmapOpacity)
                                        )
                                    }
                                }
                                1 -> {
                                    // ELA Difference Map
                                    if (elaFile != null && elaFile.exists()) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context).data(elaFile).build(),
                                            contentDescription = "ELA Error Difference",
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Text("ELA bitmap not available", color = TextSecondary, fontSize = 12.sp)
                                    }
                                }
                                2 -> {
                                    // Noise Residual (SRM) Map
                                    if (noiseFile != null && noiseFile.exists()) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context).data(noiseFile).build(),
                                            contentDescription = "Noise Residual",
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Text("Noise map not available", color = TextSecondary, fontSize = 12.sp)
                                    }
                                }
                                3 -> {
                                    // Clean Original
                                    if (origFile.exists()) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context).data(origFile).build(),
                                            contentDescription = "Original Media",
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Text("Original media not cached", color = TextSecondary, fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        // Opacity slider for Heatmap Tab
                        if (selectedVisualTab == 0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Heatmap Opacity: ${(heatmapOpacity * 100).toInt()}%",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    modifier = Modifier.width(130.dp)
                                )
                                Slider(
                                    value = heatmapOpacity,
                                    onValueChange = { heatmapOpacity = it },
                                    valueRange = 0.1f..1.0f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = ElectricCyan,
                                        activeTrackColor = ElectricCyan,
                                        inactiveTrackColor = SurfaceBorder
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Video Timeline (if video modality)
        if (videoTimeline.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Video Temporal Frame Timeline",
                    subtitle = "Sampled inter-frame risk scores across video duration"
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(videoTimeline) { frame ->
                        val frameColor = if (frame.frameRiskScore >= 65) ManipulatedRed else AuthenticGreen
                        CyberCard(
                            modifier = Modifier.width(110.dp),
                            borderColor = if (frame.anomalyDetected) ManipulatedRed else SurfaceBorder,
                            backgroundColor = SurfaceElevated
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Frame ${frame.frameIndex}",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${frame.timestampSeconds}s",
                                    color = TextMuted,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${frame.frameRiskScore}%",
                                    color = frameColor,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (frame.anomalyDetected) "ANOMALY" else "STABLE",
                                    color = frameColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Audio Segments (if audio modality)
        if (audioSegments.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Audio Spectral Segments",
                    subtitle = "Acoustic vocoder analysis across temporal intervals"
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(audioSegments) { seg ->
                        val segColor = if (seg.segmentRiskScore >= 65) ManipulatedRed else AuthenticGreen
                        CyberCard(
                            modifier = Modifier.width(130.dp),
                            borderColor = if (seg.segmentRiskScore >= 65) ManipulatedRed else SurfaceBorder,
                            backgroundColor = SurfaceElevated
                        ) {
                            Column {
                                Text(
                                    text = "${seg.startSeconds}s - ${seg.endSeconds}s",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Risk: ${seg.segmentRiskScore}%",
                                    color = segColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "Vocoder: ${"%.2f".format(seg.vocoderAnomalyScore)}",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Complementary Detector Results
        item {
            SectionHeader(
                title = "Complementary Detector Breakdown",
                subtitle = "${detectors.size} analytical pipelines executed"
            )
        }

        items(detectors) { det ->
            val isHigh = det.normalizedScore >= 65
            val detColor = if (isHigh) ManipulatedRed else AuthenticGreen

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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = det.detectorName,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "v${det.version}  •  ${det.executionTimeMs} ms",
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${det.normalizedScore}/100",
                                color = detColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Confidence ${(det.confidence * 100).toInt()}%",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = det.evidenceSummary,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Structured Forensic Explainability Findings (Section 11)
        item {
            SectionHeader(
                title = "Explainable Forensic Reasoning",
                subtitle = "Human-readable answers to primary investigative questions"
            )
        }

        item {
            CyberCard(
                borderColor = CobaltBlue,
                backgroundColor = SurfaceDark
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ExplainabilityBlock("1. What was analyzed?", "File '${entity.filename}' (${entity.mediaType}, ${entity.dimensions}, ${"%.2f".format(entity.fileSizeBytes / 1024.0 / 1024.0)} MB, SHA-256: ${entity.fileHash.take(16)}...).")
                    ExplainabilityBlock("2. Which methods were executed?", detectors.joinToString(", ") { it.detectorName })
                    ExplainabilityBlock("3. What did the methods find?", detectors.joinToString("\n") { "• ${it.detectorName}: ${it.evidenceSummary}" })
                    ExplainabilityBlock("4. How were findings combined?", "Fused via ${entity.fusionMethod} with variance penalty and calibration against benchmark hold-out splits.")
                    ExplainabilityBlock("5. Potential False Positive Confounders?", "Lossy compression (WhatsApp, web transcoding), optical depth-of-field bokeh, and camera portrait beauty smoothing can mimic synthetic artifacts.")
                    ExplainabilityBlock("6. Recommended Next Steps?", "Corroborate with original source raw file or C2PA provenance credentials; inspect device EXIF camera serial tags.")
                }
            }
        }

        // Technical Metadata
        item {
            SectionHeader(
                title = "Technical Evidence & Metadata",
                subtitle = "Cryptographic integrity and container attributes"
            )
        }

        item {
            CyberCard(
                borderColor = SurfaceBorder,
                backgroundColor = SurfaceElevated
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    MetadataRow("Cryptographic SHA-256", entity.fileHash, isMonospace = true)
                    MetadataRow("MIME Type", metadata?.mimeType ?: "image/jpeg")
                    MetadataRow("Dimensions / Stream", entity.dimensions)
                    MetadataRow("Format / Compression", entity.format)
                    MetadataRow("Processing Latency", "${entity.processingDurationMs} ms")
                    MetadataRow("Validation Status", metadata?.headerValidationStatus ?: "PASSED")

                    if (metadata?.exifData?.isNotEmpty() == true) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "EXIF CAMERA TAGS:",
                            color = ElectricCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        for ((k, v) in metadata.exifData) {
                            MetadataRow(k, v)
                        }
                    }
                }
            }
        }

        // Scientific Limitations Notice
        item {
            CyberCard(
                borderColor = SurfaceBorder,
                backgroundColor = SurfaceDark
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Notice",
                        tint = ElectricCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "FORENSIC LIMITATIONS & SCIENTIFIC INTEGRITY",
                            color = ElectricCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = entity.limitationsText,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Action Buttons: Delete and Export JSON
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        val json = viewModel.getExportJson(entity)
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Forensic JSON", json))
                        Toast.makeText(context, "Full Forensic JSON copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                        .testTag("btn_export_json")
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Export JSON",
                        tint = ElectricCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Export JSON",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = {
                        viewModel.deleteAnalysis(entity.id)
                        Toast.makeText(context, "Record and sandbox files deleted", Toast.LENGTH_SHORT).show()
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ManipulatedRedBg),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, ManipulatedRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .testTag("btn_delete_record")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = ManipulatedRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Delete Dossier",
                        color = ManipulatedRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ExplainabilityBlock(question: String, answer: String) {
    Column {
        Text(
            text = question,
            color = ElectricCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = answer,
            color = TextSecondary,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun MetadataRow(label: String, value: String, isMonospace: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 11.sp,
            modifier = Modifier.width(130.dp)
        )
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 11.sp,
            fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
            maxLines = 1
        )
    }
}
