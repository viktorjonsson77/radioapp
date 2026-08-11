package se.radioapp.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RadioColors = lightColorScheme(
    primary = Color(0xFFD71920),
    onPrimary = Color.White,
    secondary = Color(0xFF6D3A3C),
    background = Color(0xFFFFF8F5),
    surface = Color(0xFFFFF8F5),
    surfaceVariant = Color(0xFFF7E4E1),
    onSurfaceVariant = Color(0xFF584140),
)

@Composable
fun RadioTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = RadioColors, content = content)
}
