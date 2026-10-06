package cl.aldemar.musselapp.data.model

/** Perfil operativo con el que ingresa el usuario. */
enum class Rol(val etiqueta: String) {
    OPERARIO("Operario Terreno"),
    SUPERVISOR("Supervisor Calidad"),
}
