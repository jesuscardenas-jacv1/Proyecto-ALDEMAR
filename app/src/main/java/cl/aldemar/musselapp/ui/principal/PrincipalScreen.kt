package cl.aldemar.musselapp.ui.principal

import android.app.Activity
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChecklistRtl
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.aldemar.musselapp.R
import cl.aldemar.musselapp.data.model.CentroCultivo
import cl.aldemar.musselapp.data.model.EstadoMuestra
import cl.aldemar.musselapp.data.model.Muestra
import cl.aldemar.musselapp.data.model.Rol
import cl.aldemar.musselapp.data.model.Usuario
import cl.aldemar.musselapp.data.repository.FakeMuestraRepository
import cl.aldemar.musselapp.ui.theme.EstadoCorregidoFondo
import cl.aldemar.musselapp.ui.theme.EstadoCorregidoTexto
import cl.aldemar.musselapp.ui.theme.EstadoObservadoFondo
import cl.aldemar.musselapp.ui.theme.EstadoObservadoTexto
import cl.aldemar.musselapp.ui.theme.EstadoPendienteFondo
import cl.aldemar.musselapp.ui.theme.EstadoPendienteTexto
import cl.aldemar.musselapp.ui.theme.EstadoValidadoFondo
import cl.aldemar.musselapp.ui.theme.EstadoValidadoTexto
import cl.aldemar.musselapp.ui.theme.MusselAppTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Secciones de la app accesibles desde la Pantalla Principal. */
enum class Destino(val etiqueta: String, val icono: ImageVector) {
    MUESTRAS("Muestras", Icons.Filled.Dataset),
    NUEVA_MUESTRA("Nueva Muestra", Icons.Filled.AddCircleOutline),
    HISTORIAL("Historial", Icons.Filled.History),
    SUPERVISION("Supervisión", Icons.Filled.ManageAccounts),
    ;

    companion object {
        /** Menú de opciones según el rol: el operario registra, el supervisor revisa. */
        fun disponiblesPara(rol: Rol): List<Destino> = when (rol) {
            Rol.OPERARIO -> listOf(MUESTRAS, NUEVA_MUESTRA, HISTORIAL)
            Rol.SUPERVISOR -> listOf(MUESTRAS, HISTORIAL, SUPERVISION)
        }
    }
}

/** Punto de entrada con estado: conecta el ViewModel con la UI. */
@Composable
fun PrincipalRoute(
    usuario: Usuario,
    centro: CentroCultivo,
    onCerrarSesion: () -> Unit,
    viewModel: PrincipalViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PrincipalScreen(
        usuario = usuario,
        centro = centro,
        uiState = uiState,
        onSincronizar = viewModel::sincronizar,
        onCerrarSesion = onCerrarSesion,
        onMensajeMostrado = viewModel::onMensajeMostrado,
    )
}

