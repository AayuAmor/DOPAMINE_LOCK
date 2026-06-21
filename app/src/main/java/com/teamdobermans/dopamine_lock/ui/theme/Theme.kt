package com.teamdobermans.dopamine_lock.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DopamineDarkColorScheme = darkColorScheme(
    primary = DopamineWhite,
    onPrimary = DopamineBlack,
    primaryContainer = DopamineSubtle,
    onPrimaryContainer = DopamineWhite,
    secondary = DopamineGrey,
    onSecondary = DopamineBlack,
    secondaryContainer = DopamineCard,
    onSecondaryContainer = DopamineGrey,
    tertiary = DopamineDim,
    onTertiary = DopamineWhite,
    background = DopamineBlack,
    onBackground = DopamineWhite,
    surface = DopamineSurface,
    onSurface = DopamineWhite,
    surfaceVariant = DopamineCard,
    onSurfaceVariant = DopamineGrey,
    outline = DopamineBorder,
    outlineVariant = DopamineDivider,
    error = DopamineError,
    onError = DopamineBlack,
    errorContainer = DopamineCard,
    onErrorContainer = DopamineError,
    inverseSurface = DopamineWhite,
    inverseOnSurface = DopamineBlack,
    scrim = DopamineBlack
)

@Composable
fun DOPAMINE_LOCKTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DopamineDarkColorScheme,
        typography = Typography,
        content = content
    )
}
