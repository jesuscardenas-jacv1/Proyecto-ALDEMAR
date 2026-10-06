package cl.aldemar.musselapp.data.repository

import cl.aldemar.musselapp.data.model.Rol
import cl.aldemar.musselapp.data.model.Usuario

/** Acceso a la autenticación de usuarios. */
interface AuthRepository {
    suspend fun login(rut: String, password: String, rol: Rol): Result<Usuario>
}

/** Error de autenticación con un mensaje listo para mostrar en pantalla. */
class AuthException(message: String) : Exception(message)