/** UI sin estado de la Pantalla Principal (diseño "2. Principal / Dashboard" de Stitch). */
@Composable
fun PrincipalScreen(
    usuario: Usuario,
    centro: CentroCultivo,
    uiState: PrincipalUiState,
    onSincronizar: () -> Unit,
    onCerrarSesion: () -> Unit,
    onMensajeMostrado: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val destinos = Destino.disponiblesPara(usuario.rol)

    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            onMensajeMostrado()
        }
    }
    // Pantallas que aún se desarrollan en otras ramas (feature/...)
    fun proximamente(destino: Destino) {
        scope.launch { snackbarHostState.showSnackbar("«${destino.etiqueta}» disponible próximamente") }
    }

    IconosBarraEstadoClaros()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BarraSuperior(
                usuario = usuario,
                pendientesSincronizar = uiState.resumen.pendientesSincronizar,
                sincronizando = uiState.sincronizando,
                onSincronizar = onSincronizar,
                onCerrarSesion = onCerrarSesion,
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                destinos.forEach { destino ->
                    NavigationBarItem(
                        selected = destino == Destino.MUESTRAS,
                        onClick = { if (destino != Destino.MUESTRAS) proximamente(destino) },
                        icon = { Icon(destino.icono, contentDescription = null) },
                        label = { Text(destino.etiqueta) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = EstadoValidadoFondo,
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                        ),
                    )
                }
            }
        },
        floatingActionButton = {
            if (usuario.rol == Rol.OPERARIO) {
                FloatingActionButton(
                    onClick = { proximamente(Destino.NUEVA_MUESTRA) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Registrar muestra rápida")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TarjetaOperador(usuario = usuario, centro = centro)
            BannerRol(rol = usuario.rol)
            AccionPrincipal(
                rol = usuario.rol,
                pendientesRevision = uiState.resumen.pendientes,
                onClick = {
                    proximamente(if (usuario.rol == Rol.OPERARIO) Destino.NUEVA_MUESTRA else Destino.SUPERVISION)
                },
            )
            MetricasJornada(resumen = uiState.resumen)
            TarjetaHistorial(resumen = uiState.resumen, onVerTodo = { proximamente(Destino.HISTORIAL) })
            TarjetaSincronizacion(
                pendientes = uiState.resumen.pendientesSincronizar,
                sincronizando = uiState.sincronizando,
                onSincronizar = onSincronizar,
            )
            MuestrasRecientes(
                muestras = uiState.recientes,
                ultimaRegistradaEn = uiState.resumen.ultimaRegistradaEn,
                onMuestraClick = {
                    scope.launch { snackbarHostState.showSnackbar("Detalle de muestra disponible próximamente") }
                },
            )
        }
    }
}

/** La barra superior es oscura: los íconos de la barra de estado deben ser claros. */
@Composable
private fun IconosBarraEstadoClaros() {
    if (LocalInspectionMode.current) return
    val view = LocalView.current
    DisposableEffect(view) {
        val window = (view.context as Activity).window
        val controlador = WindowCompat.getInsetsController(window, view)
        val anterior = controlador.isAppearanceLightStatusBars
        controlador.isAppearanceLightStatusBars = false
        onDispose { controlador.isAppearanceLightStatusBars = anterior }
    }
}

@Composable
private fun BarraSuperior(
    usuario: Usuario,
    pendientesSincronizar: Int,
    sincronizando: Boolean,
    onSincronizar: () -> Unit,
    onCerrarSesion: () -> Unit,
) {
    var menuAbierto by remember { mutableStateOf(false) }
    Surface(color = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                Image(painterResource(R.drawable.logo_musselapp), contentDescription = "Logo MusselApp", modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("MusselApp", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        " • Muestras",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (pendientesSincronizar == 0) Color(0xFF4ADE80) else Color(0xFFFBBF24)),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (pendientesSincronizar == 0) "ONLINE / SINCRONIZADO" else "ONLINE / $pendientesSincronizar EN COLA",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
                    )
                }
            }
            IconButton(onClick = onSincronizar, enabled = !sincronizando) {
                IconoGiratorio(Icons.Filled.Sync, girando = sincronizando, descripcion = "Sincronizar", tinte = MaterialTheme.colorScheme.onPrimary)
            }
            Box {
                IconButton(onClick = { menuAbierto = true }) {
                    Avatar(nombre = usuario.nombre, tamano = 36, claro = true)
                }
                DropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
                    DropdownMenuItem(
                        text = { Text("Cerrar sesión") },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                        onClick = {
                            menuAbierto = false
                            onCerrarSesion()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun IconoGiratorio(icono: ImageVector, girando: Boolean, descripcion: String?, tinte: Color) {
    val giro = rememberInfiniteTransition(label = "giro")
    val angulo by giro.animateFloat(
        initialValue = 0f,
        targetValue = -360f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Restart),
        label = "angulo",
    )
    Icon(
        icono,
        contentDescription = descripcion,
        tint = tinte,
        modifier = Modifier.graphicsLayer { rotationZ = if (girando) angulo else 0f },
    )
}

/** Avatar con las iniciales del usuario (la app no guarda fotos de personas). */
@Composable
private fun Avatar(nombre: String, tamano: Int, claro: Boolean) {
    val iniciales = nombre.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
    Box(
        modifier = Modifier
            .size(tamano.dp)
            .clip(CircleShape)
            .background(if (claro) Color.White else MaterialTheme.colorScheme.secondary),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            iniciales,
            color = if (claro) MaterialTheme.colorScheme.primary else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (tamano * 0.38f).sp,
        )
    }
}

@Composable
private fun TarjetaOperador(usuario: Usuario, centro: CentroCultivo) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shadowElevation = 3.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Avatar(nombre = usuario.nombre, tamano = 56, claro = false)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (usuario.rol == Rol.OPERARIO) "OPERARIO DE TERRENO" else "SUPERVISOR DE CALIDAD",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
                )
                Text(usuario.nombre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    centro.nombre,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(8.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Sensors, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("ONLINE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun BannerRol(rol: Rol) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(EstadoValidadoFondo),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Badge, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                if (rol == Rol.OPERARIO) "ROL: OPERARIO (MUESTREADOR DE CAMPO)" else "ROL: SUPERVISOR (CONTROL DE CALIDAD)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                if (rol == Rol.OPERARIO) "Acceso autorizado: captura fotográfica, conteo y registro"
                else "Acceso autorizado: revisión, observación y validación",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Acción principal según el rol: registrar (operario) o revisar (supervisor). */
@Composable
private fun AccionPrincipal(rol: Rol, pendientesRevision: Int, onClick: () -> Unit) {
    val operario = rol == Rol.OPERARIO
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(EstadoValidadoFondo),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (operario) Icons.Filled.AddCircle else Icons.Filled.FactCheck,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp),
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (operario) "LÍNEA ACTIVA" else "$pendientesRevision PENDIENTES",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(EstadoValidadoFondo)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    if (operario) "NUEVA MUESTRA" else "REVISAR MUESTRAS",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 26.sp,
                    maxLines = 2,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    if (operario) "Registro de muestra en cuerda y tren activo" else "Validar u observar las muestras del día",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                )
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.ChevronRight, contentDescription = null)
            }
        }
    }
}

