package cl.aldemar.musselapp.ui.login

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Anchor
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Sailing
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.aldemar.musselapp.R
import cl.aldemar.musselapp.data.model.CentroCultivo
import cl.aldemar.musselapp.data.model.Rol
import cl.aldemar.musselapp.data.model.Usuario
import cl.aldemar.musselapp.ui.theme.MusselAppTheme
import kotlinx.coroutines.launch
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup

/** Punto de entrada con estado: conecta el ViewModel con la UI. */
@Composable
fun LoginRoute(
    onLoginExitoso: (Usuario) -> Unit,
    viewModel: LoginViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.usuario) {
        uiState.usuario?.let(onLoginExitoso)
    }

    LoginScreen(
        uiState = uiState,
        onRolChange = viewModel::onRolChange,
        onCambiarPerfil = viewModel::onCambiarPerfil,
        onRutChange = viewModel::onRutChange,
        onRutFocusLost = viewModel::onRutFocusLost,
        onPasswordChange = viewModel::onPasswordChange,
        onTogglePasswordVisible = viewModel::onTogglePasswordVisible,
        onCentroChange = viewModel::onCentroChange,
        onModoOfflineChange = viewModel::onModoOfflineChange,
        onIngresar = viewModel::ingresar,
        onMensajeMostrado = viewModel::onMensajeMostrado,
    )
}

/**
 * UI sin estado del Login (diseño "1. Login - MusselApp" de Stitch) en dos pasos:
 * 1) selección de perfil operativo, 2) ingreso de credenciales.
 */
