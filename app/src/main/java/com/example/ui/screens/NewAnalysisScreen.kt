package com.example.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.forensics.PresetSample
import com.example.ui.components.CyberCard
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
import com.example.ui.viewmodel.ForensicViewModel

@Composable
fun NewAnalysisScreen(
    viewModel: ForensicViewModel,
    onAnalysisCompleted: (String) -> Unit
) {
    val context = LocalContext.current
    val isAnalyzing by viewModel.isAnalyzing.collectAsStateWithLifecycle()
    val progressStep by viewModel.analysisStepProgress.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    val elaQuality by viewModel.configElaQuality.collectAsStateWithLifecycle()
    val runFreq by viewModel.configFrequencyAnalysis.collectAsStateWithLifecycle()
    val runNoise by viewModel.configNoiseResidual.collectAsStateWithLifecycle()
    val runSeam by viewModel.configFacialSeam.collectAsStateWithLifecycle()

    var showConfigPanel by remember { mutableStateOf(false) }

    // Android Zero-permission Photo/Video Picker
    val visualMediaPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val filename = queryFileName(context, uri) ?: "uploaded_media_${System.currentTimeMillis()}.jpg"
            viewModel.analyzeFromUri(uri, filename) { entity ->
                onAnalysisCompleted(entity.id)
            }
        }
    }

    // Generic Document/Audio File Picker
    val docPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val filename = queryFileName(context, uri) ?: "evidence_file_${System.currentTimeMillis()}.bin"
            viewModel.analyzeFromUri(uri, filename) { entity ->
                onAnalysisCompleted(entity.id)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepGraphite)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header
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
                        text = "NEW FORENSIC INGESTION",
                        color = ElectricCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Initiate Evidence Analysis",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Upload digital media to execute on-device multimodal deepfake detection pipelines.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        // Error message if any
        if (errorMessage != null) {
            item {
                CyberCard(
                    borderColor = ManipulatedRed,
                    backgroundColor = SurfaceElevated
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Error",
                            tint = ManipulatedRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Forensic Ingestion Error",
                                color = ManipulatedRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = errorMessage ?: "",
                                color = TextPrimary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Active Ingestion Spinner
        if (isAnalyzing) {
            item {
                CyberCard(
                    borderColor = ElectricCyan,
                    backgroundColor = SurfaceDark
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = ElectricCyan,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Analyzing Forensic Evidence...",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = progressStep,
                            color = ElectricCyan,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Primary Upload Dropzone Card
        item {
            CyberCard(
                borderColor = CobaltBlue,
                backgroundColor = SurfaceDark,
                testTag = "upload_dropzone"
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(CobaltBlue.copy(alpha = 0.15f))
                            .border(1.dp, CobaltBlue.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = "Upload",
                            tint = ElectricCyan,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Select Digital Forensic Media",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Supported: JPG, PNG, WEBP, MP4, MKV, WAV, MP3\nMax limit: 50 MB / 30 Megapixels",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                visualMediaPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_select_media")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Photos/Videos",
                                tint = DeepGraphite,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Image / Video",
                                color = DeepGraphite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = {
                                docPicker.launch("*/*")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                                .testTag("btn_select_document")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = "Audio/Docs",
                                tint = TextPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Audio / Files",
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Configuration Panel Accordion
        item {
            CyberCard(
                borderColor = SurfaceBorder,
                backgroundColor = SurfaceElevated
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showConfigPanel = !showConfigPanel },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Config",
                                tint = ElectricCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Forensic Detector Configuration",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Icon(
                            imageVector = if (showConfigPanel) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Toggle",
                            tint = TextSecondary
                        )
                    }

                    AnimatedVisibility(visible = showConfigPanel) {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            // ELA Slider
                            Text(
                                text = "Error Level Analysis (ELA) Compression Quality: $elaQuality%",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                            Slider(
                                value = elaQuality.toFloat(),
                                onValueChange = { viewModel.configElaQuality.value = it.toInt() },
                                valueRange = 70f..98f,
                                steps = 27,
                                colors = SliderDefaults.colors(
                                    thumbColor = ElectricCyan,
                                    activeTrackColor = ElectricCyan,
                                    inactiveTrackColor = SurfaceBorder
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Frequency DCT Toggle
                            ConfigSwitchRow(
                                title = "Discrete Cosine Spectral Anomaly (DCT)",
                                subtitle = "Detects periodic upsampling grids from GAN & Diffusion decoders",
                                checked = runFreq,
                                onCheckedChange = { viewModel.configFrequencyAnalysis.value = it }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Noise Residual Toggle
                            ConfigSwitchRow(
                                title = "Spatial Noise & PRNU Residual (SRM)",
                                subtitle = "Measures camera sensor grain vs synthetic smoothing",
                                checked = runNoise,
                                onCheckedChange = { viewModel.configNoiseResidual.value = it }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Facial Seam Toggle
                            ConfigSwitchRow(
                                title = "Facial Seam & Blend Boundary (SBI)",
                                subtitle = "Evaluates contour gradient discontinuities and chrominance shift",
                                checked = runSeam,
                                onCheckedChange = { viewModel.configFacialSeam.value = it }
                            )
                        }
                    }
                }
            }
        }

        // Quick Benchmark Preset Test Library
        item {
            SectionHeader(
                title = "Standard Benchmark Test Samples",
                subtitle = "Run real analysis on genuine camera vs synthetic artifacts"
            )
        }

        items(viewModel.presetSamples) { sample ->
            val isFake = sample.expectedOutcome == "LIKELY_MANIPULATED"
            val badgeColor = if (isFake) ManipulatedRed else AuthenticGreen

            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = SurfaceBorder,
                backgroundColor = SurfaceDark,
                onClick = {
                    viewModel.analyzePreset(sample) { entity ->
                        onAnalysisCompleted(entity.id)
                    }
                },
                testTag = "benchmark_${sample.id}"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceElevated)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (sample.mediaType) {
                                "VIDEO" -> Icons.Default.Videocam
                                "AUDIO" -> Icons.Default.Audiotrack
                                else -> Icons.Default.Image
                            },
                            contentDescription = sample.mediaType,
                            tint = ElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = sample.title,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = if (isFake) "MANIPULATED" else "AUTHENTIC",
                                color = badgeColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = sample.description,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = sample.technicalDetails,
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Security & Privacy Disclosure Notice
        item {
            CyberCard(
                borderColor = SurfaceBorder,
                backgroundColor = SurfaceElevated
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Privacy",
                        tint = ElectricCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "FORENSIC SECURITY & PRIVACY POLICY",
                            color = ElectricCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• All forensic pipelines execute strictly within local Android process sandboxing.\n• Decompression bomb guards prevent memory exhaustion.\n• Uploaded files are tagged with SHA-256 evidence hashes and never transmitted to external APIs without explicit investigator authorization.\n• Temporary artifacts are subject to the local retention policy in Settings.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfigSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 10.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = DeepGraphite,
                checkedTrackColor = ElectricCyan,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = SurfaceElevated
            )
        )
    }
}

private fun queryFileName(context: android.content.Context, uri: Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) {
                    result = it.getString(index)
                }
            }
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/')
        if (cut != null && cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result
}
