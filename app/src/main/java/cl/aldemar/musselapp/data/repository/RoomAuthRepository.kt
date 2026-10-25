package cl.aldemar.musselapp.data.repository

import cl.aldemar.musselapp.data.local.dao.UsuarioDao
import cl.aldemar.musselapp.data.local.aModelo
import cl.aldemar.musselapp.data.model.Rol
import cl.aldemar.musselapp.data.model.Usuario
import cl.aldemar.musselapp.util.HashPin
import cl.aldemar.musselapp.util.RutValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/** Autenticación contra la tabla `usuarios` de la base Room. */
class RoomAuthRepository(
    private val usuarioDao: UsuarioDao,
    private val latenciaMs: Long = 800,
) : AuthRepository {

    override suspend fun login(rut: String, password: String, rol: Rol): Result<Usuario> {
        delay(latenciaMs) // Simula la verificación de la credencial
        val usuario = usuarioDao.buscarPorRut(RutValidator.normalizar(rut))
            ?: return Result.failure(AuthException("Usuario no registrado"))
        // La base guarda el hash del PIN; el cálculo es costoso a propósito y va fuera del hilo principal
        val pinCorrecto = withContext(Dispatchers.Default) { HashPin.verificar(password, usuario.pin) }
        if (!pinCorrecto) {
            return Result.failure(AuthException("Contraseña incorrecta"))
        }
        if (usuario.rol != rol) {
            return Result.failure(AuthException("El usuario no tiene el perfil ${rol.etiqueta}"))
        }
        return Result.success(usuario.aModelo())
    }
}
