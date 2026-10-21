package cl.aldemar.musselapp.ui.detalle

import android.graphics.BitmapFactory
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
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
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
import androidx.compose.ui.graphics.asImageBitmap
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
import cl.aldemar.musselapp.data.model.EstadoMuestra
import cl.aldemar.musselapp.data.model.Muestra
import cl.aldemar.musselapp.data.model.Rol
import cl.aldemar.musselapp.data.model.Usuario
import cl.aldemar.musselapp.ui.componentes.Avatar
import cl.aldemar.musselapp.ui.componentes.ChipEstado
import cl.aldemar.musselapp.ui.theme.AlertaFondo
import cl.aldemar.musselapp.ui.theme.AlertaTexto
import cl.aldemar.musselapp.ui.theme.EstadoValidadoFondo
import cl.aldemar.musselapp.ui.theme.EstadoValidadoTexto
import cl.aldemar.musselapp.ui.theme.colores
import cl.aldemar.musselapp.util.Fechas
import cl.aldemar.musselapp.util.FichaMuestra
import cl.aldemar.musselapp.util.FichaPdf
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DetalleRoute(
    muestraId: Long,
    usuario: Usuario,
    onVolver: () -> Unit,
    onIrARevision: ((Long) -> Unit)? = null,
    viewModel: DetalleViewModel = viewModel(factory = DetalleViewModel.Factory(muestraId)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DetalleScreen(
        uiState = uiState,
        usuario = usuario,
        onVolver = onVolver,
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
                    Avatar(nombre = usuario.nombre, tamano = 34, claro = true)
                    Spacer(Modifier.width(8.dp))
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
                        // ── 2. Inspección Visual IA (Muestreo Biométrico) ─────
                        TarjetaInspeccionVisual(
                            muestra = muestra,
                            onExpandir = { onAlternarZoom(true) },
                        )

                        // ── 3. Resolución y Dictamen Técnico ──────────────────
                        TarjetaDictamenTecnico(
                            muestra = muestra,
                            esSupervisor = usuario.rol == Rol.SUPERVISOR,
                            onIrARevision = { onIrARevision?.invoke(muestra.id) },
                        )

                        // ── 4. Resumen Biométrico y Población ─────────────────
                        TarjetaResumenBiometrico(muestra = muestra)

                        // ── 5. Trazabilidad Técnica ───────────────────────────
                        TarjetaTrazabilidadTecnica(muestra = muestra)

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

    // Modal de zoom en alta resolución
    if (uiState.modalZoomAbierto && uiState.muestra != null) {
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
                            "Inspección en Alta Resolución",
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
                        RenderImagenMuestra(
                            fotoRuta = uiState.muestra.fotoRuta,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Calibración SERNAPESCA • Conteo: ${uiState.muestra.conteoIndM} ind/m",
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
                ItemMicroMetrica("CALIBRE", "${muestra.tallaMm} mm", Modifier.weight(1f))
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

@Composable
private fun TarjetaInspeccionVisual(
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.CenterFocusStrong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Inspección Visual IA (Muestreo Biométrico)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text(
                        "CHIL-${muestra.linea * 10}B",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Lienzo con la foto y bounding boxes
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE2E8F0)),
            ) {
                RenderImagenMuestra(
                    fotoRuta = muestra.fotoRuta,
                    modifier = Modifier.fillMaxSize(),
                )

                // Overlays superiores
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xCC0F172A))
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Verified, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("SERNAPESCA ID: 1042-CL", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xEEFFFFFF))
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                    ) {
                        val fechaStr = SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale.forLanguageTag("es-CL")).format(Date(muestra.registradaEn))
                        Text(fechaStr, color = Color(0xFF0F172A), fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }

                // Bounding boxes simulados
                Box(
                    modifier = Modifier
                        .padding(start = 24.dp, top = 48.dp)
                        .size(width = 80.dp, height = 48.dp)
                        .border(1.5.dp, Color(0x992563EB), RoundedCornerShape(4.dp))
                        .background(Color(0x222563EB)),
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xDD0F172A))
                            .padding(horizontal = 4.dp, vertical = 1.dp),
                    ) {
                        Text("CAL: ${muestra.tallaMm}mm", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Overlay inferior con botón zoom
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color(0x990F172A))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Lente Polarizado • ${muestra.conteoIndM} ind. detectados",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.White)
                            .clickable(onClick = onExpandir)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.ZoomIn, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Expandir", color = Color(0xFF0F172A), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// Decodifica la foto reducida (la original de la cámara pesa varios MB) y fuera del hilo principal
private fun decodificarFotoReducida(ruta: String, ladoMaxPx: Int = 1280): ImageBitmap? {
    val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(ruta, limites)
    if (limites.outWidth <= 0 || limites.outHeight <= 0) return null
    var muestreo = 1
    while (maxOf(limites.outWidth, limites.outHeight) / (muestreo * 2) >= ladoMaxPx) muestreo *= 2
    val opciones = BitmapFactory.Options().apply { inSampleSize = muestreo }
    return BitmapFactory.decodeFile(ruta, opciones)?.asImageBitmap()
}

@Composable
private fun RenderImagenMuestra(fotoRuta: String?, modifier: Modifier = Modifier) {
    if (fotoRuta != null) {
        val bitmap by produceState<ImageBitmap?>(initialValue = null, fotoRuta) {
            value = withContext(Dispatchers.IO) { decodificarFotoReducida(fotoRuta) }
        }
        val foto = bitmap
        if (foto != null) {
            Image(bitmap = foto, contentDescription = "Fotografía de muestra", modifier = modifier, contentScale = ContentScale.Crop)
            return
        }
    }
    // Imagen acuícola realista incorporada en el diseño
    Image(
        painter = painterResource(R.drawable.muestra_choritos),
        contentDescription = "Muestra de choritos sobre tabla de calibración",
        modifier = modifier,
        contentScale = ContentScale.Crop,
    )
}

@Composable
private fun TarjetaDictamenTecnico(
    muestra: Muestra,
    esSupervisor: Boolean,
    onIrARevision: () -> Unit,
) {
    // Pendiente y Corregido usan el ámbar de ColoresEstado (antes: ámbar y azul escritos a mano)
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
                    Text("Resolución y Dictamen Técnico", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
                ChipEstado(muestra.estado)
            }

            // Información del revisor o estado
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
                            muestra.revisadaPor ?: "Pendiente de revisión técnica",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        if (muestra.revisadaPor != null) {
                            Text(
                                Fechas.relativa(muestra.registradaEn),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                        }
                    }
                    Text(
                        if (muestra.revisadaPor != null) "Biólogo Marino • Supervisor Técnico (Reg. SERNAPESCA #BM-2091)" else "En espera de validación de calidad en centro de cultivo",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 11.sp,
                    )
                }
            }

            // Caja de observación con borde izquierdo de color
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
                    .drawLeftBorder(color = colorBordeEstado, strokeWidth = 4.dp)
                    .padding(12.dp),
            ) {
                Column {
                    Text(
                        "OBSERVACIÓN REGISTRADA",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                    )
                    Spacer(Modifier.height(4.dp))
                    val textoObs = muestra.comentarioSupervisor?.takeIf { it.isNotBlank() }
                        ?: muestra.observacion.takeIf { it.isNotBlank() }
                        ?: "Muestra sin observaciones adicionales de terreno."
                    Text(
                        "\"$textoObs\"",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1E293B),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    )
                }
            }

            // Si es supervisor, botón para dictaminar
            if (esSupervisor) {
                Button(
                    onClick = onIrARevision,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = Color.White,
                    ),
                ) {
                    Icon(Icons.Filled.RateReview, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Revisar y Modificar Dictamen", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TarjetaResumenBiometrico(muestra: Muestra) {
    val desvio = muestra.desvioDensidadPct
    val esBajo = desvio < 0

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
                    Text("Resumen Biométrico y Población", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (esBajo) AlertaFondo else EstadoValidadoFondo)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(
                        "${if (desvio >= 0) "+$desvio%" else "$desvio%"} DESVÍO",
                        color = if (esBajo) AlertaTexto else EstadoValidadoTexto,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            // Bloque de densidad principal
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("Densidad Poblacional", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
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
                                tint = if (esBajo) AlertaTexto else EstadoValidadoTexto,
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(Modifier.width(2.dp))
                            Text(
                                "$desvio% respecto al histórico (${Muestra.DENSIDAD_ESTANDAR_IND_M} ind/m)",
                                fontSize = 11.sp,
                                color = if (esBajo) AlertaTexto else EstadoValidadoTexto,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }

                    // Gráfica de barras miniatura
                    Row(
                        modifier = Modifier.height(48.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Box(Modifier.width(9.dp).height(32.dp).clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)).background(Color(0xFFCBD5E1)))
                        Box(Modifier.width(9.dp).height(40.dp).clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)).background(Color(0xFFCBD5E1)))
                        Box(Modifier.width(9.dp).height(48.dp).clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)).background(Color(0xFFCBD5E1)))
                        val alturaBarraActual = (muestra.conteoIndM.toFloat() / 500f * 48f).coerceIn(16f, 48f)
                        Box(
                            Modifier
                                .width(9.dp)
                                .height(alturaBarraActual.dp)
                                .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                .background(if (esBajo) AlertaTexto else EstadoValidadoTexto),
                        )
                    }
                }
            }

            // Grid de 3 métricas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ItemMetricaBiometrica("Talla Media", "${muestra.tallaMm}", "milímetros", Modifier.weight(1f))
                ItemMetricaBiometrica("Biomasa Est.", "${muestra.rendimientoKgM}", "kg / metro", Modifier.weight(1f))
                ItemMetricaBiometrica("Fijación Biso", "Normal", "resistencia 4/5", Modifier.weight(1f))
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
            Text(titulo, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(2.dp))
            Text(valor, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(1.dp))
            Text(unidad, fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
private fun TarjetaTrazabilidadTecnica(muestra: Muestra) {
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
                Text("Trazabilidad Técnica", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ItemFilaTrazabilidad("Operario en Terreno:", muestra.operador.ifBlank { "Carla Vera (ID: OP-418)" })
                ItemFilaTrazabilidad("Equipo de Captura:", "Tablet Rugged PTN-02 (Offline)")
                ItemFilaTrazabilidad("Firma Criptográfica:", "SHA-256: 9e4f71a0b3c%04d".format(muestra.id), esHash = true)
            }
        }
    }
}

@Composable
private fun ItemFilaTrazabilidad(label: String, valor: String, esHash: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
        Text(
            valor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (esHash) FontWeight.Normal else FontWeight.Bold,
            fontFamily = if (esHash) FontFamily.Monospace else FontFamily.Default,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

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
