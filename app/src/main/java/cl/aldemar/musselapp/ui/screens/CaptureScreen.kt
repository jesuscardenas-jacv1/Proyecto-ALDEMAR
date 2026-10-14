package cl.aldemar.musselapp.ui.screens

import android.Manifest
import android.content.ContentValues
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.OrientationEventListener
import android.view.Surface
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview as CameraPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import cl.aldemar.musselapp.ui.muestra.Paleta
import cl.aldemar.musselapp.ui.muestra.VisorFoto
import cl.aldemar.musselapp.ui.theme.MusselAppTheme
import java.io.File

// Colores del sistema de diseño (DESIGN.md)
private val Navy = Color(0xFF0F172A)
private val Slate = Color(0xFF334155)
private val Fondo = Color(0xFFF1F5F9)
private val GrisTexto = Color(0xFF94A3B8)
private val GrisNav = Color(0xFF64748B)
private val GrisBoton = Color(0xFFE2E8F0)
private val AzulClaro = Color(0xFFDBEAFE)
private val AzulOscuro = Color(0xFF1E3A8A)

@Composable
fun CaptureScreen(
    onFotoLista: (String) -> Unit = {}
) {
    val context = LocalContext.current

    var tienePermiso by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    val pedirPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        tienePermiso = concedido
    }

    LaunchedEffect(Unit) {
        if (!tienePermiso) pedirPermiso.launch(Manifest.permission.CAMERA)
    }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    var fotoArchivo by remember { mutableStateOf<File?>(null) }
    var fotoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var capturando by remember { mutableStateOf(false) }
    val foto = fotoBitmap
    var estadoFoto by remember { mutableStateOf(EstadoFoto.SIN_FOTO) }
    var origenCamara by remember { mutableStateOf(false) }
    var camaraAbierta by remember { mutableStateOf(false) }
    var verFoto by remember { mutableStateOf(false) }
    var animando by remember { mutableStateOf(false) } // el botón se va como bote hacia la Vista previa
    var rutaSiguiente by remember { mutableStateOf<String?>(null) }
    val progresoBote = remember { Animatable(0f) }

    // Foto nueva (cámara o galería): queda PENDIENTE hasta que el operario la confirme
    fun alRecibirFoto(archivo: File, desdeCamara: Boolean) {
        if (estadoFoto == EstadoFoto.REVISAR) fotoArchivo?.delete()
        fotoArchivo = archivo
        fotoBitmap = cargarBitmap(archivo.absolutePath)
        origenCamara = desdeCamara
        capturando = false
        camaraAbierta = false
        estadoFoto = EstadoFoto.REVISAR
    }

    // Descarta la foto pendiente (si no se confirmó, no se guarda) y vuelve a la cámara
    fun volverACamara() {
        if (estadoFoto == EstadoFoto.REVISAR) fotoArchivo?.delete()
        fotoArchivo = null
        fotoBitmap = null
        estadoFoto = EstadoFoto.SIN_FOTO
    }

    // El operario confirma que la foto salió bien: recién ahí se guarda
    fun confirmarFoto() {
        val temporal = fotoArchivo ?: return
        val destino = nuevoArchivoMuestra(context)
        try {
            temporal.copyTo(destino, overwrite = true)
            temporal.delete()
        } catch (e: Exception) {
            Toast.makeText(context, "No se pudo guardar la foto", Toast.LENGTH_SHORT).show()
            return
        }
        fotoArchivo = destino

        // Las fotos de la cámara también van a la galería del teléfono
        estadoFoto = if (!origenCamara || guardarEnGaleria(context, destino)) {
            EstadoFoto.GUARDADA
        } else {
            EstadoFoto.SOLO_APP
        }
        val mensaje = if (estadoFoto == EstadoFoto.GUARDADA) {
            "Foto guardada en la galería y en la app"
        } else {
            "Foto guardada solo en la app"
        }
        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()

        // Con la foto confirmada, el botón se expande suavemente y pasa a la Vista previa
        rutaSiguiente = destino.absolutePath
        animando = true
    }

    // Navega cuando termina la animación del botón
    LaunchedEffect(animando) {
        if (!animando) return@LaunchedEffect
        progresoBote.animateTo(1f, tween(durationMillis = 650, easing = FastOutSlowInEasing))
        rutaSiguiente?.let(onFotoLista)
    }

    // Abre la cámara a pantalla completa (pide el permiso si aún no lo tiene)
    fun abrirCamara() {
        if (tienePermiso) camaraAbierta = true else pedirPermiso.launch(Manifest.permission.CAMERA)
    }

    val selectorGaleria = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val archivo = copiarDesdeGaleria(context, uri)
            if (archivo != null) {
                alRecibirFoto(archivo, desdeCamara = false)
            } else {
                Toast.makeText(context, "No se pudo abrir la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun tomarNuevaFoto() {
        if (capturando) return
        capturando = true
        tomarFoto(
            context = context,
            imageCapture = imageCapture,
            onGuardada = { archivo -> alRecibirFoto(archivo, desdeCamara = true) },
            onError = { capturando = false }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paleta.Fondo)
    ) {
        BarraSuperior()

        // Zona de cámara
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clipToBounds()
                .background(Color.Black)
        ) {
            when {
                foto != null -> {
                    Image(
                        bitmap = foto.asImageBitmap(),
                        contentDescription = "Foto de la muestra. Toca para ampliar",
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { verFoto = true },
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
                }
                else -> ZonaSinFoto(tienePermiso = tienePermiso)
            }
        }

        // Panel inferior
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TarjetaAlineacion()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (estadoFoto == EstadoFoto.REVISAR || animando) {
                    BotonLateral(Icons.Filled.Refresh, "Repetir", false) {
                        if (animando) return@BotonLateral
                        volverACamara()
                        abrirCamara()
                    }
                } else {
                    BotonLateral(Icons.Filled.PhotoLibrary, "Galería", false) {
                        selectorGaleria.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                }
                Obturador(
                    icono = if (estadoFoto == EstadoFoto.REVISAR || animando) Icons.Filled.Check else Icons.Filled.PhotoCamera,
                    descripcion = when (estadoFoto) {
                        EstadoFoto.REVISAR -> "Foto buena, guardar y continuar"
                        EstadoFoto.SIN_FOTO -> "Tomar foto"
                        else -> "Tomar otra foto"
                    },
                    habilitado = true,
                    respirar = estadoFoto == EstadoFoto.SIN_FOTO,
                    navegar = progresoBote.value,
                    onClick = {
                        if (animando) return@Obturador
                        when (estadoFoto) {
                            EstadoFoto.SIN_FOTO -> abrirCamara()
                            EstadoFoto.REVISAR -> confirmarFoto()
                            else -> {
                                volverACamara()
                                abrirCamara()
                            }
                        }
                    }
                )
                // Espacio del tamaño de un botón lateral, para que el obturador quede centrado
                Spacer(modifier = Modifier.size(56.dp))
            }
        }

        // TODO: esta barra es de toda la app; se moverá a la navegación
        BarraInferior()
    }

    val rutaVisor = fotoArchivo?.absolutePath
    if (verFoto && rutaVisor != null) {
        VisorFoto(ruta = rutaVisor, onCerrar = { verFoto = false })
    }

    if (camaraAbierta) {
        CamaraPantallaCompleta(
            imageCapture = imageCapture,
            capturando = capturando,
            onTomar = { tomarNuevaFoto() },
            onGaleria = {
                camaraAbierta = false
                selectorGaleria.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onCerrar = { camaraAbierta = false }
        )
    }
    }
}

