package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.AnalysisReportScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EvaluationCenterScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.ModelRegistryScreen
import com.example.ui.screens.NewAnalysisScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.CobaltBlue
import com.example.ui.theme.DeepGraphite
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.ForensicViewModel

enum class DeepShieldScreen(val label: String, val icon: ImageVector, val tag: String) {
    DASHBOARD("Dashboard", Icons.Default.Analytics, "nav_dashboard"),
    NEW_ANALYSIS("Analyze", Icons.Default.AddCircle, "nav_new_analysis"),
    HISTORY("History", Icons.Default.History, "nav_history"),
    MODELS("Models", Icons.Default.Memory, "nav_models"),
    EVALUATION("Evaluation", Icons.Default.Assessment, "nav_evaluation"),
    SETTINGS("Settings", Icons.Default.Settings, "nav_settings")
}

@Composable
fun DeepShieldApp(viewModel: ForensicViewModel) {
    var currentScreen by rememberSaveable { mutableStateOf(DeepShieldScreen.DASHBOARD) }
    var activeReportId by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepGraphite)
            .statusBarsPadding(),
        bottomBar = {
            if (activeReportId == null) {
                NavigationBar(
                    containerColor = SurfaceDark,
                    contentColor = TextPrimary,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SurfaceBorder)
                        .navigationBarsPadding()
                ) {
                    DeepShieldScreen.entries.forEach { screen ->
                        val selected = currentScreen == screen
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                activeReportId = null
                                currentScreen = screen
                            },
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.label,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = screen.label,
                                    fontSize = 10.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = DeepGraphite,
                                selectedTextColor = ElectricCyan,
                                indicatorColor = ElectricCyan,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextMuted
                            ),
                            modifier = Modifier.testTag(screen.tag)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (activeReportId != null) {
                AnalysisReportScreen(
                    analysisId = activeReportId!!,
                    viewModel = viewModel,
                    onNavigateBack = { activeReportId = null }
                )
            } else {
                when (currentScreen) {
                    DeepShieldScreen.DASHBOARD -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToNewAnalysis = { currentScreen = DeepShieldScreen.NEW_ANALYSIS },
                        onNavigateToReport = { id -> activeReportId = id },
                        onNavigateToHistory = { currentScreen = DeepShieldScreen.HISTORY }
                    )
                    DeepShieldScreen.NEW_ANALYSIS -> NewAnalysisScreen(
                        viewModel = viewModel,
                        onAnalysisCompleted = { id -> activeReportId = id }
                    )
                    DeepShieldScreen.HISTORY -> HistoryScreen(
                        viewModel = viewModel,
                        onNavigateToReport = { id -> activeReportId = id }
                    )
                    DeepShieldScreen.MODELS -> ModelRegistryScreen()
                    DeepShieldScreen.EVALUATION -> EvaluationCenterScreen()
                    DeepShieldScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
