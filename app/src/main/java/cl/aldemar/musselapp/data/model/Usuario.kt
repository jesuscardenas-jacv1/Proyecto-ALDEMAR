package cl.aldemar.musselapp.data.model

/** Usuario autenticado en la app. */
data class Usuario(
    val rut: String,
    val nombre: String,
    val rol: Rol,
)