// Zona que se ve antes de tomar la foto: la cámara se abre a pantalla completa con el obturador
@Composable
private fun ZonaSinFoto(tienePermiso: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.PhotoCamera,
            contentDescription = null,
            tint = GrisTexto,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Foto de la muestra",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (tienePermiso) {
                "Toca el botón de la cámara para abrirla a pantalla completa. Puedes tomar la foto en vertical u horizontal."
            } else {
                "Se necesita permiso de cámara. Toca el botón para concederlo."
            },
            color = GrisTexto,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}

// Cámara a pantalla completa con los botones integrados; gira con el teléfono (vertical u horizontal)
@Composable
private fun CamaraPantallaCompleta(
    imageCapture: ImageCapture,
    capturando: Boolean,
    onTomar: () -> Unit,
    onGaleria: () -> Unit,
    onCerrar: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val horizontal = LocalConfiguration.current.orientation ==
            android.content.res.Configuration.ORIENTATION_LANDSCAPE

    BackHandler(onBack = onCerrar)

    // Mientras la cámara está abierta: permite girar la pantalla y oculta las barras del sistema
    DisposableEffect(Unit) {
        val actividad = context.buscarActividad()
        val orientacionPrevia = actividad?.requestedOrientation
        actividad?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR

        val controlador = actividad?.let { WindowCompat.getInsetsController(it.window, view) }
        controlador?.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controlador?.hide(WindowInsetsCompat.Type.systemBars())

        onDispose {
            controlador?.show(WindowInsetsCompat.Type.systemBars())
            if (actividad != null && orientacionPrevia != null) {
                actividad.requestedOrientation = orientacionPrevia
            }
        }
    }

    // La foto sale con la orientación en que el operario sostiene el teléfono
    DisposableEffect(Unit) {
        val escucha = object : OrientationEventListener(context) {
            override fun onOrientationChanged(grados: Int) {
                if (grados == OrientationEventListener.ORIENTATION_UNKNOWN) return
                imageCapture.targetRotation = when (grados) {
                    in 45..134 -> Surface.ROTATION_270
                    in 135..224 -> Surface.ROTATION_180
                    in 225..314 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }
            }
        }
        escucha.enable()
        onDispose { escucha.disable() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        VistaPreviaCamara(
            imageCapture = imageCapture,
            modifier = Modifier.fillMaxSize()
        )
        EsquinasVisor(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(if (horizontal) 0.5f else 0.7f)
                .fillMaxHeight(if (horizontal) 0.6f else 0.4f)
        )

        // Botones integrados sobre la imagen
        if (horizontal) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BotonCamara(Icons.Filled.Close, "Cerrar cámara", onCerrar)
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.size(52.dp))
                    Obturador(
                        icono = Icons.Filled.PhotoCamera,
                        descripcion = "Tomar foto",
                        habilitado = !capturando,
                        respirar = false,
                        onClick = onTomar
                    )
                    BotonCamara(Icons.Filled.PhotoLibrary, "Galería", onGaleria)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    BotonCamara(Icons.Filled.Close, "Cerrar cámara", onCerrar)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BotonCamara(Icons.Filled.PhotoLibrary, "Galería", onGaleria)
                    Obturador(
                        icono = Icons.Filled.PhotoCamera,
                        descripcion = "Tomar foto",
                        habilitado = !capturando,
                        respirar = false,
                        onClick = onTomar
                    )
                    Spacer(modifier = Modifier.size(52.dp))
                }
            }
        }
    }
}

