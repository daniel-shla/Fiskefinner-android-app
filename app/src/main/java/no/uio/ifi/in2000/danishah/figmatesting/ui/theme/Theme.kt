package no.uio.ifi.in2000.danishah.figmatesting.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// New FiskeFinner theme based on Figma design
private val FiskeFinnerColorScheme = darkColorScheme(
    primary = LightBlue,
    onPrimary = White,
    primaryContainer = LightBlue,
    onPrimaryContainer = White,
    
    secondary = MediumDarkBlue,
    onSecondary = White,
    secondaryContainer = MediumDarkBlue,
    onSecondaryContainer = White,
    
    tertiary = AccentPink,
    onTertiary = White,
    tertiaryContainer = AccentPink,
    onTertiaryContainer = White,
    
    background = DarkBlue,
    onBackground = White,
    
    surface = MediumDarkBlue,
    onSurface = White,
    surfaceVariant = MediumDarkBlue,
    onSurfaceVariant = LightGray,
    
    error = AccentPink,
    onError = White,
    
    outline = InactiveBlue,
    outlineVariant = InactiveBlue
)


@Composable
fun FigmaTestingTheme(
    content: @Composable () -> Unit
) {
    // Always use our FiskeFinner theme regardless of system dark/light mode
    val colorScheme = FiskeFinnerColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DarkBlue.toArgb() // Set status bar to dark blue
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}