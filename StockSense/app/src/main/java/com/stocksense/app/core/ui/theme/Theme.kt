package com.stocksense.app.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val StockSenseDarkColorScheme = darkColorScheme(
    primary = AccentGreen,
    onPrimary = BackgroundDark,
    primaryContainer = AccentGreenDim,
    onPrimaryContainer = AccentGreen,
    secondary = InfoBlue,
    onSecondary = TextPrimary,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = CardDark,
    onSurfaceVariant = TextSecondary,
    error = LossRed,
    onError = TextPrimary,
    outline = CardBorder,
)

@Composable
fun StockSenseTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StockSenseDarkColorScheme,
        typography = Typography,
        content = content
    )
}