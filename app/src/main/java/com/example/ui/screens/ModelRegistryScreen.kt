package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forensics.ModelRegistry
import com.example.forensics.ModelRegistryEntry
import com.example.forensics.ModelStatus
import com.example.ui.components.CyberCard
import com.example.ui.theme.AuthenticGreen
import com.example.ui.theme.AuthenticGreenBg
import com.example.ui.theme.CobaltBlue
import com.example.ui.theme.DeepGraphite
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.InconclusiveAmber
import com.example.ui.theme.InconclusiveAmberBg
import com.example.ui.theme.ManipulatedRed
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelRegistryScreen() {
    val context = LocalContext.current
    var selectedModality by remember { mutableStateOf("ALL") }

    val filteredModels = remember(selectedModality) {
        if (selectedModality == "ALL") {
            ModelRegistry.entries
        } else {
            ModelRegistry.entries.filter { it.modality.contains(selectedModality) }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepGraphite)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
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
                        text = "RESEARCH ARCHITECTURE CATALOG",
                        color = ElectricCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Model Registry & Matrix",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Operational status, paper provenance, licenses, and known limitations across all cataloged detectors.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        // Modality Filters
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val modalities = listOf("ALL" to "All Detectors", "IMAGE" to "Image", "VIDEO" to "Video", "AUDIO" to "Audio", "AUDIO_VIDEO" to "Multimodal")
                items(modalities) { (key, label) ->
                    val isSelected = selectedModality == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedModality = key },
                        label = { Text(label, fontSize = 11.sp) },
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

        // Integrity Statement Card
        item {
            CyberCard(
                borderColor = SurfaceBorder,
                backgroundColor = SurfaceDark
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Status",
                        tint = ElectricCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "FORENSIC INTEGRITY NOTICE (SECTION 9)",
                            color = ElectricCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "DeepShield AI strictly separates active on-device operational engines from external research pipelines requiring multi-gigabyte PyTorch/CUDA weights. Unimplemented detectors are honestly documented with their research links and requirements.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Model List
        items(filteredModels) { model ->
            ModelEntryCard(
                model = model,
                onOpenRepo = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(model.repositoryUrl))
                    context.startActivity(intent)
                }
            )
        }
    }
}

@Composable
private fun ModelEntryCard(
    model: ModelRegistryEntry,
    onOpenRepo: () -> Unit
) {
    val statusColor = when (model.status) {
        ModelStatus.OPERATIONAL -> AuthenticGreen
        ModelStatus.CHECKPOINT_REQUIRED -> InconclusiveAmber
        ModelStatus.SERVER_BACKEND_REQUIRED -> CobaltBlue
        ModelStatus.UNAVAILABLE -> ManipulatedRed
    }

    val statusBg = when (model.status) {
        ModelStatus.OPERATIONAL -> AuthenticGreenBg
        ModelStatus.CHECKPOINT_REQUIRED -> InconclusiveAmberBg
        ModelStatus.SERVER_BACKEND_REQUIRED -> CobaltBlue.copy(alpha = 0.2f)
        ModelStatus.UNAVAILABLE -> ManipulatedRed.copy(alpha = 0.2f)
    }

    CyberCard(
        borderColor = if (model.status.isOperational) SurfaceBorder else SurfaceBorder.copy(alpha = 0.6f),
        backgroundColor = SurfaceDark,
        testTag = "model_card_${model.id}"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = model.name,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${model.institution}  •  ${model.license}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusBg)
                        .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = model.status.label,
                        color = statusColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Architecture & Signal
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(SurfaceElevated)
                    .padding(8.dp)
            ) {
                Text(
                    text = "PRIMARY SIGNAL: ${model.primarySignal}",
                    color = ElectricCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Architecture: ${model.architecture}",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            // Specs
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Modality: ${model.modality}", color = TextMuted, fontSize = 11.sp)
                Text(text = "Inputs: ${model.supportedInputs}", color = TextMuted, fontSize = 11.sp)
            }

            // Limitations
            Text(
                text = "Known Limitations: ${model.knownLimitations}",
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            // GitHub link
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(onClick = onOpenRepo)
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = "Repository",
                    tint = ElectricCyan,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = model.repositoryUrl,
                    color = ElectricCyan,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
            }
        }
    }
}
