package cl.aldemar.musselapp.ui.detalle

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.aldemar.musselapp.R
import cl.aldemar.musselapp.data.model.CentroCultivo
import cl.aldemar.musselapp.data.model.EstadoMuestra
import cl.aldemar.musselapp.data.model.Muestra
import cl.aldemar.musselapp.data.model.Rol
import cl.aldemar.musselapp.data.model.Usuario
import cl.aldemar.musselapp.ui.componentes.MenuPerfil
import cl.aldemar.musselapp.ui.componentes.ChipEstado
import cl.aldemar.musselapp.ui.componentes.FotoMuestra
import cl.aldemar.musselapp.ui.theme.AlertaFondo
import cl.aldemar.musselapp.ui.theme.AlertaTexto
import cl.aldemar.musselapp.ui.theme.EstadoValidadoFondo
import cl.aldemar.musselapp.ui.theme.EstadoValidadoTexto
import cl.aldemar.musselapp.ui.theme.colores
import cl.aldemar.musselapp.util.Fechas
import cl.aldemar.musselapp.util.FichaMuestra
import cl.aldemar.musselapp.util.FichaPdf
import cl.aldemar.musselapp.util.RutValidator
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DetalleRoute(
    muestraId: Long,
    usuario: Usuario,
    centro: CentroCultivo,
    onVolver: () -> Unit,
    onCerrarSesion: () -> Unit,
    onIrARevision: ((Long) -> Unit)? = null,
    viewModel: DetalleViewModel = viewModel(factory = DetalleViewModel.Factory(muestraId)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DetalleScreen(
        uiState = uiState,
        usuario = usuario,
        centro = centro,
        onVolver = onVolver,
        onCerrarSesion = onCerrarSesion,
        onIrARevision = onIrARevision,
        onAlternarZoom = viewModel::alternarModalZoom,
        onMostrarMensaje = viewModel::mostrarMensaje,
        onLimpiarMensaje = viewModel::limpiarMensaje,
    )
}

@Composable
fun DetalleScreen(
    uiState: DetalleUiState,
    usuario: Usuario,
    onVolver: () -> Unit,
    onIrARevision: ((Long) -> Unit)?,
    onAlternarZoom: (Boolean) -> Unit,
    onMostrarMensaje: (String) -> Unit,
    onLimpiarMensaje: () -> Unit,
    centro: CentroCultivo = CentroCultivo.disponibles.first(),
    onCerrarSesion: () -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // La ficha PDF se genera fuera del hilo principal (lee la foto y escribe un archivo)
    fun conFicha(accion: (File) -> Unit) {
        val muestra = uiState.muestra ?: return
        scope.launch {
            try {
                val pdf = withContext(Dispatchers.IO) { FichaPdf.generar(context, muestra) }
                accion(pdf)
            } catch (e: ActivityNotFoundException) {
                onMostrarMensaje("No hay una app instalada para esta acción")
            } catch (e: Exception) {
                onMostrarMensaje("No se pudo generar la ficha técnica")
            }
        }
    }

    // "Descargar": el usuario elige dónde guardar el PDF (por ejemplo, Descargas)
    val guardarFicha = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf"),
    ) { destino ->
        if (destino != null) {
            conFicha { pdf ->
                scope.launch {
                    try {
                        withContext(Dispatchers.IO) { FichaPdf.guardarEn(context, pdf, destino) }
                        onMostrarMensaje("Ficha técnica guardada")
                    } catch (e: Exception) {
                        onMostrarMensaje("No se pudo guardar la ficha técnica")
                    }
                }
            }
        }
    }

    LaunchedEffect(uiState.mensajeToast) {
        val mensaje = uiState.mensajeToast
        if (mensaje != null) {
            snackbarHostState.showSnackbar(mensaje)
            onLimpiarMensaje()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver al Historial",
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(R.drawable.logo_musselapp),
                            contentDescription = "Logo MusselApp",
                            modifier = Modifier.size(26.dp),
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Detalle Muestra",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Text(
                            text = "MusselApp • Operaciones",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                        )
                    }
                    // Al tocar el avatar se ve el perfil y se puede cerrar sesión, como en las demás pantallas
                    MenuPerfil(usuario = usuario, centro = centro, onCerrarSesion = onCerrarSesion)
                }
            }
        },
    ) { paddingValores ->
        if (uiState.cargando) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValores),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            val muestra = uiState.muestra
            if (muestra == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValores),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("No se encontró la información de la muestra.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValores)
                        .verticalScroll(rememberScrollState()),
                ) {
                    // ── 1. Franja Contextual Superior ─────────────────────────
                    FranjaContextualDetalle(
                        muestra = muestra,
                        onCompartir = { conFicha { pdf -> FichaPdf.compartir(context, pdf, muestra) } },
                        onImprimir = { conFicha { pdf -> FichaPdf.imprimir(context, pdf, muestra) } },
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        // ── 2. Fotografía de la muestra ───────────────────────
                        TarjetaFotoMuestra(
                            muestra = muestra,
                            onExpandir = { onAlternarZoom(true) },
                        )

                        // ── 3. Resolución y Dictamen Técnico ──────────────────
                        TarjetaDictamenTecnico(
                            muestra = muestra,
                            esSupervisor = usuario.rol == Rol.SUPERVISOR,
                            onIrARevision = { onIrARevision?.invoke(muestra.id) },
                        )

                        // ── 4. Resumen biométrico ─────────────────────────────
                        TarjetaResumenBiometrico(muestra = muestra)

                        // ── 5. Trazabilidad ───────────────────────────────────
                        TarjetaTrazabilidad(muestra = muestra)

                        // ── 6. Acciones Finales ───────────────────────────────
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Button(
                                onClick = { guardarFicha.launch(FichaMuestra.nombreArchivo(muestra)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                            ) {
                                Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Descargar Ficha Técnica PDF", fontWeight = FontWeight.SemiBold)
                            }

                            OutlinedButton(
                                onClick = onVolver,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Volver al Historial", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Foto ampliada (solo si la muestra tiene foto)
    val muestraZoom = uiState.muestra
    if (uiState.modalZoomAbierto && muestraZoom != null && muestraZoom.fotoRuta != null) {
        Dialog(onDismissRequest = { onAlternarZoom(false) }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                color = Color.Black,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "Fotografía ${muestraZoom.codigo}",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        IconButton(onClick = { onAlternarZoom(false) }) {
                            Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = Color.White)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp)),
                    ) {
                        FotoMuestra(
                            fotoRuta = muestraZoom.fotoRuta,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit,
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Conteo: ${muestraZoom.conteoIndM} ind/m • Talla media: ${muestraZoom.tallaMm} mm",
                        color = Color.LightGray,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun FranjaContextualDetalle(
    muestra: Muestra,
    onCompartir: () -> Unit,
    onImprimir: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Science,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Muestra #${muestra.codigo} • Tren ${muestra.tren} • ${muestra.codigoLinea}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                Row {
                    IconButton(onClick = onCompartir, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Share, contentDescription = "Compartir", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onImprimir, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Print, contentDescription = "Imprimir", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Grid de 4 micro-métricas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ItemMicroMetrica("PROF.", "${muestra.profundidadM} m", Modifier.weight(1f))
                ItemMicroMetrica("DENSIDAD", "${muestra.conteoIndM} /m", Modifier.weight(1f))
                ItemMicroMetrica("TALLA", "${muestra.tallaMm} mm", Modifier.weight(1f))
                ItemMicroMetrica("REND.", "${muestra.rendimientoKgM} kg/m", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ItemMicroMetrica(etiqueta: String, valor: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 6.dp),
    ) {
        Column {
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = valor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
    }
}

/** Foto real de la muestra y la fecha de registro; sin foto se muestra "Sin fotografía". */
@Composable
private fun TarjetaFotoMuestra(
    muestra: Muestra,
    onExpandir: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.CenterFocusStrong,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text("Fotografía de la muestra", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(8.dp)),
            ) {
                FotoMuestra(fotoRuta = muestra.fotoRuta, modifier = Modifier.fillMaxSize())

                // Fecha y hora en que el operario registró la muestra
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xEEFFFFFF))
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                ) {
                    Text(
                        fechaHora(muestra.registradaEn),
                        color = Color(0xFF0F172A),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                    )
                }

                if (muestra.fotoRuta != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.White)
                            .clickable(onClickLabel = "Ampliar la fotografía", onClick = onExpandir)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.ZoomIn, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Expandir", color = Color(0xFF0F172A), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaDictamenTecnico(
    muestra: Muestra,
    esSupervisor: Boolean,
    onIrARevision: () -> Unit,
) {
    // Pendiente y Corregido usan el ámbar de ColoresEstado
    val colorBordeEstado = when (muestra.estado) {
        EstadoMuestra.VALIDADO -> EstadoValidadoTexto
        EstadoMuestra.CORREGIDO, EstadoMuestra.PENDIENTE -> muestra.estado.colores.acento
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.RateReview, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Dictamen del supervisor", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
                ChipEstado(muestra.estado)
            }

            // Quién revisó y cuándo (datos de la tabla revisiones)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            muestra.revisadaPor ?: "Pendiente de revisión",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        muestra.revisadaEn?.let { revisada ->
                            Text(
                                Fechas.relativa(revisada),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                        }
                    }
                    Text(
                        if (muestra.revisadaPor != null) "Supervisor de Calidad" else "En espera de la revisión del supervisor de calidad",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 12.sp,
                    )
                }
            }

            muestra.comentarioSupervisor?.takeIf { it.isNotBlank() }?.let { comentario ->
                CajaTexto(etiqueta = "COMENTARIO DEL SUPERVISOR", texto = comentario, colorBorde = colorBordeEstado)
            }
            CajaTexto(
                etiqueta = "OBSERVACIÓN DEL OPERARIO",
                texto = muestra.observacion.takeIf { it.isNotBlank() } ?: "Sin observaciones de terreno.",
                colorBorde = MaterialTheme.colorScheme.outline,
            )

            // Solo una muestra pendiente se puede dictaminar: Validado y Corregido son finales
            if (esSupervisor && muestra.estado == EstadoMuestra.PENDIENTE) {
                Button(
                    onClick = onIrARevision,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = Color.White,
                    ),
                ) {
                    Icon(Icons.Filled.RateReview, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Revisar esta muestra", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/** Texto destacado con una franja de color a la izquierda. */
@Composable
private fun CajaTexto(etiqueta: String, texto: String, colorBorde: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
            )
            .drawLeftBorder(color = colorBorde, strokeWidth = 4.dp)
            .padding(12.dp),
    ) {
        Column {
            Text(
                etiqueta,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                texto,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1E293B),
            )
        }
    }
}

@Composable
private fun TarjetaResumenBiometrico(muestra: Muestra) {
    val desvio = muestra.desvioDensidadPct
    val esBajo = desvio < 0
    val desvioTexto = if (desvio >= 0) "+$desvio%" else "$desvio%"
    val colorDesvio = if (esBajo) AlertaTexto else EstadoValidadoTexto

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Analytics, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Resumen biométrico", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (esBajo) AlertaFondo else EstadoValidadoFondo)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text("$desvioTexto DESVÍO", color = colorDesvio, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Densidad medida por el operario frente a la densidad de referencia
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Densidad (conteo manual)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            "${muestra.conteoIndM}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("ind / m", fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(bottom = 4.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (esBajo) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,
                            contentDescription = null,
                            tint = colorDesvio,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            "$desvioTexto respecto a la densidad de referencia (${Muestra.DENSIDAD_ESTANDAR_IND_M} ind/m)",
                            fontSize = 12.sp,
                            color = colorDesvio,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ItemMetricaBiometrica("Talla media", "${muestra.tallaMm}", "milímetros", Modifier.weight(1f))
                ItemMetricaBiometrica("Rendimiento", "${muestra.rendimientoKgM}", "kg / m (estimado)", Modifier.weight(1f))
                ItemMetricaBiometrica("Profundidad", "${muestra.profundidadM}", "metros", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ItemMetricaBiometrica(titulo: String, valor: String, unidad: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(titulo, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(2.dp))
            Text(valor, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(1.dp))
            Text(unidad, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary, textAlign = TextAlign.Center)
        }
    }
}

/** Quién registró y revisó la muestra, y si ya se envió: todo sale de la base. */
@Composable
private fun TarjetaTrazabilidad(muestra: Muestra) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Trazabilidad", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ItemFilaTrazabilidad("Código", muestra.codigo)
                ItemFilaTrazabilidad("Operario", muestra.operador.ifBlank { "—" })
                ItemFilaTrazabilidad(
                    "RUT operario",
                    muestra.operadorRut.takeIf { it.isNotBlank() }?.let(RutValidator::formatear) ?: "—",
                )
                ItemFilaTrazabilidad("Registrada", fechaHora(muestra.registradaEn))
                ItemFilaTrazabilidad("Revisada", muestra.revisadaEn?.let(::fechaHora) ?: "Pendiente")
                ItemFilaTrazabilidad("Envío", if (muestra.sincronizada) "Sincronizada" else "Pendiente de envío")
            }
        }
    }
}

@Composable
private fun ItemFilaTrazabilidad(label: String, valor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.width(12.dp))
        Text(
            valor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
        )
    }
}

/** "24/10/2026 • 09:30" en hora local. */
private fun fechaHora(instante: Long): String =
    SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale.forLanguageTag("es-CL")).format(Date(instante))

private fun Modifier.drawLeftBorder(color: Color, strokeWidth: androidx.compose.ui.unit.Dp): Modifier = this.then(
    Modifier.drawWithContent {
        drawContent()
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(0f, 0f),
            end = androidx.compose.ui.geometry.Offset(0f, size.height),
            strokeWidth = strokeWidth.toPx(),
        )
    }
)
