package com.cs346.intellical.ui.theme

import android.app.Activity
import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

class ThemeState(context: Context) {
    private val prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
    
    var isDarkMode by mutableStateOf(prefs.getBoolean("is_dark_mode", true))
        private set

    fun toggleDarkMode() {
        isDarkMode = !isDarkMode
        prefs.edit().putBoolean("is_dark_mode", isDarkMode).apply()
    }
}

val LocalThemeState = staticCompositionLocalOf<ThemeState> { error("No ThemeState provided") }

private val DarkColorScheme = darkColorScheme(
    primary = CosmicGreen,
    onPrimary = SpaceBlack,
    primaryContainer = CosmicGreenDark,
    onPrimaryContainer = CosmicGreenLight,

    secondary = NebulaTeal,
    onSecondary = SpaceBlack,
    secondaryContainer = NebulaTealDark,
    onSecondaryContainer = NebulaTealLight,

    tertiary = StardustGold,
    onTertiary = SpaceBlack,
    tertiaryContainer = StardustGoldDark,
    onTertiaryContainer = StardustGoldLight,

    error = HealthRed,
    onError = SpaceBlack,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = HealthRedLight,

    background = SpaceBlack,
    onBackground = StarWhite,
    surface = SpaceNavy,
    onSurface = StarWhite,
    surfaceVariant = SpaceGray,
    onSurfaceVariant = StarGray,

    outline = SpaceMist,
    outlineVariant = SpaceGray,

    inverseSurface = Color(0xFFF5F5F5), // Light background
    inverseOnSurface = Color(0xFF000000),
    inversePrimary = CosmicGreenDark
)

private val LightColorScheme = lightColorScheme(
    primary = CosmicGreen,
    onPrimary = Color.White,
    primaryContainer = CosmicGreenLight,
    onPrimaryContainer = Color.Black,

    secondary = NebulaTeal,
    onSecondary = Color.White,
    secondaryContainer = NebulaTealLight,
    onSecondaryContainer = Color.Black,

    tertiary = StardustGold,
    onTertiary = Color.Black,
    tertiaryContainer = StardustGoldLight,
    onTertiaryContainer = Color.Black,

    error = HealthRed,
    onError = Color.White,
    errorContainer = HealthRedLight,
    onErrorContainer = Color.Black,

    background = Color(0xFFF5F5F5),
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFE0E0E0),
    onSurfaceVariant = Color.DarkGray,

    outline = Color.Gray,
    outlineVariant = Color.LightGray,

    inverseSurface = SpaceNavy,
    inverseOnSurface = StarWhite,
    inversePrimary = CosmicGreen
)

@Composable
fun IntelliCalTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val themeState = remember { ThemeState(context) }

    val darkTheme = themeState.isDarkMode
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalThemeState provides themeState) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}