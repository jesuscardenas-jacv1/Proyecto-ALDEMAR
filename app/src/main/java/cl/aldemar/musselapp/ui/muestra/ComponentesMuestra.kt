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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Componentes y datos compartidos por Nueva Muestra y Vista previa

private val Navy = Color(0xFF0F172A)
private val Slate = Color(0xFF334155)
private val Fondo = Color(0xFFF1F5F9)
private val GrisTexto = Color(0xFF94A3B8)
private val GrisNav = Color(0xFF64748B)

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

// Lista desplegable: muestra el valor elegido y abre un menú con las opciones
@Composable
internal fun Selector(
    etiqueta: String,
    valor: String?,
    vacio: String,
    icono: ImageVector?,
    opciones: List<String>,
    onElegir: (Int) -> Unit
) {
    var abierto by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = etiqueta,
            color = Slate,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Fondo)
                    .border(1.5.dp, GrisNav.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .clickable { abierto = true }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icono != null) {
                    Icon(imageVector = icono, contentDescription = null, tint = Slate)
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Text(
                    text = valor ?: vacio,
                    color = if (valor != null) Color.Black else GrisNav,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "Abrir lista",
                    tint = Slate
                )
            }
            DropdownMenu(
                expanded = abierto,
                onDismissRequest = { abierto = false }
            ) {
                opciones.forEachIndexed { i, opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion) },
                        onClick = {
                            onElegir(i)
                            abierto = false
                        }
                    )
                }
            }
        }
    }
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
            .background(if (seleccionado) Navy else Color.White)
            .border(
                width = 1.5.dp,
                color = if (seleccionado) Navy else GrisNav.copy(alpha = 0.4f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = estrato.rango,
            color = if (seleccionado) Color.White else Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = estrato.nombre,
            color = if (seleccionado) GrisTexto else GrisNav,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}
