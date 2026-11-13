package com.baabearianstudios.cannoncrossfire.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = AmberGold,
    onPrimary = Color.Black,
    secondary = AmberDeep,
    onSecondary = Color.White,
    tertiary = AmberBright,
    error = CrimsonDanger,
    onError = Color.White,
    background = ObsidianDark,
    onBackground = WarmIvory,
    surface = SlatePanel,
    onSurface = WarmIvory,
    onSurfaceVariant = MutedSilver
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)

@Composable
fun CannonCrossfireTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
