package cl.aldemar.musselapp.ui.muestra

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Anchor
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.aldemar.musselapp.data.model.CentroCultivo
import cl.aldemar.musselapp.ui.theme.MusselAppTheme

/** Datos del paso 1 del formulario, que se pasan a las siguientes pantallas. */
data class ParametrosMuestra(
    val centroId: String,
    val tren: String,
    val linea: Int,
    val profundidadM: Int,
)

@Composable
fun MuestraScreen(
    // Centro con el que el operario inició sesión: queda elegido de forma automática
    centroInicial: CentroCultivo? = null,
    onVolver: () -> Unit = {},
    onContinuar: (ParametrosMuestra) -> Unit = {},
    vm: MuestraViewModel = viewModel()
) {
    // Observamos el estado del ViewModel
    val state by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Deja elegido el centro del login (no pisa un centro que el operario ya cambió)
    LaunchedEffect(centroInicial) {
        vm.iniciar(centroInicial)
    }

    // Cuando el ViewModel confirma que todo es válido, se muestra el mensaje y se avanza
    LaunchedEffect(state.parametrosValidos) {
        val parametros = state.parametrosValidos ?: return@LaunchedEffect
        state.mensaje?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
        vm.navegacionRealizada()
        onContinuar(parametros)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paleta.Fondo)
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
                    valor = state.centro?.nombre,
                    vacio = "Selecciona un centro",
                    icono = Icons.Filled.Business,
                    opciones = CentroCultivo.disponibles.map { it.nombre },
                    error = state.errorCentro,
                    onElegir = { i -> vm.cambiarCentro(CentroCultivo.disponibles[i]) }
                )
                Selector(
                    etiqueta = "Tren de Cultivo (Longline)",
                    valor = state.tren?.let { nombreTren(it) },
                    vacio = "Selecciona un tren",
                    icono = Icons.Filled.DirectionsBoat,
                    opciones = Trenes.map { it.second },
                    error = state.errorTren,
                    onElegir = { i -> vm.cambiarTren(Trenes[i].first) }
                )
                Selector(
                    etiqueta = "Línea de Siembra",
                    valor = state.linea?.let { etiquetaLinea(it) },
                    vacio = "Selecciona una línea",
                    icono = Icons.Filled.Anchor,
                    opciones = Lineas.map { etiquetaLinea(it) },
                    error = state.errorLinea,
                    onElegir = { i -> vm.cambiarLinea(Lineas[i]) }
                )
            }

            Tarjeta {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.weight(1f)) {
                        TituloTarjeta("Estrato de Profundidad", Icons.Filled.Straighten)
                    }
                    Text(
                        text = "Cuerda 6m",
                        color = Paleta.Etiqueta,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Paleta.Borde)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Estrato.entries.forEach { e ->
                        OpcionEstrato(
                            estrato = e,
                            seleccionado = state.profundidadM == e.profundidadM,
                            modifier = Modifier.weight(1f),
                            onClick = { vm.cambiarEstrato(e.profundidadM) }
                        )
                    }
                }
                state.errorEstrato?.let { TextoError(it) }
            }
        }

        // Botón de acción principal, siempre visible abajo
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Paleta.Pie)
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            // El botón siempre se puede presionar: el ViewModel decide si hay errores
            BotonContinuar(onClick = vm::guardar)
            BotonLimpiar(onClick = vm::limpiar)
        }
    }
}

@Composable
private fun BarraSuperior(onVolver: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Paleta.Barra)
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
                color = Paleta.TextoBarra,
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
                color = Paleta.Texto,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "PASO 1 DE 3",
                color = Paleta.Acento,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Paleta.AcentoSuave)
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
                        .background(if (i == 0) Paleta.Acento else Paleta.BordeCampo)
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("1. Parámetros", "2. Foto", "3. Validación").forEachIndexed { i, texto ->
                Text(
                    text = texto,
                    color = if (i == 0) Paleta.Texto else Paleta.Etiqueta,
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
private fun TituloTarjeta(texto: String, icono: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Paleta.Primario),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = Paleta.SobrePrimario,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = texto,
            color = Paleta.Texto,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BotonContinuar(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Paleta.Primario)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Continuar a Captura",
            color = Paleta.SobrePrimario,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BotonLimpiar(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Limpiar formulario",
            color = Paleta.TextoSuave,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
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
