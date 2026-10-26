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

/**
 * Autenticación contra la tabla `usuarios` de la base Room.
 * [bloqueo] limita los intentos de PIN por RUT para que no se pueda adivinar probando.
 */
class RoomAuthRepository(
    private val usuarioDao: UsuarioDao,
    private val latenciaMs: Long = 800,
    private val bloqueo: BloqueoLogin = BloqueoLogin(IntentosEnMemoria()),
) : AuthRepository {

    override suspend fun login(rut: String, password: String, rol: Rol): Result<Usuario> {
        val rutNormalizado = RutValidator.normalizar(rut)
        bloqueo.msRestantes(rutNormalizado).takeIf { it > 0 }?.let { espera ->
            return Result.failure(AuthException(mensajeBloqueo(espera)))
        }
        delay(latenciaMs) // Simula la verificación de la credencial
        val usuario = usuarioDao.buscarPorRut(rutNormalizado)
        // La base guarda el hash del PIN; el cálculo es costoso a propósito y va fuera del hilo principal
        val pinCorrecto = usuario != null &&
            withContext(Dispatchers.Default) { HashPin.verificar(password, usuario.pin) }
        if (usuario == null || !pinCorrecto) {
            // Mismo mensaje si el RUT no existe o el PIN es incorrecto: no revela qué RUT están registrados
            val bloqueoMs = bloqueo.registrarFallo(rutNormalizado)
            val mensaje = when {
                bloqueoMs > 0 -> mensajeBloqueo(bloqueoMs)
                bloqueo.intentosRestantes(rutNormalizado) <= AVISO_INTENTOS -> {
                    val quedan = bloqueo.intentosRestantes(rutNormalizado)
                    if (quedan == 1) "RUT o PIN incorrectos. Te queda 1 intento"
                    else "RUT o PIN incorrectos. Te quedan $quedan intentos"
                }
                else -> "RUT o PIN incorrectos"
            }
            return Result.failure(AuthException(mensaje))
        }
        bloqueo.registrarExito(rutNormalizado)
        if (usuario.rol != rol) {
            return Result.failure(AuthException("El usuario no tiene el perfil ${rol.etiqueta}"))
        }
        return Result.success(usuario.aModelo())
    }

    private companion object {
        /** Desde cuántos intentos restantes se avisa al usuario. */
        const val AVISO_INTENTOS = 2

        fun mensajeBloqueo(espera: Long): String {
            val segundos = (espera + 999) / 1000
            val tiempo = if (segundos < 60) "$segundos s" else "${(segundos + 59) / 60} min"
            return "Demasiados intentos fallidos. Intenta de nuevo en $tiempo"
        }
    }
}
