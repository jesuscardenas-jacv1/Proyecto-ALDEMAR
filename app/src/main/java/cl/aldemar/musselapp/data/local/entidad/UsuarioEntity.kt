package cl.aldemar.musselapp.data.local.entidad

import androidx.room.Entity
import androidx.room.PrimaryKey
import cl.aldemar.musselapp.data.model.Rol

/**
 * Usuario de la app (tabla `usuarios`).
 * El RUT se guarda normalizado (solo dígitos y K): "111111111".
 * El PIN se guarda en texto plano porque es un MVP académico con datos ficticios.
 */
@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey val rut: String,
    val nombre: String,
    val rol: Rol,
    val pin: String,
)