@Composable
private fun EncabezadoSeccion(titulo: String, detalle: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        if (detalle != null) {
            Text(detalle, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
private fun MetricasJornada(resumen: ResumenJornada) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        EncabezadoSeccion("Métricas de la Jornada", "HOY")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TarjetaMetrica("Talla Med.", Icons.Filled.Straighten, "${resumen.tallaPromedioMm}", "mm", Modifier.weight(1f)) {
                TextoMetrica(resumen.calibre)
            }
            TarjetaMetrica("Conteo Prom.", Icons.Filled.GridView, "${resumen.conteoPromedioIndM}", "ind/m", Modifier.weight(1f)) {
                TextoMetrica(resumen.densidad)
            }
            TarjetaMetrica("Líneas", Icons.Filled.ChecklistRtl, "${resumen.lineasMuestreadas}", "/${resumen.lineasTotales}", Modifier.weight(1f)) {
                LinearProgressIndicator(
                    progress = { resumen.avanceLineas },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainer,
                    drawStopIndicator = {},
                )
            }
        }
    }
}

@Composable
private fun TextoMetrica(texto: String) {
    Text(texto, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary)
}

@Composable
private fun TarjetaMetrica(
    titulo: String,
    icono: ImageVector,
    valor: String,
    unidad: String,
    modifier: Modifier = Modifier,
    pie: @Composable () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = modifier.fillMaxHeight(),
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    titulo,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text(valor, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.width(3.dp))
                Text(unidad, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(bottom = 3.dp))
            }
            Spacer(Modifier.weight(1f))
            pie()
        }
    }
}

