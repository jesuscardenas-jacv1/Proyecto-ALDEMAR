package cl.aldemar.musselapp.data.model

/** Flujo de revisión: la muestra parte Pendiente y el supervisor la Valida o la Corrige (los dos son finales). */
enum class EstadoMuestra(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    CORREGIDO("Corregido"),
    VALIDADO("Validado"),
}
