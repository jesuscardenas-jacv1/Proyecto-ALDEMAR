package cl.aldemar.musselapp.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.aldemar.musselapp.data.model.CentroCultivo
import cl.aldemar.musselapp.data.model.Rol
import cl.aldemar.musselapp.data.repository.AuthRepository
import cl.aldemar.musselapp.data.repository.FakeAuthRepository
import cl.aldemar.musselapp.util.RutValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository = FakeAuthRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /** Paso 1: elige el perfil y avanza al ingreso de credenciales. */
    fun onRolChange(rol: Rol) = _uiState.update { it.copy(rol = rol, mensajeError = null) }

    /** Vuelve al paso 1 descartando el PIN y los errores. */
    fun onCambiarPerfil() = _uiState.update {
        it.copy(rol = null, password = "", passwordVisible = false, rutError = null, passwordError = null, mensajeError = null)
    }

    fun onRutChange(rut: String) =
        _uiState.update { it.copy(rut = rut, rutError = null, mensajeError = null) }

    /** Al salir del campo, deja el RUT con puntos y guion. */
    fun onRutFocusLost() = _uiState.update {
        if (RutValidator.esValido(it.rut)) it.copy(rut = RutValidator.formatear(it.rut)) else it
    }

    fun onPasswordChange(password: String) =
        _uiState.update { it.copy(password = password, passwordError = null, mensajeError = null) }

    fun onTogglePasswordVisible() = _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }

    fun onCentroChange(centro: CentroCultivo) = _uiState.update { it.copy(centro = centro) }

    fun onModoOfflineChange(activo: Boolean) = _uiState.update { it.copy(modoOffline = activo) }

    fun onMensajeMostrado() = _uiState.update { it.copy(mensajeError = null) }

    fun onCerrarSesion() = _uiState.update { LoginUiState(centro = it.centro) }

    fun ingresar() {
        val estado = _uiState.value
        val rol = estado.rol ?: return
        if (estado.cargando) return

        val rutError = when {
            estado.rut.isBlank() -> "Ingrese su RUT o código de operario"
            !RutValidator.esValido(estado.rut) -> "RUT inválido"
            else -> null
        }
        val passwordError = if (estado.password.isBlank()) "Ingrese su contraseña" else null
        if (rutError != null || passwordError != null) {
            _uiState.update { it.copy(rutError = rutError, passwordError = passwordError) }
            return
        }

        _uiState.update { it.copy(cargando = true, mensajeError = null) }
        viewModelScope.launch {
            authRepository.login(estado.rut, estado.password, rol)
                .onSuccess { usuario ->
                    _uiState.update { it.copy(cargando = false, usuario = usuario, password = "") }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(cargando = false, mensajeError = error.message ?: "No se pudo ingresar")
                    }
                }
        }
    }
}
