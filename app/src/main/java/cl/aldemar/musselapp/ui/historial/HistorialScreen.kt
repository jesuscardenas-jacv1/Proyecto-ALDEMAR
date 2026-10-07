package cl.aldemar.musselapp.ui.historial

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.aldemar.musselapp.data.model.CentroCultivo
import cl.aldemar.musselapp.data.model.EstadoMuestra
import cl.aldemar.musselapp.data.model.Muestra
import cl.aldemar.musselapp.data.model.Rol
import cl.aldemar.musselapp.data.model.Usuario
import cl.aldemar.musselapp.data.repository.FakeMuestraRepository
import cl.aldemar.musselapp.ui.componentes.BarraSuperiorMussel
import cl.aldemar.musselapp.ui.componentes.BotonNuevaMuestra
import cl.aldemar.musselapp.ui.componentes.ChipEstado
import cl.aldemar.musselapp.ui.componentes.NavegacionInferior
import cl.aldemar.musselapp.ui.principal.Destino
import cl.aldemar.musselapp.ui.theme.AcentoObservado
import cl.aldemar.musselapp.ui.theme.AcentoPendiente
import cl.aldemar.musselapp.ui.theme.AcentoValidado
import cl.aldemar.musselapp.ui.theme.EstadoObservadoFondo
import cl.aldemar.musselapp.ui.theme.EstadoObservadoTexto
import cl.aldemar.musselapp.ui.theme.EstadoValidadoFondo
import cl.aldemar.musselapp.ui.theme.EstadoValidadoTexto
import cl.aldemar.musselapp.ui.theme.MusselAppTheme
import cl.aldemar.musselapp.util.Fechas
import java.util.Locale

/** Punto de entrada con estado: conecta el ViewModel con la UI. */
@Composable
fun HistorialRoute(
    usuario: Usuario,
    centro: CentroCultivo,
    onNavegar: (Destino) -> Unit,
    onVerMuestra: (Long) -> Unit,
    onRecontar: (Long) -> Unit,
    onCerrarSesion: () -> Unit,
    viewModel: HistorialViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HistorialScreen(
        usuario = usuario,
        centro = centro,
        uiState = uiState,
        onBusquedaChange = viewModel::onBusquedaChange,
        onFiltroChange = viewModel::onFiltroChange,
        onPeriodoChange = viewModel::onPeriodoChange,
        onAlternarOrden = viewModel::onAlternarOrden,
        onRestablecerFiltros = viewModel::onRestablecerFiltros,
        onSincronizar = viewModel::sincronizar,
        onMensajeMostrado = viewModel::onMensajeMostrado,
        onNavegar = onNavegar,
        onVerMuestra = onVerMuestra,
        onRecontar = onRecontar,
        onCerrarSesion = onCerrarSesion,
    )
}

