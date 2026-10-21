package com.example.ui.screens

import android.os.Build
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CyberCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AuthenticGreen
import com.example.ui.theme.CobaltBlue
import com.example.ui.theme.DeepGraphite
import com.example.ui.theme.ElectricCyan
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

@Composable
fun SettingsScreen(
    viewModel: ForensicViewModel
) {
    val context = LocalContext.current
    var retentionPolicy by remember { mutableStateOf("PERMANENT") }

    val sandboxDir = remember { File(context.filesDir, "forensic_artifacts") }
    var sandboxSizeKb by remember {
        val total = sandboxDir.listFiles()?.sumOf { it.length() } ?: 0L
        mutableStateOf(total / 1024)
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
                        text = "FORENSIC SYSTEM CONFIGURATION",
                        color = ElectricCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Security & Storage Policy",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Manage evidence lifecycle, sandbox storage cleanup, and system diagnostics.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        // Retention Policy Card
        item {
            SectionHeader(
                title = "Evidence Retention Policy",
                subtitle = "Automatic cleanup interval for digital media working copies"
            )
            Spacer(modifier = Modifier.height(8.dp))

            CyberCard(
                borderColor = SurfaceBorder,
                backgroundColor = SurfaceDark
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    RetentionOption(
                        label = "Indefinite Evidence Retention",
                        description = "Persist evidence dossiers and artifacts until manually purged.",
                        selected = retentionPolicy == "PERMANENT",
                        onSelect = { retentionPolicy = "PERMANENT" }
                    )
                    RetentionOption(
                        label = "7-Day Purge Policy",
                        description = "Automatically delete working artifacts older than 7 calendar days.",
                        selected = retentionPolicy == "7_DAYS",
                        onSelect = { retentionPolicy = "7_DAYS" }
                    )
                    RetentionOption(
                        label = "Session Volatility Only",
                        description = "Erase working copies immediately upon session completion.",
                        selected = retentionPolicy == "SESSION",
                        onSelect = { retentionPolicy = "SESSION" }
                    )
                }
            }
        }

        // Storage & Sandbox Cleanup
        item {
            SectionHeader(
                title = "Sandbox Artifact Management",
                subtitle = "Active disk footprint: $sandboxSizeKb KB"
            )
            Spacer(modifier = Modifier.height(8.dp))

            CyberCard(
                borderColor = SurfaceBorder,
                backgroundColor = SurfaceElevated
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Local sandbox directory stores synthesized heatmaps, ELA difference matrices, and downsampled working frames for visual inspection.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                sandboxDir.listFiles()?.forEach { it.delete() }
                                sandboxSizeKb = 0
                                Toast.makeText(context, "Sandbox cached images cleaned", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                                .testTag("btn_clean_sandbox")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CleaningServices,
                                contentDescription = "Clean",
                                tint = ElectricCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Purge Cache",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.clearAllAnalyses()
                                sandboxDir.listFiles()?.forEach { it.delete() }
                                sandboxSizeKb = 0
                                Toast.makeText(context, "All evidence records purged", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ManipulatedRedBg),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, ManipulatedRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .testTag("btn_purge_all")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = "Purge DB",
                                tint = ManipulatedRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Wipe Database",
                                color = ManipulatedRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Hardware Diagnostics Card
        item {
            SectionHeader(
                title = "Hardware & Engine Diagnostics",
                subtitle = "Local Android runtime capabilities"
            )
            Spacer(modifier = Modifier.height(8.dp))

            CyberCard(
                borderColor = SurfaceBorder,
                backgroundColor = SurfaceDark
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DiagRow("Target SDK", "${Build.VERSION.SDK_INT} (Android ${Build.VERSION.RELEASE})")
                    DiagRow("Device Model", "${Build.MANUFACTURER} ${Build.MODEL}")
                    DiagRow("CPU Architectures", Build.SUPPORTED_ABIS.joinToString(", "))
                    DiagRow("Available Memory", "${Runtime.getRuntime().maxMemory() / 1024 / 1024} MB heap limit")
                    DiagRow("ELA Engine", "Operational (In-memory quantization delta)")
                    DiagRow("Laplacian SRM", "Operational (2D Convolution)")
                    DiagRow("DCT Spectral Anomaly", "Operational (2D Fast Cosine)")
                    DiagRow("Facial Seam SBI", "Operational (Elliptical Contour)")
                }
            }
        }

        // About & Methodology
        item {
            CyberCard(
                borderColor = CobaltBlue,
                backgroundColor = SurfaceDark
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Policy,
                            contentDescription = "Policy",
                            tint = ElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "FORENSIC METHODOLOGY & SCIENTIFIC CITATION",
                            color = ElectricCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "DeepShield AI implements explainable multimodal deepfake detection following standards established in CVPR, ICCV, and Interspeech forensic literature. Detection findings combine spatial compression anomalies (ELA), photo-response non-uniformity residuals (SRM), 2D DCT spectral energy spikes, and Self-Blended Image (SBI) facial boundary analysis.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Version 1.0.0-hackathon-release | Open Architecture",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun RetentionOption(
    label: String,
    description: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onSelect)
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(
                selectedColor = ElectricCyan,
                unselectedColor = TextMuted
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = if (selected) TextPrimary else TextSecondary,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                text = description,
                color = TextMuted,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun DiagRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextSecondary, fontSize = 11.sp)
        Text(text = value, color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
    }
}
