package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DeepShieldColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = DeepGraphite,
    primaryContainer = SurfaceElevated,
    onPrimaryContainer = ElectricCyan,
    secondary = CobaltBlue,
    onSecondary = TextPrimary,
    secondaryContainer = SurfaceBorder,
    onSecondaryContainer = TextPrimary,
    tertiary = DeepViolet,
    onTertiary = TextPrimary,
    background = DeepGraphite,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceBorder,
    outlineVariant = TextMuted,
    error = ManipulatedRed,
    onError = TextPrimary,
    errorContainer = ManipulatedRedBg,
    onErrorContainer = ManipulatedRed
)

@Composable
fun DeepShieldTheme(
    darkTheme: Boolean = true, // Default to deep cyber theme as required by forensic spec
    dynamicColor: Boolean = false, // Keep the custom forensic cyber aesthetic intact
    content: @Composable () -> Unit
) {
    val colorScheme = DeepShieldColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = DeepGraphite.toArgb()
                window.navigationBarColor = DeepGraphite.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
