package cl.aldemar.musselapp.data.model

/** Flujo de revisión del supervisor: Pendiente → Observado → Corregido → Validado. */
enum class EstadoMuestra(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    OBSERVADO("Observado"),
    CORREGIDO("Corregido"),
    VALIDADO("Validado"),
}
