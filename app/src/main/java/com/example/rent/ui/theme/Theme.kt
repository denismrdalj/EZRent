package com.example.rent.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Black12 = Color(0x121212FF)

private val DarkColorPalette = darkColors(
    primary = Black12,
    primaryVariant = Color(0xFF7C7C7C),
    secondary = Color(0xFFFF9800),
    onSurface = Color.LightGray
)

private val LightColorPalette = lightColors(
    primary = Color(0xFFFFFFFF),
    primaryVariant = Color(0xFF7C7C7C),
    secondary = Color(0xFFFF9752),
    onSurface = Color.LightGray
    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)

@Composable
fun RentTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
