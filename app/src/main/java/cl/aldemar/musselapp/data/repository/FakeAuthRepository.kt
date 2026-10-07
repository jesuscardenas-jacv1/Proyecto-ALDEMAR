package cl.aldemar.musselapp.data.repository

import cl.aldemar.musselapp.data.model.Rol
import cl.aldemar.musselapp.data.model.Usuario
import cl.aldemar.musselapp.util.RutValidator
import kotlinx.coroutines.delay

/**
 * Repositorio con credenciales ficticias para el MVP (caso académico, sin backend).
 *
 * Usuarios de prueba:
 * - Operario Terreno:   RUT 11.111.111-1 · PIN 1234
 * - Supervisor Calidad: RUT 22.222.222-2 · PIN 1234
 */
class FakeAuthRepository(
    private val latenciaMs: Long = 1200,
) : AuthRepository {

    private data class Credencial(val password: String, val usuario: Usuario)

    private val credenciales = mapOf(
        "111111111" to Credencial("1234", Usuario("11.111.111-1", "Operario de Prueba", Rol.OPERARIO)),
        "222222222" to Credencial("1234", Usuario("22.222.222-2", "Supervisor de Prueba", Rol.SUPERVISOR)),
    )

    override suspend fun login(rut: String, password: String, rol: Rol): Result<Usuario> {
        delay(latenciaMs) // Simula la verificación de la credencial
        val credencial = credenciales[RutValidator.normalizar(rut)]
            ?: return Result.failure(AuthException("Usuario no registrado"))
        if (credencial.password != password) {
            return Result.failure(AuthException("Contraseña incorrecta"))
        }
        if (credencial.usuario.rol != rol) {
            return Result.failure(AuthException("El usuario no tiene el perfil ${rol.etiqueta}"))
        }
        return Result.success(credencial.usuario)
    }
}
