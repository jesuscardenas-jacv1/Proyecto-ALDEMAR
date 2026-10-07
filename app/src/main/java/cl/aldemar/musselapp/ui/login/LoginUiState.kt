package cl.aldemar.musselapp.ui.login

import cl.aldemar.musselapp.data.model.CentroCultivo
import cl.aldemar.musselapp.data.model.Rol
import cl.aldemar.musselapp.data.model.Usuario

/**
 * Estado completo de la pantalla de Login.
 * Mientras [rol] es null se muestra el paso de selección de perfil;
 * con un perfil elegido se muestra el ingreso de credenciales.
 */
data class LoginUiState(
    val rol: Rol? = null,
    val rut: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val centro: CentroCultivo = CentroCultivo.disponibles.first(),
    val modoOffline: Boolean = true,
    val rutError: String? = null,
    val passwordError: String? = null,
    val cargando: Boolean = false,
    val mensajeError: String? = null,
    val usuario: Usuario? = null,
)