/** UI sin estado del Historial (diseño "6. Historial" de Stitch). */
@Composable
fun HistorialScreen(
    usuario: Usuario,
    centro: CentroCultivo,
    uiState: HistorialUiState,
    onBusquedaChange: (String) -> Unit,
    onFiltroChange: (FiltroEstado) -> Unit,
    onPeriodoChange: (Periodo) -> Unit,
    onAlternarOrden: () -> Unit,
    onRestablecerFiltros: () -> Unit,
    onSincronizar: () -> Unit,
    onMensajeMostrado: () -> Unit,
    onNavegar: (Destino) -> Unit,
    onVerMuestra: (Long) -> Unit,
    onRecontar: (Long) -> Unit,
    onCerrarSesion: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            onMensajeMostrado()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BarraSuperiorMussel(
                seccion = "Historial",
                usuario = usuario,
                centro = centro,
                pendientesSincronizar = uiState.pendientesSincronizar,
                sincronizando = uiState.sincronizando,
                onSincronizar = onSincronizar,
                onCerrarSesion = onCerrarSesion,
            )
        },
        bottomBar = { NavegacionInferior(rol = usuario.rol, seleccionado = Destino.HISTORIAL, onNavegar = onNavegar) },
        floatingActionButton = {
            if (usuario.rol == Rol.OPERARIO) BotonNuevaMuestra(onClick = { onNavegar(Destino.NUEVA_MUESTRA) })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { BarraBusqueda(texto = uiState.criterios.busqueda, onTextoChange = onBusquedaChange) }
            item {
                SelectorPeriodoYOrden(
                    periodo = uiState.criterios.periodo,
                    orden = uiState.criterios.orden,
                    onPeriodoChange = onPeriodoChange,
                    onAlternarOrden = onAlternarOrden,
                )
            }
            item {
                ChipsEstado(
                    seleccionado = uiState.criterios.filtro,
                    conteos = uiState.conteos,
                    onFiltroChange = onFiltroChange,
                )
            }
            item { AvisoToque() }
            uiState.tallaPromedioMm?.let { promedio ->
                item { ResumenTalla(promedioMm = promedio, cantidad = uiState.muestras.size) }
            }

            if (uiState.muestras.isEmpty()) {
                item { EstadoVacio(onRestablecer = onRestablecerFiltros) }
            } else {
                items(uiState.muestras, key = { it.id }) { muestra ->
                    TarjetaMuestra(
                        muestra = muestra,
                        puedeRecontar = usuario.rol == Rol.OPERARIO,
                        onVerDetalle = { onVerMuestra(muestra.id) },
                        onRecontar = { onRecontar(muestra.id) },
                    )
                }
            }
            item { TarjetaCondiciones() }
        }
    }
}

@Composable
private fun BarraBusqueda(texto: String, onTextoChange: (String) -> Unit) {
    val focusManager = LocalFocusManager.current
    TextField(
        value = texto,
        onValueChange = onTextoChange,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        placeholder = { Text("Buscar por línea, tren o fecha…") },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (texto.isNotEmpty()) {
                IconButton(onClick = {
                    onTextoChange("")
                    focusManager.clearFocus()
                }) {
                    Icon(Icons.Filled.Cancel, contentDescription = "Limpiar búsqueda")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedLeadingIconColor = MaterialTheme.colorScheme.secondary,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.secondary,
            focusedPlaceholderColor = MaterialTheme.colorScheme.outline,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.outline,
        ),
    )
}

@Composable
private fun SelectorPeriodoYOrden(
    periodo: Periodo,
    orden: Orden,
    onPeriodoChange: (Periodo) -> Unit,
    onAlternarOrden: () -> Unit,
) {
    var menuAbierto by remember { mutableStateOf(false) }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.weight(1f)) {
            BotonClaro(
                icono = Icons.Filled.CalendarToday,
                texto = if (periodo == Periodo.HOY) "Hoy, ${Fechas.corta(System.currentTimeMillis())}" else periodo.etiqueta,
                iconoFinal = Icons.Filled.ExpandMore,
                onClick = { menuAbierto = true },
                modifier = Modifier.fillMaxWidth(),
            )
            DropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
                Periodo.entries.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion.etiqueta, fontWeight = if (opcion == periodo) FontWeight.Bold else null) },
                        onClick = {
                            onPeriodoChange(opcion)
                            menuAbierto = false
                        },
                    )
                }
            }
        }
        BotonClaro(icono = Icons.AutoMirrored.Filled.Sort, texto = orden.etiqueta, onClick = onAlternarOrden)
    }
}

