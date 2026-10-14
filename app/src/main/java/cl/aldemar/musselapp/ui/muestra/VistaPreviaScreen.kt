package cl.aldemar.musselapp.ui.muestra

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Anchor
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cl.aldemar.musselapp.data.model.CentroCultivo
import cl.aldemar.musselapp.ui.theme.MusselAppTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Navy del diseño (DESIGN.md): barras y botones sobre la foto
private val Navy = Color(0xFF0F172A)

// Estado "Pendiente" (DESIGN.md)
private val PendienteFondo = Color(0xFFFEF3C7)
private val PendienteBorde = Color(0xFFD97706)
private val PendienteTexto = Color(0xFF78350F)

private const val MAX_OBSERVACION = 300

/**
 * Última pantalla del operario: revisa la foto y los datos, los corrige si hace falta
 * y confirma el muestreo.
 */
@Composable
fun VistaPreviaScreen(
    borrador: BorradorMuestra,
    onVolver: () -> Unit = {},
    onRepetirFoto: () -> Unit = {},
    onFotoCambiada: (String?) -> Unit = {},
    onParametrosCambiados: (ParametrosMuestra) -> Unit = {},
    onObservacionCambiada: (String) -> Unit = {},
    onConfirmar: () -> Unit = {},
) {
    val context = LocalContext.current
    val focoActual = LocalFocusManager.current
    val teclado = LocalSoftwareKeyboardController.current

    // Cierra el teclado y quita el foco del cuadro de notas; lo escrito ya queda guardado
    val cerrarTeclado = {
        focoActual.clearFocus()
        teclado?.hide()
    }

    var confirmando by remember { mutableStateOf(false) }
    var eliminando by remember { mutableStateOf(false) }
    var verFoto by remember { mutableStateOf(false) }
    var editando by remember { mutableStateOf(false) }
    var guardarLocal by remember { mutableStateOf(true) } // TODO: guardar en la base local (Room)

    val parametros = borrador.parametros
    val rutaFoto = borrador.fotoRuta
    val listo = parametros != null && rutaFoto != null

    // Elegir otra foto desde la galería del teléfono
    val selectorGaleria = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val archivo = copiarEnApp(context, uri)
            if (archivo != null) {
                borrarArchivo(rutaFoto)
                onFotoCambiada(archivo.absolutePath)
            } else {
                Toast.makeText(context, "No se pudo abrir la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paleta.Fondo)
            .imePadding()
            .pointerInput(Unit) { detectTapGestures(onTap = { cerrarTeclado() }) }
    ) {
        BarraSuperior(onVolver)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Estado de la muestra
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "NUEVA MUESTRA",
                    color = Paleta.Etiqueta,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "PENDIENTE ENVÍO",
                    color = PendienteTexto,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PendienteFondo)
                        .border(1.5.dp, PendienteBorde, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // Fotografía: se toca para verla completa
            TarjetaFoto(
                rutaFoto = rutaFoto,
                onAmpliar = { verFoto = true },
                onRepetir = onRepetirFoto,
                onGaleria = {
                    selectorGaleria.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onEliminar = { eliminando = true }
            )

            // Métricas (se completan en el paso 2)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaMetrica(
                    titulo = "CONTEO BIOMASA",
                    valor = borrador.conteoIndM?.toString(),
                    unidad = "uds / m",
                    modifier = Modifier.weight(1f)
                )
                TarjetaMetrica(
                    titulo = "TALLA MEDIA",
                    valor = borrador.tallaMm?.toString(),
                    unidad = "mm",
                    modifier = Modifier.weight(1f)
                )
            }

            // Ubicación: lectura o edición en la misma pantalla
            Tarjeta(espacio = 12.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Anchor, contentDescription = null, tint = Paleta.Acento)
                        Text(
                            text = "UBICACIÓN Y MUESTREO",
                            color = Paleta.Texto,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (parametros != null) {
                        BotonEditar(editando = editando, onClick = { editando = !editando })
                    }
                }

                if (parametros == null) {
                    Text("Completa el formulario de Nueva Muestra.", color = Paleta.TextoSuave, fontSize = 13.sp)
                } else if (editando) {
                    Selector(
                        etiqueta = "Centro de Cultivo",
                        valor = CentroCultivo.disponibles.firstOrNull { it.id == parametros.centroId }?.nombre,
                        vacio = "Selecciona un centro",
                        icono = Icons.Filled.Business,
                        opciones = CentroCultivo.disponibles.map { it.nombre },
                        onElegir = { i ->
                            onParametrosCambiados(parametros.copy(centroId = CentroCultivo.disponibles[i].id))
                        }
                    )
                    Selector(
                        etiqueta = "Tren de Cultivo (Longline)",
                        valor = nombreTren(parametros.tren),
                        vacio = "Selecciona un tren",
                        icono = Icons.Filled.DirectionsBoat,
                        opciones = Trenes.map { it.second },
                        onElegir = { i -> onParametrosCambiados(parametros.copy(tren = Trenes[i].first)) }
                    )
                    Selector(
                        etiqueta = "Línea de Siembra",
                        valor = etiquetaLinea(parametros.linea),
                        vacio = "Selecciona una línea",
                        icono = Icons.Filled.Anchor,
                        opciones = Lineas.map { etiquetaLinea(it) },
                        onElegir = { i -> onParametrosCambiados(parametros.copy(linea = Lineas[i])) }
                    )
                    Text(
                        text = "Estrato de profundidad",
                        color = Paleta.Etiqueta,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Estrato.entries.forEach { e ->
                            OpcionEstrato(
                                estrato = e,
                                seleccionado = Estrato.desde(parametros.profundidadM) == e,
                                modifier = Modifier.weight(1f),
                                onClick = { onParametrosCambiados(parametros.copy(profundidadM = e.profundidadM)) }
                            )
                        }
                    }
                } else {
                    val centro = CentroCultivo.disponibles.firstOrNull { it.id == parametros.centroId }
                    val estrato = Estrato.desde(parametros.profundidadM)
                    Fila("Centro", centro?.nombre ?: "—")
                    Fila("Tren", nombreTren(parametros.tren))
                    Fila("Línea", "Línea %02d".format(parametros.linea))
                    Fila("Profundidad", "Estrato ${estrato.rango} (${estrato.nombre})")
                }
                Fila("Nivel fouling", borrador.fouling ?: "—")
            }

            // Notas del operador (editables)
            Tarjeta(espacio = 12.dp) {
                Text(
                    text = "NOTAS DEL OPERADOR EN TERRENO",
                    color = Paleta.Etiqueta,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = borrador.observacion,
                    onValueChange = { onObservacionCambiada(it.take(MAX_OBSERVACION)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 96.dp),
                    placeholder = { Text("Escribe una observación de la muestra…", color = Paleta.TextoSuave) },
                    minLines = 3,
                    maxLines = 6,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Paleta.Texto,
                        unfocusedTextColor = Paleta.Texto,
                        focusedBorderColor = Paleta.Acento,
                        unfocusedBorderColor = Paleta.BordeFuerte,
                        cursorColor = Paleta.Acento,
                        focusedContainerColor = Paleta.Campo,
                        unfocusedContainerColor = Paleta.Campo,
                    ),
                    supportingText = {
                        Text(
                            text = "${borrador.observacion.length}/$MAX_OBSERVACION",
                            color = Paleta.TextoSuave,
                            fontSize = 11.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                )
            }

            // Guardado local
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Paleta.Superficie)
                    .border(1.5.dp, Paleta.Borde, RoundedCornerShape(12.dp))
                    .clickable { guardarLocal = !guardarLocal }
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Guardar localmente en el dispositivo",
                        color = Paleta.Texto,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Almacenamiento seguro sin conexión",
                        color = Paleta.TextoSuave,
                        fontSize = 12.sp
                    )
                }
                Checkbox(
                    checked = guardarLocal,
                    onCheckedChange = { guardarLocal = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Paleta.Primario,
                        checkmarkColor = Paleta.SobrePrimario,
                        uncheckedColor = Paleta.BordeCampo
                    )
                )
            }
        }

        // Acción principal, siempre visible abajo
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Paleta.Pie)
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (listo) Paleta.Primario else Paleta.Campo)
                    .clickable(enabled = listo) {
                        cerrarTeclado()
                        confirmando = true
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "GUARDAR Y ENVIAR MUESTRA",
                    color = if (listo) Paleta.SobrePrimario else Paleta.TextoSuave,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (verFoto && rutaFoto != null) {
        VisorFoto(ruta = rutaFoto, onCerrar = { verFoto = false })
    }

    if (eliminando) {
        AlertDialog(
            onDismissRequest = { eliminando = false },
            containerColor = Paleta.Superficie,
            titleContentColor = Paleta.Texto,
            textContentColor = Paleta.Etiqueta,
            title = { Text("¿Eliminar la foto?", fontWeight = FontWeight.Bold) },
            text = { Text("Se quitará la foto de esta muestra. Podrás tomar otra o elegir una de la galería.") },
            confirmButton = {
                TextButton(onClick = {
                    eliminando = false
                    borrarArchivo(rutaFoto)
                    onFotoCambiada(null)
                }) { Text("ELIMINAR", color = Paleta.Error, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { eliminando = false }) {
                    Text("CANCELAR", color = Paleta.TextoSuave, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (confirmando) {
        AlertDialog(
            onDismissRequest = { confirmando = false },
            containerColor = Paleta.Superficie,
            titleContentColor = Paleta.Texto,
            textContentColor = Paleta.Etiqueta,
            title = { Text("¿Confirmar muestreo?", fontWeight = FontWeight.Bold) },
            text = { Text("Revisaste la fotografía y los datos. Se registrará la muestra y quedará lista para enviarse.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmando = false
                    onConfirmar()
                }) { Text("CONFIRMAR", color = Paleta.Acento, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { confirmando = false }) {
                    Text("CANCELAR", color = Paleta.TextoSuave, fontWeight = FontWeight.Bold)
                }
            }
        )
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
                text = "Vista previa",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "MUSSELAPP • OPERARIO",
                color = Paleta.TextoBarra,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Foto + acciones en orden: Repetir (cámara), Galería, Eliminar
@Composable
private fun TarjetaFoto(
    rutaFoto: String?,
    onAmpliar: () -> Unit,
    onRepetir: () -> Unit,
    onGaleria: () -> Unit,
    onEliminar: () -> Unit
) {
    val bitmap = remember(rutaFoto) { rutaFoto?.let { cargarBitmap(it, 4) } }
    val hora = remember(rutaFoto) {
        rutaFoto?.let {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(File(it).lastModified()))
        }
    }

    Tarjeta(sinPadding = true) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(Color.Black)
                .clickable(enabled = bitmap != null, onClick = onAmpliar),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Fotografía de la muestra. Toca para ampliar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Navy)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ZoomIn,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text("Toca para ampliar", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                if (hora != null) {
                    Text(
                        text = hora,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Navy)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            } else {
                Text("Sin fotografía", color = Color.White, fontSize = 14.sp)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BotonAccion(
                texto = if (bitmap != null) "Repetir" else "Tomar foto",
                icono = if (bitmap != null) Icons.Filled.Refresh else Icons.Filled.PhotoCamera,
                modifier = Modifier.weight(1f),
                onClick = onRepetir
            )
            BotonAccion(
                texto = "Galería",
                icono = Icons.Filled.PhotoLibrary,
                modifier = Modifier.weight(1f),
                onClick = onGaleria
            )
            BotonAccion(
                texto = "Eliminar",
                icono = Icons.Filled.Delete,
                modifier = Modifier.weight(1f),
                peligro = true,
                habilitado = rutaFoto != null,
                onClick = onEliminar
            )
        }
    }
}

@Composable
private fun BotonAccion(
    texto: String,
    icono: ImageVector,
    modifier: Modifier = Modifier,
    peligro: Boolean = false,
    habilitado: Boolean = true,
    onClick: () -> Unit
) {
    val color = when {
        !habilitado -> Paleta.BordeCampo
        peligro -> Paleta.Error
        else -> Paleta.Etiqueta
    }
    Column(
        modifier = modifier
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(1.5.dp, if (habilitado) color.copy(alpha = 0.7f) else Paleta.Borde, RoundedCornerShape(10.dp))
            .clickable(enabled = habilitado, onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = icono, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
        Text(text = texto, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun BotonEditar(editando: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .heightIn(min = 40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (editando) Paleta.Primario else Paleta.AcentoSuave)
            .border(1.5.dp, if (editando) Paleta.Primario else Paleta.Acento, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = if (editando) Icons.Filled.Check else Icons.Filled.Edit,
            contentDescription = null,
            tint = if (editando) Paleta.SobrePrimario else Paleta.Acento,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = if (editando) "LISTO" else "EDITAR",
            color = if (editando) Paleta.SobrePrimario else Paleta.Acento,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// Foto a pantalla completa: se acerca con dos dedos y se puede girar a vertical u horizontal
@Composable
internal fun VisorFoto(ruta: String, onCerrar: () -> Unit) {
    val original = remember(ruta) { cargarBitmap(ruta, 2) }
    var grados by remember { mutableStateOf(0) }
    var escala by remember { mutableStateOf(1f) }
    var desplazamiento by remember { mutableStateOf(Offset.Zero) }

    // La foto se gira de verdad (no solo se estira), para que siempre se vea completa
    val bitmap = remember(original, grados) {
        if (original == null || grados == 0) {
            original
        } else {
            val matriz = Matrix().apply { postRotate(grados.toFloat()) }
            Bitmap.createBitmap(original, 0, 0, original.width, original.height, matriz, true)
        }
    }

    fun girar(delta: Int) {
        grados = (grados + delta + 360) % 360
        escala = 1f
        desplazamiento = Offset.Zero
    }

    Dialog(
        onDismissRequest = onCerrar,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Fotografía de la muestra",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                escala = (escala * zoom).coerceIn(1f, 5f)
                                desplazamiento = if (escala == 1f) Offset.Zero else desplazamiento + pan
                            }
                        }
                        .graphicsLayer {
                            scaleX = escala
                            scaleY = escala
                            translationX = desplazamiento.x
                            translationY = desplazamiento.y
                        }
                )
            } else {
                Text(
                    text = "No se pudo abrir la foto",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            BotonVisor(
                icono = Icons.Filled.Close,
                descripcion = "Cerrar",
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(12.dp),
                onClick = onCerrar
            )

            // Girar a la izquierda / derecha
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BotonVisor(Icons.Filled.RotateLeft, "Girar a la izquierda") { girar(-90) }
                BotonVisor(Icons.Filled.RotateRight, "Girar a la derecha") { girar(90) }
            }
        }
    }
}

// Botón redondo para usar sobre la foto ampliada
@Composable
private fun BotonVisor(
    icono: ImageVector,
    descripcion: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Navy.copy(alpha = 0.85f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icono, contentDescription = descripcion, tint = Color.White)
    }
}

@Composable
private fun TarjetaMetrica(
    titulo: String,
    valor: String?,
    unidad: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Paleta.Superficie)
            .border(1.5.dp, Paleta.Borde, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = titulo, color = Paleta.TextoSuave, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = valor ?: "—",
                color = Paleta.Texto,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = unidad,
                color = Paleta.TextoSuave,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
    }
}

@Composable
private fun Fila(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Paleta.Campo)
            .border(1.5.dp, Paleta.BordeFuerte, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = etiqueta,
            color = Paleta.Etiqueta,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = valor,
            color = Paleta.Texto,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.6f)
        )
    }
}

// Copia la foto elegida en la galería a la carpeta de muestras de la app
private fun copiarEnApp(context: Context, uri: Uri): File? {
    return try {
        val carpeta = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "muestras")
        carpeta.mkdirs()
        val archivo = File(carpeta, "muestra_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { entrada ->
            archivo.outputStream().use { salida -> entrada.copyTo(salida) }
        } ?: return null
        archivo
    } catch (e: Exception) {
        null
    }
}

// Borra la copia de la app; la que está en la galería del teléfono no se toca
private fun borrarArchivo(ruta: String?) {
    if (ruta == null) return
    runCatching { File(ruta).delete() }
}

// Lee la foto reducida y la gira según la orientación que guardó la cámara
private fun cargarBitmap(ruta: String, reduccion: Int): Bitmap? {
    val opciones = BitmapFactory.Options().apply { inSampleSize = reduccion }
    val bitmap = BitmapFactory.decodeFile(ruta, opciones) ?: return null

    val orientacion = ExifInterface(ruta).getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL
    )
    val grados = when (orientacion) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> 0f
    }
    if (grados == 0f) return bitmap

    val matriz = Matrix().apply { postRotate(grados) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matriz, true)
}

@Preview(showBackground = true)
@Composable
fun VistaPreviaScreenPreview() {
    MusselAppTheme {
        VistaPreviaScreen(
            borrador = BorradorMuestra(
                parametros = ParametrosMuestra("dalcahue", "B", 12, 3),
                observacion = "Buena fijación, ligera presencia de macroalgas."
            )
        )
    }
}
