package cl.aldemar.musselapp.data.model

/** Muestra de choritos extraída de un tramo de cuerda. */
data class Muestra(
    val id: Long,
    val centroId: String,
    val linea: Int,
    val tren: String,
    val profundidadM: Int,
    val tallaMm: Int,
    val conteoIndM: Int,
    val observacion: String,
    val estado: EstadoMuestra,
    val registradaEn: Long,
    val sincronizada: Boolean,
    val operador: String = "",
    /** Supervisor que revisó la muestra (Validado / Observado). */
    val revisadaPor: String? = null,
    /** Comentario técnico del supervisor al observar o validar la muestra. */
    val comentarioSupervisor: String? = null,
) {
    val codigoLinea: String get() = "L%02d".format(linea)

    /** Código de trazabilidad, por ejemplo "MST-010". */
    val codigo: String get() = "MST-%03d".format(id)

    companion object {
        /** Densidad mínima esperada; bajo este valor la muestra requiere reconteo. */
        const val DENSIDAD_MINIMA_IND_M = 350
    }
}