@Composable
private fun TarjetaHistorial(resumen: ResumenJornada, onVerTodo: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconoCuadrado(Icons.Filled.HistoryEdu, fondo = MaterialTheme.colorScheme.surfaceContainer)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Historial de Muestras", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${resumen.totalMuestras} registros capturados hoy",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
                TextButton(onClick = onVerTodo) {
                    Text("Ver todo", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ContadorEstado(resumen.validadas, "VALIDADAS", EstadoPendienteFondo, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                ContadorEstado(resumen.pendientes, "PENDIENTES", EstadoPendienteFondo, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                ContadorEstado(resumen.observadas, "OBSERVADAS", EstadoObservadoFondo, EstadoObservadoTexto, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ContadorEstado(cantidad: Int, etiqueta: String, fondo: Color, texto: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(fondo)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("$cantidad", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = texto)
        Text(etiqueta, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = texto.copy(alpha = 0.8f))
    }
}

@Composable
private fun IconoCuadrado(icono: ImageVector, fondo: Color) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(fondo),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun TarjetaSincronizacion(pendientes: Int, sincronizando: Boolean, onSincronizar: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconoCuadrado(Icons.Filled.CloudSync, fondo = EstadoValidadoFondo)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Sincronización Satelital", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (pendientes == 0) "Todas las muestras están sincronizadas" else "$pendientes muestras almacenadas localmente",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
                Text(
                    if (pendientes == 0) "Al día" else "En cola",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (pendientes == 0) EstadoValidadoTexto else EstadoPendienteTexto,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (pendientes == 0) EstadoValidadoFondo else EstadoPendienteFondo)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.WifiTethering, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    "Enlace Iridium activo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f),
                )
                Button(
                    onClick = onSincronizar,
                    enabled = !sincronizando && pendientes > 0,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                ) {
                    if (sincronizando) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onSecondary)
                        Spacer(Modifier.width(8.dp))
                        Text("Sincronizando…", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Filled.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Sincronizar ahora", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun MuestrasRecientes(muestras: List<Muestra>, ultimaRegistradaEn: Long?, onMuestraClick: (Muestra) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        EncabezadoSeccion("Muestras Recientes", ultimaRegistradaEn?.let { "Última ${haceCuanto(it)}" })
        if (muestras.isEmpty()) {
            Text(
                "Aún no hay muestras registradas hoy",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
        muestras.forEach { muestra -> FilaMuestra(muestra, onClick = { onMuestraClick(muestra) }) }
    }
}

@Composable
private fun FilaMuestra(muestra: Muestra, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 64.dp)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(muestra.codigoLinea, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleSmall)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Línea %02d • Tren %s".format(muestra.linea, muestra.tren),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "Prof. ${muestra.profundidadM} m • ${muestra.tallaMm} mm • ${muestra.conteoIndM} ind/m • ${muestra.observacion}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ChipEstado(muestra.estado)
                Text(
                    FORMATO_HORA.format(Date(muestra.registradaEn)),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
    }
}

@Composable
fun ChipEstado(estado: EstadoMuestra) {
    val (fondo, texto, icono) = when (estado) {
        EstadoMuestra.VALIDADO -> Triple(EstadoValidadoFondo, EstadoValidadoTexto, Icons.Filled.CheckCircle)
        EstadoMuestra.OBSERVADO -> Triple(EstadoObservadoFondo, EstadoObservadoTexto, Icons.Filled.Warning)
        EstadoMuestra.CORREGIDO -> Triple(EstadoCorregidoFondo, EstadoCorregidoTexto, Icons.Filled.EditNote)
        EstadoMuestra.PENDIENTE -> Triple(EstadoPendienteFondo, EstadoPendienteTexto, Icons.Filled.Schedule)
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(fondo)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icono, contentDescription = null, tint = texto, modifier = Modifier.size(12.dp))
        Spacer(Modifier.width(4.dp))
        Text(estado.etiqueta.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = texto)
    }
}

private val FORMATO_HORA = SimpleDateFormat("HH:mm", Locale("es", "CL"))

private fun haceCuanto(instante: Long, ahora: Long = System.currentTimeMillis()): String {
    val minutos = ((ahora - instante) / 60_000).coerceAtLeast(0)
    return when {
        minutos < 1 -> "hace instantes"
        minutos < 60 -> "hace $minutos min"
        else -> "hace ${minutos / 60} h"
    }
}

@Preview(showBackground = true, heightDp = 1600)
@Composable
private fun PrincipalOperarioPreview() {
    val muestras = FakeMuestraRepository().muestras.value
    MusselAppTheme {
        PrincipalScreen(
            usuario = Usuario("11.111.111-1", "Carla Vera", Rol.OPERARIO),
            centro = CentroCultivo.disponibles.first(),
            uiState = PrincipalUiState(
                resumen = ResumenJornada.desde(muestras),
                recientes = muestras.take(3),
            ),
            onSincronizar = {},
            onCerrarSesion = {},
            onMensajeMostrado = {},
        )
    }
}