@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onRolChange: (Rol) -> Unit,
    onCambiarPerfil: () -> Unit,
    onRutChange: (String) -> Unit,
    onRutFocusLost: () -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisible: () -> Unit,
    onCentroChange: (CentroCultivo) -> Unit,
    onModoOfflineChange: (Boolean) -> Unit,
    onIngresar: () -> Unit,
    onMensajeMostrado: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    // Perfil recién tocado cuya animación se está mostrando
    var rolEnAnimacion by remember { mutableStateOf<Rol?>(null) }
    val paso = uiState.rol?.let { PasoLogin.Credenciales(it) }
        ?: rolEnAnimacion?.let { PasoLogin.Animacion(it) }
        ?: PasoLogin.Perfil

    LaunchedEffect(uiState.mensajeError) {
        uiState.mensajeError?.let {
            snackbarHostState.showSnackbar(it)
            onMensajeMostrado()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Encabezado()

            Column(
                modifier = Modifier.widthIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                AnimatedContent(
                    targetState = paso,
                    transitionSpec = {
                        (fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 12 }) togetherWith fadeOut(tween(150))
                    },
                    label = "pasoLogin",
                ) { p ->
                    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        when (p) {
                            PasoLogin.Perfil -> PasoSeleccionPerfil(onRolChange = { rolEnAnimacion = it })
                            is PasoLogin.Animacion -> AnimacionPerfil(
                                rol = p.rol,
                                onTerminada = {
                                    onRolChange(p.rol)
                                    rolEnAnimacion = null
                                },
                            )
                            is PasoLogin.Credenciales -> {
                                // El botón Atrás del teléfono vuelve a la selección de perfil
                                BackHandler(enabled = !uiState.cargando, onBack = onCambiarPerfil)
                                PasoCredenciales(
                                    rol = p.rol,
                                    uiState = uiState,
                                    onCambiarPerfil = onCambiarPerfil,
                                    onRutChange = onRutChange,
                                    onRutFocusLost = onRutFocusLost,
                                    onPasswordChange = onPasswordChange,
                                    onTogglePasswordVisible = onTogglePasswordVisible,
                                    onCentroChange = onCentroChange,
                                    onModoOfflineChange = onModoOfflineChange,
                                    onIngresar = onIngresar,
                                    onOlvidoPin = {
                                        scope.launch { snackbarHostState.showSnackbar("Solicite el restablecimiento del PIN a su supervisor") }
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Pasos visibles del Login. */
private sealed interface PasoLogin {
    data object Perfil : PasoLogin
    data class Animacion(val rol: Rol) : PasoLogin
    data class Credenciales(val rol: Rol) : PasoLogin
}

/** Paso 1: el usuario elige con qué perfil operativo ingresa. */
@Composable
private fun PasoSeleccionPerfil(onRolChange: (Rol) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            EtiquetaConIcono("Perfil Operativo", Icons.Filled.Badge)
            Text(
                "Seleccione con qué perfil desea ingresar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
        Column(
            modifier = Modifier.selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Rol.entries.forEach { rol ->
                TarjetaPerfil(rol = rol, onClick = { onRolChange(rol) })
            }
        }
    }
}

@Composable
private fun TarjetaPerfil(rol: Rol, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = false, role = Role.Button, onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(iconoRol(rol), contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(rol.etiqueta, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Text(rol.descripcion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            }
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
        }
    }
}

private fun iconoRol(rol: Rol): ImageVector = when (rol) {
    Rol.OPERARIO -> Icons.Filled.Sailing
    Rol.SUPERVISOR -> Icons.Filled.FactCheck
}

/** Paso 2: credenciales del perfil elegido. */
@Composable
private fun PasoCredenciales(
    rol: Rol,
    uiState: LoginUiState,
    onCambiarPerfil: () -> Unit,
    onRutChange: (String) -> Unit,
    onRutFocusLost: () -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisible: () -> Unit,
    onCentroChange: (CentroCultivo) -> Unit,
    onModoOfflineChange: (Boolean) -> Unit,
    onIngresar: () -> Unit,
    onOlvidoPin: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val pinFocus = remember { FocusRequester() }

    PerfilSeleccionado(rol = rol, habilitado = !uiState.cargando, onCambiar = onCambiarPerfil)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CampoFormulario(
                etiqueta = "RUT / Código de Operario",
                valor = uiState.rut,
                onValorChange = onRutChange,
                icono = Icons.Filled.Fingerprint,
                placeholder = "Ej: 15.482.901-6",
                error = uiState.rutError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Next,
                ),
                // "Siguiente" va directo al PIN, sin pasar por "¿Olvidó PIN?"
                keyboardActions = KeyboardActions(onNext = { pinFocus.requestFocus() }),
                modifier = Modifier.onFocusChanged { if (!it.isFocused) onRutFocusLost() },
            )

            CampoFormulario(
                etiqueta = "Contraseña de Seguridad",
                valor = uiState.password,
                onValorChange = onPasswordChange,
                icono = Icons.Filled.Lock,
                placeholder = "PIN",
                error = uiState.passwordError,
                accionEtiqueta = {
                    TextButton(
                        onClick = onOlvidoPin,
                        contentPadding = ButtonDefaults.TextButtonContentPadding,
                    ) {
                        Text(
                            "¿Olvidó PIN?",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                },
                trailingIcon = {
                    IconButton(onClick = onTogglePasswordVisible) {
                        Icon(
                            if (uiState.passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (uiState.passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                        )
                    }
                },
                visualTransformation = if (uiState.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    onIngresar()
                }),
                modifier = Modifier.focusRequester(pinFocus),
            )

            SelectorCentro(centro = uiState.centro, onCentroChange = onCentroChange)

            TarjetaModoOffline(activo = uiState.modoOffline, onCambio = onModoOfflineChange)

            BotonIngresar(cargando = uiState.cargando) {
                focusManager.clearFocus()
                onIngresar()
            }
        }
    }
}

/** Resumen del perfil elegido en el paso 1, con opción de volver a elegir. */
@Composable
private fun PerfilSeleccionado(rol: Rol, habilitado: Boolean, onCambiar: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(iconoRol(rol), contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Perfil Operativo", style = MaterialTheme.typography.labelSmall)
                Text(rol.etiqueta, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = onCambiar, enabled = habilitado) {
                Text("Cambiar", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun Encabezado() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
        ) {
            Image(
                painter = painterResource(R.drawable.logo_musselapp),
                contentDescription = "Logo MusselApp",
                modifier = Modifier
                    .padding(12.dp)
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp)),
            )
        }
        Text(
            "MusselApp",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun EtiquetaConIcono(texto: String, icono: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(texto, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun campoColores() = TextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    errorContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    errorIndicatorColor = MaterialTheme.colorScheme.error,
    focusedLeadingIconColor = MaterialTheme.colorScheme.secondary,
    unfocusedLeadingIconColor = MaterialTheme.colorScheme.secondary,
    focusedTrailingIconColor = MaterialTheme.colorScheme.secondary,
    unfocusedTrailingIconColor = MaterialTheme.colorScheme.secondary,
    unfocusedPlaceholderColor = MaterialTheme.colorScheme.outline,
    focusedPlaceholderColor = MaterialTheme.colorScheme.outline,
)

@Composable
private fun CampoFormulario(
    etiqueta: String,
    valor: String,
    onValorChange: (String) -> Unit,
    icono: ImageVector,
    placeholder: String,
    error: String?,
    modifier: Modifier = Modifier,
    accionEtiqueta: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(etiqueta, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            accionEtiqueta?.invoke()
        }
        TextField(
            value = valor,
            onValueChange = onValorChange,
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            textStyle = MaterialTheme.typography.titleMedium,
            placeholder = { Text(placeholder) },
            leadingIcon = { Icon(icono, contentDescription = null) },
            trailingIcon = trailingIcon,
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = campoColores(),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorCentro(centro: CentroCultivo, onCentroChange: (CentroCultivo) -> Unit) {
    var expandido by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        EtiquetaConIcono("Centro de Cultivo Asignado", Icons.Filled.Anchor)
        ExposedDropdownMenuBox(expanded = expandido, onExpandedChange = { expandido = it }) {
            TextField(
                value = centro.nombre,
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                textStyle = MaterialTheme.typography.titleSmall,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
                shape = RoundedCornerShape(8.dp),
                colors = campoColores(),
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
            )
            ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
                CentroCultivo.disponibles.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion.nombre, maxLines = 2, overflow = TextOverflow.Ellipsis) },
                        onClick = {
                            onCentroChange(opcion)
                            expandido = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaModoOffline(activo: Boolean, onCambio: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(Icons.Filled.CloudSync, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Modo Offline Terreno", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                "Permite registrar cosechas y biometrías sin señal celular en pontón o bote de apoyo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
        Spacer(Modifier.width(8.dp))
        Switch(
            checked = activo,
            onCheckedChange = onCambio,
            colors = SwitchDefaults.colors(
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
            ),
        )
    }
}

@Composable
private fun BotonIngresar(cargando: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !cargando,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.secondary,
            disabledContentColor = MaterialTheme.colorScheme.onSecondary,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp)),
    ) {
        if (cargando) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onSecondary,
            )
            Spacer(Modifier.width(12.dp))
            Text("VERIFICANDO CREDENCIAL...", fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        } else {
            Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text("INGRESAR AL SISTEMA", fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
            Spacer(Modifier.width(12.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun PasoPerfilPreview() {
    MusselAppTheme {
        LoginScreen(
            uiState = LoginUiState(),
            onRolChange = {}, onCambiarPerfil = {}, onRutChange = {}, onRutFocusLost = {},
            onPasswordChange = {}, onTogglePasswordVisible = {}, onCentroChange = {},
            onModoOfflineChange = {}, onIngresar = {}, onMensajeMostrado = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun PasoCredencialesPreview() {
    MusselAppTheme {
        LoginScreen(
            uiState = LoginUiState(rol = Rol.OPERARIO, rut = "11.111.111-1", password = "1234"),
            onRolChange = {}, onCambiarPerfil = {}, onRutChange = {}, onRutFocusLost = {},
            onPasswordChange = {}, onTogglePasswordVisible = {}, onCentroChange = {},
            onModoOfflineChange = {}, onIngresar = {}, onMensajeMostrado = {},
        )
    }
}