// Botón redondo semitransparente para usar sobre la cámara
@Composable
private fun BotonCamara(icono: ImageVector, descripcion: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icono,
            contentDescription = descripcion,
            tint = Color.White
        )
    }
}

private tailrec fun Context.buscarActividad(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.buscarActividad()
    else -> null
}

@Composable
private fun BarraSuperior() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Navy)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
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
        Icon(
            imageVector = Icons.Filled.Sync,
            contentDescription = "Sincronizar",
            tint = Color.White
        )
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Slate),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "Perfil",
                tint = Color.White
            )
        }
    }
}

// Esquinas blancas del visor.
// TODO: chips flotantes (flash, grilla, etc.) si se requieren
@Composable
private fun EsquinasVisor(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val largo = 28.dp.toPx()
        val grosor = 3.dp.toPx()
        val color = Color.White
        val ancho = size.width
        val alto = size.height

        // arriba izquierda
        drawLine(color, Offset(0f, 0f), Offset(largo, 0f), grosor)
        drawLine(color, Offset(0f, 0f), Offset(0f, largo), grosor)
        // arriba derecha
        drawLine(color, Offset(ancho, 0f), Offset(ancho - largo, 0f), grosor)
        drawLine(color, Offset(ancho, 0f), Offset(ancho, largo), grosor)
        // abajo izquierda
        drawLine(color, Offset(0f, alto), Offset(largo, alto), grosor)
        drawLine(color, Offset(0f, alto), Offset(0f, alto - largo), grosor)
        // abajo derecha
        drawLine(color, Offset(ancho, alto), Offset(ancho - largo, alto), grosor)
        drawLine(color, Offset(ancho, alto), Offset(ancho, alto - largo), grosor)
    }
}

