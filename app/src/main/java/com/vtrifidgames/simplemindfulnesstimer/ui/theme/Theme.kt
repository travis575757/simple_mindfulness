package com.vtrifidgames.simplemindfulnesstimer.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(

)

private val LightColorScheme = lightColorScheme(
    primary = Lime40,
    secondary = LimeGrey40,
    tertiary = Green40,
    secondaryContainer = LimeGrey40,
    background = Color.White,
    surface = Color.White,

    surfaceBright = Color.White,
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color.White,
    surfaceContainerHighest = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainerLowest = Color.White,
    surfaceDim = Color.White,

    /* Other default colors to override
    onPrimary = Lime40,
    primaryContainer = Lime40,
    onPrimaryContainer = Lime40,
    inversePrimary = Lime40,
    onSecondary = Lime40,
    onSecondaryContainer = Lime40,
    onTertiary = Lime40,
    tertiaryContainer = Lime40,
    onTertiaryContainer = Lime40,
    background = Lime40,
    onBackground = Lime40,
    surface = Lime40,
    onSurface = Lime40,
    surfaceVariant = Lime40,
    onSurfaceVariant = Lime40,
    surfaceTint = Lime40,
    inverseOnSurface = Lime40,
    error = Lime40,
    onError = Lime40,
    errorContainer = Lime40,
    onErrorContainer = Lime40,
    outline = Lime40,
    outlineVariant = Lime40,
    scrim = Lime40,
    surfaceBright = Lime40,
    surfaceContainer = Lime40,
    surfaceContainerHigh = Lime40,
    surfaceContainerHighest = Lime40,
    surfaceContainerLow = Lime40,
    surfaceContainerLowest = Lime40,
    surfaceDim = Lime40,*/

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
*/
)

@Composable
fun SimpleMindfulnessTimerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    /*
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    */
    // TODO colors schemes
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}