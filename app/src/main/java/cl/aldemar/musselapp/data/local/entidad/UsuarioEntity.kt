package cl.aldemar.musselapp.data.local.entidad

import androidx.room.Entity
import androidx.room.PrimaryKey
import cl.aldemar.musselapp.data.model.Rol

/**
 * Usuario de la app (tabla `usuarios`).
 * El RUT se guarda normalizado (solo dígitos y K): "111111111".
 * La columna `pin` guarda el hash del PIN (PBKDF2, ver HashPin), nunca el PIN en texto plano.
 */
@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey val rut: String,
    val nombre: String,
    val rol: Rol,
    val pin: String,
)
