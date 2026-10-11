package cl.aldemar.musselapp.ui.muestra

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Anchor
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Straighten
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.aldemar.musselapp.data.model.CentroCultivo
import cl.aldemar.musselapp.ui.theme.MusselAppTheme

// Colores del sistema de diseño (DESIGN.md)
private val Navy = Color(0xFF0F172A)
private val Slate = Color(0xFF334155)
private val Fondo = Color(0xFFF1F5F9)
private val GrisTexto = Color(0xFF94A3B8)
private val GrisNav = Color(0xFF64748B)
private val GrisBoton = Color(0xFFE2E8F0)
private val AzulClaro = Color(0xFFDBEAFE)
private val AzulOscuro = Color(0xFF1E3A8A)

/** Datos del paso 1 del formulario, que se pasan a las siguientes pantallas. */
data class ParametrosMuestra(
    val centroId: String,
    val tren: String,
    val linea: Int,
    val profundidadM: Int,
)

@Composable
fun MuestraScreen(
    centroInicial: CentroCultivo? = null,
    onVolver: () -> Unit = {},
    onContinuar: (ParametrosMuestra) -> Unit = {}
) {
    var centro by remember { mutableStateOf(centroInicial) }
    var tren by remember { mutableStateOf<Pair<String, String>?>(null) }
    var linea by remember { mutableStateOf<Int?>(null) }
    var estrato by remember { mutableStateOf<Estrato?>(null) }

    val centroElegido = centro
    val trenElegido = tren
    val lineaElegida = linea
    val estratoElegido = estrato
    val completo = centroElegido != null && trenElegido != null &&
            lineaElegida != null && estratoElegido != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
    ) {
        BarraSuperior(onVolver)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EncabezadoPasos()

            Tarjeta {
                TituloTarjeta("Ubicación de Muestreo", Icons.Filled.LocationOn)

                Selector(
                    etiqueta = "Centro de Cultivo",
                    valor = centroElegido?.nombre,
                    vacio = "Selecciona un centro",
                    icono = null,
                    opciones = CentroCultivo.disponibles.map { it.nombre },
                    onElegir = { i -> centro = CentroCultivo.disponibles[i] }
                )
                Selector(
                    etiqueta = "Tren de Cultivo (Longline)",
                    valor = trenElegido?.second,
                    vacio = "Selecciona un tren",
                    icono = Icons.Filled.DirectionsBoat,
                    opciones = Trenes.map { it.second },
                    onElegir = { i -> tren = Trenes[i] }
                )
                Selector(
                    etiqueta = "Línea de Siembra",
                    valor = lineaElegida?.let { etiquetaLinea(it) },
                    vacio = "Selecciona una línea",
                    icono = Icons.Filled.Anchor,
                    opciones = Lineas.map { etiquetaLinea(it) },
                    onElegir = { i -> linea = Lineas[i] }
                )
            }

            Tarjeta {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.weight(1f)) {
                        TituloTarjeta("Estrato de Profundidad", Icons.Filled.Straighten)
                    }
                    Text(
                        text = "Cuerda 6m",
                        color = Slate,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GrisBoton)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Estrato.entries.forEach { e ->
                        OpcionEstrato(
                            estrato = e,
                            seleccionado = estratoElegido == e,
                            modifier = Modifier.weight(1f),
                            onClick = { estrato = e }
                        )
                    }
                }
            }
        }

        // Botón de acción principal, siempre visible abajo
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            BotonContinuar(
                habilitado = completo,
                onClick = {
                    if (centroElegido != null && trenElegido != null &&
                        lineaElegida != null && estratoElegido != null
                    ) {
                        onContinuar(
                            ParametrosMuestra(
                                centroId = centroElegido.id,
                                tren = trenElegido.first,
                                linea = lineaElegida,
                                profundidadM = estratoElegido.profundidadM
                            )
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun BarraSuperior(onVolver: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Navy)
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clickable(onClick = onVolver),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = Color.White
            )
        }
        Column(modifier = Modifier.padding(start = 4.dp)) {
            Text(
                text = "MusselApp • Nueva muestra",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "ONLINE / SINCRONIZADO",
                color = GrisTexto,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EncabezadoPasos() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Nueva Muestra de Choritos",
                color = Navy,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "PASO 1 DE 3",
                color = AzulOscuro,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(AzulClaro)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(3) { i ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (i == 0) Navy else GrisBoton)
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("1. Parámetros", "2. Biometría y Fotos", "3. Validación").forEachIndexed { i, texto ->
                Text(
                    text = texto,
                    color = if (i == 0) Navy else GrisNav,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun Tarjeta(contenido: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.5.dp, GrisNav.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        contenido()
    }
}

@Composable
private fun TituloTarjeta(texto: String, icono: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(imageVector = icono, contentDescription = null, tint = Slate)
        Text(
            text = texto,
            color = Navy,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BotonContinuar(habilitado: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (habilitado) Navy else GrisBoton)
            .clickable(enabled = habilitado, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Continuar a Captura",
            color = if (habilitado) Color.White else GrisTexto,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MuestraScreenPreview() {
    MusselAppTheme {
        MuestraScreen(centroInicial = CentroCultivo.disponibles.first())
    }
}