@Composable
private fun TarjetaAlineacion() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Paleta.Superficie)
            .border(1.5.dp, Paleta.Borde, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Paleta.AcentoSuave),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Straighten,
                contentDescription = null,
                tint = Paleta.Acento
            )
        }
        Column {
            Text(
                text = "Alineación de Muestra",
                color = Paleta.Texto,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Mantenga la regla calibrada paralela a la línea de choritos para cálculo automático.",
                color = Paleta.TextoSuave,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun BotonLateral(
    icono: ImageVector,
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (seleccionado) Paleta.Borde else Paleta.Campo)
                .border(1.5.dp, Paleta.BordeCampo, RoundedCornerShape(12.dp))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icono,
                contentDescription = texto,
                tint = if (seleccionado) Color.White else Paleta.Etiqueta
            )
        }
        Text(
            text = texto,
            color = Paleta.Etiqueta,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private enum class EstadoFoto { SIN_FOTO, REVISAR, GUARDADA, SOLO_APP }

// Botón principal: "respira" esperando la foto, se encoge al presionarlo y, al confirmar,
// libera un aro suave que se expande y se disuelve (como un respiro) antes de pasar de pantalla
@Composable
private fun Obturador(
    icono: ImageVector,
    descripcion: String,
    habilitado: Boolean,
    respirar: Boolean,
    navegar: Float = 0f, // 0 = quieto; hacia 1 el botón se expande y se desvanece
    onClick: () -> Unit
) {
    val interaccion = remember { MutableInteractionSource() }
    val presionado by interaccion.collectIsPressedAsState()

    val escalaPresion by animateFloatAsState(
        targetValue = if (presionado) 0.88f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "presion"
    )
    val respiracion by rememberInfiniteTransition(label = "respiracion").animateFloat(
        initialValue = 1f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "escala"
    )
    val escalaBase = escalaPresion * (if (respirar && habilitado && navegar == 0f) respiracion else 1f)
    // Al confirmar: una leve contracción y luego se expande disolviéndose
    val escala = if (navegar <= 0f) escalaBase else {
        if (navegar < 0.25f) 1f - 0.08f * (navegar / 0.25f)
        else 0.92f + 0.4f * ((navegar - 0.25f) / 0.75f)
    }
    val opacidad = if (navegar < 0.4f) 1f else (1f - (navegar - 0.4f) / 0.6f).coerceIn(0f, 1f)
    val progresoAro = ((navegar - 0.15f) / 0.85f).coerceIn(0f, 1f)

    Box(modifier = Modifier.size(76.dp), contentAlignment = Alignment.Center) {
        if (navegar > 0f) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .graphicsLayer {
                        val e = 1f + 1.1f * progresoAro
                        scaleX = e
                        scaleY = e
                        alpha = 0.45f * (1f - progresoAro)
                    }
                    .border(2.dp, Navy, CircleShape)
            )
        }
        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = escala
                    scaleY = escala
                    alpha = opacidad
                }
                .size(76.dp)
                .clip(CircleShape)
                .background(Navy)
                .padding(4.dp)
                .clip(CircleShape)
                .background(Color.White)
                .padding(5.dp)
                .clip(CircleShape)
                .background(if (habilitado) Navy else Paleta.TextoSuave)
                .clickable(
                    interactionSource = interaccion,
                    indication = null,
                    enabled = habilitado,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icono,
                contentDescription = descripcion,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun BarraInferior() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Paleta.Pie)
            .navigationBarsPadding()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ItemNavegacion(Icons.Filled.GridView, "Muestras", false)
        ItemNavegacion(Icons.Filled.AddCircleOutline, "Nueva Muestra", true)
        ItemNavegacion(Icons.Filled.History, "Historial", false)
        ItemNavegacion(Icons.Filled.ManageAccounts, "Supervisión", false)
    }
}

