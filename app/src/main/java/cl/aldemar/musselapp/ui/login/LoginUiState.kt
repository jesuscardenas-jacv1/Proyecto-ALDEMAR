package cl.aldemar.musselapp.ui.login

import cl.aldemar.musselapp.data.model.CentroCultivo
import cl.aldemar.musselapp.data.model.Rol
import cl.aldemar.musselapp.data.model.Usuario

/** Estado completo de la pantalla de Login. */
data class LoginUiState(
    val rol: Rol = Rol.OPERARIO,
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
