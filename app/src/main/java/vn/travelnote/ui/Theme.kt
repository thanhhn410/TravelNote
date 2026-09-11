package vn.travelnote.ui

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val Teal = Color(0xFF0F766E)
private val TealLight = Color(0xFF5EEAD4)
private val Amber = Color(0xFFF59E0B)

private val DarkColors = darkColorScheme(
    primary = TealLight,
    onPrimary = Color(0xFF00332E),
    primaryContainer = Color(0xFF115E59),
    onPrimaryContainer = Color(0xFFCCFBF1),
    secondary = Amber,
    onSecondary = Color(0xFF3A2600),
    background = Color(0xFF0B0F12),
    onBackground = Color(0xFFE6EAEE),
    surface = Color(0xFF11171C),
    onSurface = Color(0xFFE6EAEE),
    surfaceVariant = Color(0xFF1B242B),
    onSurfaceVariant = Color(0xFFB6C2CC),
    error = Color(0xFFFF8A80)
)

private val LightColors = lightColorScheme(
    primary = Teal,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCCFBF1),
    onPrimaryContainer = Color(0xFF042F2A),
    secondary = Color(0xFFB45309),
    onSecondary = Color.White,
    background = Color(0xFFF7F9FA),
    onBackground = Color(0xFF10171C),
    surface = Color.White,
    onSurface = Color(0xFF10171C),
    surfaceVariant = Color(0xFFE7EDF1),
    onSurfaceVariant = Color(0xFF44515B)
)

enum class ThemeMode { SYSTEM, LIGHT, DARK }

@Composable
fun TravelNoteTheme(
    mode: ThemeMode,
    dynamic: Boolean = true,
    content: @Composable () -> Unit
) {
    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val context = LocalContext.current
    val colors = when {
        dynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> DarkColors
        else -> LightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colors.background.toArgb()
            window.navigationBarColor = colors.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !dark
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !dark
        }
    }

    MaterialTheme(colorScheme = colors, content = content)
}
