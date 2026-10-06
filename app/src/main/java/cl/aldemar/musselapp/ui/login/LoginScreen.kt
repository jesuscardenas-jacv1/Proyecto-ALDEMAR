package cl.aldemar.musselapp.ui.login

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
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Anchor
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCodeScanner
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
import androidx.compose.material3.FilledTonalButton
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

/** UI sin estado del Login (diseño "1. Login - MusselApp" de Stitch). */
@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onRolChange: (Rol) -> Unit,
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
    val focusManager = LocalFocusManager.current
    val pinFocus = remember { FocusRequester() }

    LaunchedEffect(uiState.mensajeError) {
        uiState.mensajeError?.let {
            snackbarHostState.showSnackbar(it)
            onMensajeMostrado()
        }
    }
    fun avisar(mensaje: String) {
        scope.launch { snackbarHostState.showSnackbar(mensaje) }
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
                SelectorRol(rolSeleccionado = uiState.rol, onRolChange = onRolChange)

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
                                    onClick = { avisar("Solicite el restablecimiento del PIN a su supervisor") },
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

                TarjetaCredencialQr(onActivar = { avisar("Lectura de credencial RFID / QR disponible próximamente") })
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
private fun SelectorRol(rolSeleccionado: Rol, onRolChange: (Rol) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        EtiquetaConIcono("Perfil Operativo", Icons.Filled.Badge)
        Row(
            modifier = Modifier.selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Rol.entries.forEach { rol ->
                val seleccionado = rol == rolSeleccionado
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (seleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = if (seleccionado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 72.dp)
                        .selectable(selected = seleccionado, role = Role.RadioButton, onClick = { onRolChange(rol) }),
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            if (rol == Rol.OPERARIO) Icons.Filled.Sailing else Icons.Filled.FactCheck,
                            contentDescription = null,
                            tint = if (seleccionado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            rol.etiqueta.replace(" ", "\n"),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp,
                        )
                    }
                }
            }
        }
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

@Composable
private fun TarjetaCredencialQr(onActivar: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.QrCodeScanner, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Credencial RFID / QR", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    "Escaneo directo con guantes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            FilledTonalButton(onClick = onActivar, shape = RoundedCornerShape(8.dp)) {
                Text("ACTIVAR", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun LoginScreenPreview() {
    MusselAppTheme {
        LoginScreen(
            uiState = LoginUiState(rut = "11.111.111-1", password = "1234"),
            onRolChange = {},
            onRutChange = {},
            onRutFocusLost = {},
            onPasswordChange = {},
            onTogglePasswordVisible = {},
            onCentroChange = {},
            onModoOfflineChange = {},
            onIngresar = {},
            onMensajeMostrado = {},
        )
    }
}