@Composable
private fun BotonClaro(
    icono: ImageVector,
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconoFinal: ImageVector? = null,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier.heightIn(min = 48.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                texto,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = if (iconoFinal != null) Modifier.weight(1f) else Modifier,
            )
            if (iconoFinal != null) {
                Icon(iconoFinal, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable
private fun ChipsEstado(
    seleccionado: FiltroEstado,
    conteos: Map<FiltroEstado, Int>,
    onFiltroChange: (FiltroEstado) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(FiltroEstado.entries) { filtro ->
            val activo = filtro == seleccionado
            val cantidad = conteos[filtro] ?: 0
            Surface(
                onClick = { onFiltroChange(filtro) },
                shape = RoundedCornerShape(12.dp),
                color = if (activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                contentColor = if (activo) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                shadowElevation = if (activo) 0.dp else 1.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    acentoDe(filtro)?.let { color ->
                        Box(Modifier.size(9.dp).clip(CircleShape).background(color))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(filtro.etiqueta, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(6.dp))
                    if (activo) {
                        Text(
                            "$cantidad",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 1.dp),
                        )
                    } else {
                        Text("($cantidad)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}

private fun acentoDe(filtro: FiltroEstado): Color? = when (filtro) {
    FiltroEstado.TODOS -> null
    FiltroEstado.VALIDADOS -> AcentoValidado
    FiltroEstado.PENDIENTES -> AcentoPendiente
    FiltroEstado.OBSERVADOS -> AcentoObservado
}

private fun acentoDe(estado: EstadoMuestra): Color = when (estado) {
    EstadoMuestra.VALIDADO -> AcentoValidado
    EstadoMuestra.PENDIENTE, EstadoMuestra.CORREGIDO -> AcentoPendiente
    EstadoMuestra.OBSERVADO -> AcentoObservado
}

@Composable
private fun AvisoToque() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(EstadoValidadoFondo)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.TouchApp, contentDescription = null, tint = EstadoValidadoTexto, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(
            "Toca cualquier muestra del listado para abrir su detalle completo y trazabilidad",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = EstadoValidadoTexto,
        )
    }
}

@Composable
private fun ResumenTalla(promedioMm: Double, cantidad: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.WaterDrop, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            "Promedio talla: ${"%.1f".format(Locale("es", "CL"), promedioMm)} mm",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
        Text(
            "$cantidad ${if (cantidad == 1) "muestra" else "muestras"}",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = AcentoValidado,
        )
    }
}

@Composable
private fun TarjetaMuestra(
    muestra: Muestra,
    puedeRecontar: Boolean,
    onVerDetalle: () -> Unit,
    onRecontar: () -> Unit,
) {
    val observada = muestra.estado == EstadoMuestra.OBSERVADO
    Surface(
        onClick = onVerDetalle,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        border = if (observada) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Franja lateral con el color del estado
            Box(
                Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(acentoDe(muestra.estado)),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("#${muestra.codigo}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "• ${Fechas.relativa(muestra.registradaEn)}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(bottom = 3.dp),
                                maxLines = 1,
                            )
                        }
                        Text(
                            "Línea %02d — Tren %s".format(muestra.linea, muestra.tren),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    ChipEstado(muestra.estado)
                }

                MetricasMuestra(muestra)

                when {
                    muestra.estado == EstadoMuestra.VALIDADO && muestra.revisadaPor != null ->
                        Aviso(Icons.Filled.Verified, "Aprobado por Sup. ${muestra.revisadaPor}", EstadoValidadoFondo, EstadoValidadoTexto)
                    observada && muestra.comentarioSupervisor != null ->
                        Aviso(Icons.Filled.ErrorOutline, muestra.comentarioSupervisor, EstadoObservadoFondo, EstadoObservadoTexto)
                    muestra.estado == EstadoMuestra.CORREGIDO ->
                        Aviso(Icons.Filled.Replay, "Corregida: a la espera de una nueva revisión", EstadoValidadoFondo, EstadoValidadoTexto)
                }

                if (observada) {
                    AccionesObservada(
                        codigo = muestra.codigo,
                        puedeRecontar = puedeRecontar,
                        onRecontar = onRecontar,
                        onVerDetalle = onVerDetalle,
                    )
                } else {
                    PieTarjeta(muestra)
                }
            }
        }
    }
}

@Composable
private fun MetricasMuestra(muestra: Muestra) {
    val densidadBaja = muestra.conteoIndM < Muestra.DENSIDAD_MINIMA_IND_M
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (densidadBaja) EstadoObservadoFondo.copy(alpha = 0.5f) else MaterialTheme.colorScheme.background)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Metrica(
            "Densidad", "${muestra.conteoIndM} ind/m", Modifier.weight(1.1f),
            color = if (densidadBaja) EstadoObservadoTexto else null,
        )
        Metrica("Talla Media", "${muestra.tallaMm} mm", Modifier.weight(1f))
        Metrica("Profundidad", "${muestra.profundidadM} m", Modifier.weight(1f))
    }
}

@Composable
private fun Metrica(titulo: String, valor: String, modifier: Modifier, color: Color? = null) {
    Column(modifier = modifier) {
        Text(
            titulo,
            style = MaterialTheme.typography.labelMedium,
            color = color ?: MaterialTheme.colorScheme.secondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            valor,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color ?: MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
    }
}

@Composable
private fun Aviso(icono: ImageVector, texto: String, fondo: Color, colorTexto: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(fondo)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icono, contentDescription = null, tint = colorTexto, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(texto, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = colorTexto)
    }
}

@Composable
private fun AccionesObservada(codigo: String, puedeRecontar: Boolean, onRecontar: () -> Unit, onVerDetalle: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (puedeRecontar) {
            OutlinedButton(
                onClick = onRecontar,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Filled.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Recontar", fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }
        Button(
            onClick = onVerDetalle,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
            modifier = Modifier.weight(1f),
        ) {
            Text("Ver detalle", fontWeight = FontWeight.Bold, maxLines = 1)
            Spacer(Modifier.width(6.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Ver detalle de #$codigo", modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun PieTarjeta(muestra: Muestra) {
    val (icono, texto) = when {
        muestra.estado == EstadoMuestra.PENDIENTE && muestra.operador.isNotBlank() -> Icons.Filled.Person to "Op. ${muestra.operador}"
        muestra.sincronizada -> Icons.Filled.CloudDone to "Sincronizado"
        else -> Icons.Filled.CloudUpload to "Pendiente de envío"
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            texto,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text("Detalles", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Icon(Icons.Filled.ChevronRight, contentDescription = null)
    }
}

@Composable
private fun EstadoVacio(onRestablecer: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.FilterAltOff, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(32.dp))
        }
        Text("Sin muestras coincidentes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            "No se encontraron registros que coincidan con la búsqueda o los filtros seleccionados.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
        )
        OutlinedButton(onClick = onRestablecer, shape = RoundedCornerShape(10.dp)) {
            Text("Restablecer filtros", fontWeight = FontWeight.Bold)
        }
    }
}

/** Contexto ambiental del centro (datos ficticios del caso académico). */
@Composable
private fun TarjetaCondiciones() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Cloud, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                "CONDICIONES DE CULTIVO EN SENO",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
            )
            Text(
                "Salinidad: 31.8 PSU • Temp. agua: 11.4 °C • Marea: Bajamar",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 1800)
@Composable
private fun HistorialPreview() {
    val todas = FakeMuestraRepository().muestras.value
    val resultado = FiltrosHistorial.aplicar(todas, CriteriosHistorial(periodo = Periodo.TODO))
    MusselAppTheme {
        HistorialScreen(
            usuario = Usuario("11.111.111-1", "Carla Vera", Rol.OPERARIO),
            centro = CentroCultivo.disponibles.first(),
            uiState = HistorialUiState(
                criterios = CriteriosHistorial(periodo = Periodo.TODO),
                muestras = resultado.muestras,
                conteos = resultado.conteos,
                tallaPromedioMm = resultado.tallaPromedioMm,
            ),
            onBusquedaChange = {}, onFiltroChange = {}, onPeriodoChange = {}, onAlternarOrden = {},
            onRestablecerFiltros = {}, onSincronizar = {}, onMensajeMostrado = {}, onNavegar = {},
            onVerMuestra = {}, onRecontar = {}, onCerrarSesion = {},
        )
    }
}
