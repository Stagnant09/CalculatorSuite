package org.calculator

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.calculator.ui.screens.XYScreen
import org.calculator.ui.screens.XYViewmodel
import org.calculator.ui.theme.*

private val DarkColorScheme = darkColorScheme(
    primary = primaryDark,
    secondary = secondaryDark,
    tertiary = tertiaryDark,
    background = backgroundDark,
    surface = surfaceDark,
    onPrimary = onPrimaryDark,
    onSecondary = onSecondaryDark,
    onTertiary = onTertiaryDark,
    onBackground = onBackgroundDark,
    onSurface = onSurfaceDark,
)

private val LightColorScheme = lightColorScheme(
    primary = primaryLight,
    secondary = secondaryLight,
    tertiary = tertiaryLight,
    background = backgroundLight,
    surface = surfaceLight,
    onPrimary = onPrimaryLight,
    onSecondary = onSecondaryLight,
    onTertiary = onTertiaryLight,
    onBackground = onBackgroundLight,
    onSurface = onSurfaceLight,
)

@Composable
@Preview
fun App() {
    val useDarkTheme = isSystemDarkTheme()
    val colorScheme = if (useDarkTheme) DarkColorScheme else LightColorScheme
    val viewModel = remember { XYViewmodel() }
    MaterialTheme(
        colorScheme = colorScheme,
        content = { XYScreen(viewModel) }
    )
}

// Common implementation for desktop
@Composable
fun isSystemDarkTheme(): Boolean = isSystemInDarkTheme()