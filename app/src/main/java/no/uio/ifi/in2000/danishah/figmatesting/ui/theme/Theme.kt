package no.uio.ifi.in2000.danishah.figmatesting.ui.theme

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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
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

// Keep the old color schemes for backward compatibility
private val DarkColorScheme = darkColorScheme(
    primary = Blue700,
    secondary = Green700,
    tertiary = Blue200,
    background = Gray900,
    surface = Gray700,
    onPrimary = Gray100,
    onSecondary = Gray100,
    onTertiary = Gray900,
    onBackground = Gray100,
    onSurface = Gray100
)

private val LightColorScheme = lightColorScheme(
    primary = Blue500,
    secondary = Green500,
    tertiary = Blue200,
    background = Gray100,
    surface = Gray300,
    onPrimary = Gray100,
    onSecondary = Gray100,
    onTertiary = Gray900,
    onBackground = Gray900,
    onSurface = Gray900
)

@Composable
fun FigmaTestingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled by default to use our custom theme
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