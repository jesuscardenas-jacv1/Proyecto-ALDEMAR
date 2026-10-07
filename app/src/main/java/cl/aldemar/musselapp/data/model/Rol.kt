package cl.aldemar.musselapp.data.model

/** Perfil operativo con el que ingresa el usuario. */
enum class Rol(val etiqueta: String, val descripcion: String) {
    OPERARIO("Operario Terreno", "Registro de muestras, fotografías y conteo en el centro de cultivo"),
    SUPERVISOR("Supervisor Calidad", "Revisión, observación y validación de las muestras registradas"),
}
