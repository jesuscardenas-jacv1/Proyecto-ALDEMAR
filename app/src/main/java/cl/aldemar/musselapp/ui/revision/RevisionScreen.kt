package cl.aldemar.musselapp.ui.revision

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.consumeWindowInsets
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
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import cl.aldemar.musselapp.data.model.Usuario
import cl.aldemar.musselapp.ui.componentes.MenuPerfil
import cl.aldemar.musselapp.ui.componentes.ChipEstado
import cl.aldemar.musselapp.ui.componentes.FotoMuestra
import cl.aldemar.musselapp.ui.componentes.NavegacionInferior
import cl.aldemar.musselapp.ui.principal.Destino
import cl.aldemar.musselapp.ui.theme.EstadoValidadoFondo
import cl.aldemar.musselapp.ui.theme.EstadoValidadoTexto
import cl.aldemar.musselapp.ui.theme.colores

@Composable
fun RevisionRoute(
    usuario: Usuario,
    centro: CentroCultivo,
    onVolver: () -> Unit,
    onNavegar: (Destino) -> Unit,
    onCerrarSesion: () -> Unit,
    /** Muestra abierta desde el Detalle; null abre la cola desde la primera pendiente. */
    muestraIdInicial: Long? = null,
    viewModel: RevisionViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(muestraIdInicial) { muestraIdInicial?.let(viewModel::enfocarMuestra) }

    RevisionScreen(
        uiState = uiState,
        usuario = usuario,
        centro = centro,
        onVolver = onVolver,
        onNavegar = onNavegar,
        onCerrarSesion = onCerrarSesion,
        onSeleccionarMuestra = viewModel::seleccionarMuestra,
        onElegirEstado = viewModel::elegirEstado,
        onActualizarComentario = viewModel::actualizarComentario,
        onAgregarMotivo = viewModel::agregarMotivoRapido,
        onAlternarZoom = viewModel::alternarModalZoom,
        onGuardar = { viewModel.guardarDictamen(supervisor = usuario) },
        onLimpiarMensaje = viewModel::limpiarMensaje,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevisionScreen(
    uiState: RevisionUiState,
    usuario: Usuario,
    centro: CentroCultivo,
    onVolver: () -> Unit,
    onNavegar: (Destino) -> Unit,
    onSeleccionarMuestra: (Long) -> Unit,
    onElegirEstado: (EstadoMuestra) -> Unit,
    onActualizarComentario: (String) -> Unit,
    onAgregarMotivo: (String) -> Unit,
    onAlternarZoom: (Boolean) -> Unit,
    onGuardar: () -> Unit,
    onLimpiarMensaje: () -> Unit,
    onCerrarSesion: () -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }

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
                            contentDescription = "Volver",
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
                            text = "MusselApp",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4ADE80)),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "SUPERVISIÓN • ONLINE EN TERRENO",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            )
                        }
                    }
                    // Al tocar el avatar se ve el perfil y se puede cerrar sesión, como en las demás pantallas
                    MenuPerfil(usuario = usuario, centro = centro, onCerrarSesion = onCerrarSesion)
                }
            }
        },
        bottomBar = {
            NavegacionInferior(
                rol = usuario.rol,
                seleccionado = Destino.SUPERVISION,
                onNavegar = onNavegar,
            )
        },
    ) { innerPadding ->
        if (uiState.cargando) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    // Con el teclado abierto se puede desplazar hasta el comentario técnico sin que quede tapado
                    .consumeWindowInsets(innerPadding)
                    .imePadding()
                    .verticalScroll(rememberScrollState()),
            ) {
                // ── 1. Contextual Header de Supervisión ──────────────────────
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shadowElevation = 3.dp,
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
                                    Icons.Filled.VerifiedUser,
                                    contentDescription = null,
                                    tint = Color(0xFF60A5FA),
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Supervisión de Calidad",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }

                            // Badge de pendientes
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                            ) {
                                Text(
                                    "${uiState.muestrasPendientes.size} PENDIENTES",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                            }
                        }

                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                centro.nombre,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
                            )
                        }
                    }
                }

                // Si no hay muestras pendientes
                if (uiState.muestrasPendientes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(
                                Icons.Filled.AssignmentTurnedIn,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = EstadoValidadoTexto,
                            )
                            Text(
                                "¡Todas las muestras han sido revisadas!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                "No hay muestras pendientes de validación en este momento.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                } else {
                    val muestra = uiState.muestraActual

                    // ── Selector de muestras pendientes (Chips Horizontales) ──
                    Column(modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)) {
                        Text(
                            "SELECCIONAR MUESTRA EN COLA:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            uiState.muestrasPendientes.forEach { m ->
                                val seleccionada = m.id == muestra?.id
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (seleccionada) MaterialTheme.colorScheme.primary else Color.White)
                                        .border(
                                            width = if (seleccionada) 2.dp else 1.dp,
                                            color = if (seleccionada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                            shape = RoundedCornerShape(8.dp),
                                        )
                                        .clickable { onSeleccionarMuestra(m.id) }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            m.codigo,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (seleccionada) Color.White else MaterialTheme.colorScheme.onSurface,
                                        )
                                        Text(
                                            "${m.codigoLinea} • Tren ${m.tren}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            color = if (seleccionada) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.secondary,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (muestra != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            // ── 2. Tarjeta Hero de Muestra Evaluada ───────────────
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
                                        verticalAlignment = Alignment.Top,
                                    ) {
                                        Column {
                                            Text(
                                                "EVALUANDO MUESTRA",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.secondary,
                                                fontWeight = FontWeight.Bold,
                                            )
                                            Text(
                                                "#${muestra.codigo}",
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                            )
                                            Text(
                                                "${muestra.codigoLinea} • Tren de Cultivo ${muestra.tren}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.secondary,
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            ChipEstado(muestra.estado)
                                            Spacer(Modifier.height(4.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant,
                                            ) {
                                                Text(
                                                    "Prof: ${muestra.profundidadM}m",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                )
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(10.dp))

                                    // Tira de métricas marinas
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                        ) {
                                            Column {
                                                Text("Densidad", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                                Row(verticalAlignment = Alignment.Bottom) {
                                                    Text("${muestra.conteoIndM}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                                    Text(" ind/m", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                                }
                                            }
                                            Column {
                                                Text("Calibre Prom.", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                                Row(verticalAlignment = Alignment.Bottom) {
                                                    Text("${muestra.tallaMm}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                                    Text(" mm", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                                }
                                            }
                                            Column {
                                                Text("Rendimiento", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                                Row(verticalAlignment = Alignment.Bottom) {
                                                    Text("${muestra.rendimientoKgM}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                                    Text(" kg/m", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // ── 3. Fotografía de la muestra ─────────────────────
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(4f / 3f)
                                        .clip(RoundedCornerShape(12.dp)),
                                ) {
                                    FotoMuestra(
                                        fotoRuta = muestra.fotoRuta,
                                        modifier = Modifier.fillMaxSize(),
                                    )

                                    if (muestra.fotoRuta != null) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(10.dp)
                                                .clip(RoundedCornerShape(50))
                                                .background(Color.White)
                                                .clickable(onClickLabel = "Ampliar la fotografía") { onAlternarZoom(true) }
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Filled.ZoomIn, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(2.dp))
                                                Text("Zoom", color = Color(0xFF0F172A), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }

                            // ── 4. Formulario de Dictamen Técnico ───────────────
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
                                    Text(
                                        "DICTAMEN TÉCNICO DEL SUPERVISOR",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                    )

                                    // Botones de Decisión (Validar / Corregir)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        // Botón VALIDAR
                                        val esValidado = uiState.estadoElegido == EstadoMuestra.VALIDADO
                                        Button(
                                            onClick = { onElegirEstado(EstadoMuestra.VALIDADO) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(56.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (esValidado) EstadoValidadoTexto else EstadoValidadoFondo,
                                                contentColor = if (esValidado) Color.White else EstadoValidadoTexto,
                                            ),
                                            border = androidx.compose.foundation.BorderStroke(
                                                width = if (esValidado) 2.dp else 1.5.dp,
                                                color = EstadoValidadoTexto,
                                            ),
                                        ) {
                                            Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("VALIDAR", fontWeight = FontWeight.Bold)
                                        }

                                        // Botón CORREGIR: mismos colores que el chip Corregido
                                        val esCorregido = uiState.estadoElegido == EstadoMuestra.CORREGIDO
                                        val coloresCorregido = EstadoMuestra.CORREGIDO.colores
                                        Button(
                                            onClick = { onElegirEstado(EstadoMuestra.CORREGIDO) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(56.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (esCorregido) coloresCorregido.texto else coloresCorregido.fondo,
                                                contentColor = if (esCorregido) Color.White else coloresCorregido.texto,
                                            ),
                                            border = androidx.compose.foundation.BorderStroke(
                                                width = if (esCorregido) 2.dp else 1.5.dp,
                                                color = coloresCorregido.texto,
                                            ),
                                        ) {
                                            Icon(Icons.Filled.EditNote, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("CORREGIR", fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // Chips de motivos rápidos
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            "Justificaciones técnicas frecuentes:",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.secondary,
                                        )
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            MotivoChip("Densidad bajo umbral (< 350 ind/m)") { onAgregarMotivo("Densidad bajo el rango mínimo estándar (< 350 ind/m)") }
                                            MotivoChip("Epifauna / Fouling detectado") { onAgregarMotivo("Presencia notable de epifauna y fouling en el tramo") }
                                            MotivoChip("Talla irregular") { onAgregarMotivo("Dispersión de calibre anómala para el ciclo de engorda") }
                                            MotivoChip("Fijación débil") { onAgregarMotivo("Biso débil en cuerda, verificar anclaje") }
                                        }
                                    }

                                    // Campo de Comentario Técnico
                                    OutlinedTextField(
                                        value = uiState.comentarioTecnico,
                                        onValueChange = onActualizarComentario,
                                        label = { Text("Comentario técnico") },
                                        placeholder = { Text("Ingresa el fundamento del dictamen o qué se corrigió...") },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 3,
                                        shape = RoundedCornerShape(8.dp),
                                        isError = uiState.errorComentario != null,
                                        supportingText = {
                                            if (uiState.errorComentario != null) {
                                                Text(uiState.errorComentario, color = MaterialTheme.colorScheme.error)
                                            } else {
                                                Text("Obligatorio al Corregir; opcional al Validar.")
                                            }
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                        ),
                                    )

                                    // Botón Guardar Dictamen
                                    Button(
                                        onClick = onGuardar,
                                        enabled = !uiState.guardando,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(54.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary,
                                        ),
                                    ) {
                                        if (uiState.guardando) {
                                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                        } else {
                                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text("Confirmar y Guardar Dictamen", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Zoom
    val muestraZoom = uiState.muestraActual
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
                        Text("Inspección de Muestra", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { onAlternarZoom(false) }) {
                            Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = Color.White)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(8.dp))) {
                        FotoMuestra(fotoRuta = muestraZoom.fotoRuta, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                    }
                }
            }
        }
    }
}

@Composable
private fun MotivoChip(texto: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}