@Composable
private fun ItemNavegacion(icono: ImageVector, texto: String, activo: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .width(56.dp)
                .height(30.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(if (activo) Paleta.AcentoSuave else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icono,
                contentDescription = texto,
                tint = if (activo) Paleta.Acento else Paleta.TextoSuave,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = texto,
            color = if (activo) Paleta.Acento else Paleta.TextoSuave,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun VistaPreviaCamara(
    imageCapture: ImageCapture,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    // En el panel Preview de Android Studio no hay cámara real
    if (LocalInspectionMode.current) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Vista previa de cámara", color = Color.White)
        }
        return
    }

    val contexto = LocalContext.current
    DisposableEffect(Unit) {
        onDispose {
            runCatching { ProcessCameraProvider.getInstance(contexto).get().unbindAll() }
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FIT_CENTER
            }
            val futuro = ProcessCameraProvider.getInstance(ctx)

            futuro.addListener({
                val proveedor = futuro.get()
                val vistaPrevia = CameraPreview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                proveedor.unbindAll()
                proveedor.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    vistaPrevia,
                    imageCapture
                )
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )
}

// Crea un archivo nuevo en la carpeta de muestras de la app
private fun nuevoArchivoMuestra(context: Context): File {
    val carpeta = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "muestras")
    carpeta.mkdirs()
    return File(carpeta, "muestra_${System.currentTimeMillis()}.jpg")
}

// Archivo temporal: la foto solo pasa a la carpeta de la app si el operario la confirma
private fun nuevoArchivoTemporal(context: Context): File {
    val carpeta = File(context.cacheDir, "pendientes")
    carpeta.mkdirs()
    return File(carpeta, "pendiente_${System.currentTimeMillis()}.jpg")
}

private fun tomarFoto(
    context: Context,
    imageCapture: ImageCapture,
    onGuardada: (File) -> Unit,
    onError: () -> Unit
) {
    val archivo = nuevoArchivoTemporal(context)
    val opciones = ImageCapture.OutputFileOptions.Builder(archivo).build()

    imageCapture.takePicture(
        opciones,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                onGuardada(archivo)
            }

            override fun onError(exception: ImageCaptureException) {
                Toast.makeText(context, "No se pudo tomar la foto", Toast.LENGTH_SHORT).show()
                onError()
            }
        }
    )
}

// Guarda una copia en la galería del teléfono (Pictures/MusselApp).
// En Android 10+ no necesita permisos; en versiones anteriores se omite.
private fun guardarEnGaleria(context: Context, archivo: File): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
    return try {
        val valores = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, archivo.name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MusselApp")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, valores)
            ?: return false

        resolver.openOutputStream(uri)?.use { salida ->
            archivo.inputStream().use { entrada -> entrada.copyTo(salida) }
        } ?: return false

        valores.clear()
        valores.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, valores, null, null)
        true
    } catch (e: Exception) {
        false
    }
}

// Copia la imagen elegida en la galería a la carpeta de la app
private fun copiarDesdeGaleria(context: Context, uri: Uri): File? {
    return try {
        val archivo = nuevoArchivoTemporal(context)
        context.contentResolver.openInputStream(uri)?.use { entrada ->
            archivo.outputStream().use { salida -> entrada.copyTo(salida) }
        } ?: return null
        archivo
    } catch (e: Exception) {
        null
    }
}

// Lee la foto reducida y la gira según la orientación que guardó la cámara
private fun cargarBitmap(ruta: String): Bitmap? {
    val opciones = BitmapFactory.Options().apply { inSampleSize = 4 }
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
fun CaptureScreenPreview() {
    MusselAppTheme {
        CaptureScreen()
    }
}
