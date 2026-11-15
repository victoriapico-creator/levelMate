package com.example.notesbysubject.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = OrangePrimaryDark,
    onPrimary = DarkGrey,
    primaryContainer = OrangeContainerDark,
    onPrimaryContainer = OnOrangeContainerDark,
    secondary = Color(0xFFBDBDBD),
    onSecondary = DarkGrey,
    background = DarkGreyBackground,
    onBackground = LightGreyText,
    surface = DarkGreySurface,
    onSurface = LightGreyText,
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFCCCCCC)
)

private val LightColorScheme = lightColorScheme(
    primary = OrangePrimary,
    onPrimary = Color.White,
    primaryContainer = OrangeContainerLight,
    onPrimaryContainer = OnOrangeContainerLight,
    secondary = DarkGreySecondary,
    onSecondary = Color.White,
    background = LightGreyBackground,
    onBackground = DarkGrey,
    surface = Color.White,
    onSurface = DarkGrey,
    surfaceVariant = Color(0xFFEEEEEE),
    onSurfaceVariant = DarkGreySecondary
)

@Composable
fun NotesBySubjectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
