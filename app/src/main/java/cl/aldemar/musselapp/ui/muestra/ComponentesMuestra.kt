package cl.aldemar.musselapp.ui.muestra

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Componentes y datos compartidos por Nueva Muestra, Captura y Vista previa

/**
 * Paleta clara del flujo de muestreo, basada en los colores del diseño (Navy, Slate y fondo F1F5F9).
 * Los campos van en un gris más oscuro que el fondo y las tarjetas, para que se distingan.
 */
internal object Paleta {
    val Fondo = Color(0xFFF1F5F9) // fondo de pantalla
    val Barra = Color(0xFF0F172A) // Navy: barra superior
    val Pie = Color(0xFFFFFFFF) // barra inferior con la acción principal
    val Superficie = Color(0xFFFFFFFF) // tarjetas
    val Campo = Color(0xFFE2E8F0) // campos y filas dentro de una tarjeta
    val Borde = Color(0xFFCBD5E1) // borde de tarjetas
    val BordeCampo = Color(0xFF94A3B8) // borde de opciones sin elegir
    val BordeFuerte = Color(0xFF64748B) // borde de campos y filas, bien marcado
    val Texto = Color(0xFF0F172A)
    val Etiqueta = Color(0xFF334155) // Slate
    val TextoSuave = Color(0xFF64748B)
    val TextoBarra = Color(0xFF94A3B8) // texto secundario sobre la barra Navy
    val Acento = Color(0xFF1E3A8A) // etiquetas y bordes de lo ya elegido
    val AcentoSuave = Color(0xFFDBEAFE) // fondo de lo elegido y de los recuadros de íconos
    val Primario = Color(0xFF0F172A) // botón principal y opción seleccionada
    val SobrePrimario = Color(0xFFFFFFFF)
    val Error = Color(0xFFBA1A1A)
}

internal val RojoError = Paleta.Error

internal enum class Estrato(val rango: String, val nombre: String, val profundidadM: Int) {
    SUPERFICIE("0-2m", "Superficie", 1),
    MEDIA("2-4m", "Media", 3),
    FONDO("4-6m", "Fondo", 5);

    companion object {
        fun desde(profundidadM: Int): Estrato = when {
            profundidadM <= 2 -> SUPERFICIE
            profundidadM <= 4 -> MEDIA
            else -> FONDO
        }
    }
}

// TODO: mover trenes y líneas al repositorio cuando existan datos reales
// El código (A, B, C) es el que usa Muestra.tren; el texto es solo para mostrar
internal val Trenes = listOf(
    "A" to "Tren A - Longline Sur",
    "B" to "Tren B - Longline Centro",
    "C" to "Tren C - Longline Norte",
)
internal val Lineas = (1..12).toList()

internal fun etiquetaLinea(linea: Int) = "Línea %02d (Cuerda colectora 6m)".format(linea)

internal fun nombreTren(codigo: String) = Trenes.firstOrNull { it.first == codigo }?.second ?: "Tren $codigo"

// Tarjeta de la paleta oscura
@Composable
internal fun Tarjeta(
    sinPadding: Boolean = false,
    espacio: Dp = 14.dp,
    contenido: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Paleta.Superficie)
            .border(1.5.dp, Paleta.Borde, RoundedCornerShape(12.dp))
            .padding(if (sinPadding) 0.dp else 16.dp),
        verticalArrangement = Arrangement.spacedBy(if (sinPadding) 0.dp else espacio)
    ) {
        contenido()
    }
}

// Lista desplegable: sin elegir se ve neutra con el texto "Selecciona…";
// al elegir un valor el borde y la etiqueta toman el color de acento
@Composable
internal fun Selector(
    etiqueta: String,
    valor: String?,
    vacio: String,
    icono: ImageVector?,
    opciones: List<String>,
    error: String? = null, // si no es null, el campo se marca en rojo (como isError)
    onElegir: (Int) -> Unit
) {
    var abierto by remember { mutableStateOf(false) }
    val elegido = valor != null

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = etiqueta,
            color = if (elegido) Paleta.Acento else Paleta.Etiqueta,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (elegido) Paleta.AcentoSuave else Paleta.Campo)
                    .border(
                        1.5.dp,
                        when {
                            error != null -> Paleta.Error
                            elegido -> Paleta.Acento
                            else -> Paleta.BordeFuerte
                        },
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { abierto = true }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icono != null) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Paleta.Primario),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icono,
                            contentDescription = null,
                            tint = Paleta.SobrePrimario,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Text(
                    text = valor ?: vacio,
                    color = if (elegido) Paleta.Texto else Paleta.Etiqueta,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "Abrir lista",
                    tint = Paleta.Etiqueta
                )
            }
            DropdownMenu(
                expanded = abierto,
                onDismissRequest = { abierto = false },
                containerColor = Paleta.Superficie
            ) {
                opciones.forEachIndexed { i, opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion, color = Paleta.Texto) },
                        onClick = {
                            onElegir(i)
                            abierto = false
                        }
                    )
                }
            }
        }
        // Mensaje bajo el campo (como supportingText)
        if (error != null) {
            TextoError(error)
        }
    }
}

/** Texto rojo que explica qué corregir en un campo. */
@Composable
internal fun TextoError(texto: String) {
    Text(
        text = texto,
        color = Paleta.Error,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
internal fun OpcionEstrato(
    estrato: Estrato,
    seleccionado: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (seleccionado) Paleta.Primario else Paleta.Campo)
            .border(
                width = 1.5.dp,
                color = if (seleccionado) Paleta.Primario else Paleta.BordeCampo,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = estrato.rango,
            color = if (seleccionado) Paleta.SobrePrimario else Paleta.Texto,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = estrato.nombre,
            color = if (seleccionado) Paleta.TextoBarra else Paleta.TextoSuave,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}
