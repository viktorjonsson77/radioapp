package se.radioapp.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RadioColors = lightColorScheme(
    primary = Color(0xFFD71920),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = Color(0xFF410004),
    secondary = Color(0xFF6D3A3C),
    secondaryContainer = Color(0xFFF9DDDC),
    onSecondaryContainer = Color(0xFF281516),
    background = Color(0xFFFFF8F5),
    onBackground = Color(0xFF241918),
    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF241918),
    surfaceVariant = Color(0xFFF5E7E4),
    onSurfaceVariant = Color(0xFF584140),
    outline = Color(0xFF8B706D),
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

@Composable
fun RadioTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = RadioColors, content = content)
}
