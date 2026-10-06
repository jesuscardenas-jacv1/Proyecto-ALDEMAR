package cl.aldemar.musselapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Esquema fijo con la paleta del proyecto: sin color dinámico ni modo oscuro,
// para que la app se vea igual en todos los equipos de terreno.
private val MusselColorScheme = lightColorScheme(
    primary = Principal,
    onPrimary = Color.White,
    secondary = Secundario,
    onSecondary = Color.White,
    background = Fondo,
    onBackground = Texto,
    surface = Superficie,
    onSurface = Texto,
    surfaceVariant = SuperficieCampo,
    onSurfaceVariant = Secundario,
    surfaceContainerLow = Fondo,
    surfaceContainer = SuperficieCampo,
    outline = Adicional,
    error = Error,
)

@Composable
fun MusselAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MusselColorScheme,
        typography = Typography,
        content = content
    )
}
