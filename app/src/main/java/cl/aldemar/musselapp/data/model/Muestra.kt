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
    val fotoRuta: String? = null,
) {
    val codigoLinea: String get() = "L%02d".format(linea)

    /** Código de trazabilidad, por ejemplo "MST-010". */
    val codigo: String get() = "MST-%03d".format(id)

    /** Rendimiento estimado en kg por metro. */
    val rendimientoKgM: Double get() = ((conteoIndM * tallaMm * 0.00028 * 10).toInt() / 10.0).coerceAtLeast(0.1)

    /** Porcentaje de desvío respecto a la densidad estándar histórica (380 ind/m). */
    val desvioDensidadPct: Int get() = (((conteoIndM - DENSIDAD_ESTANDAR_IND_M).toDouble() / DENSIDAD_ESTANDAR_IND_M) * 100).toInt()

    companion object {
        /** Densidad mínima esperada; bajo este valor la muestra requiere reconteo. */
        const val DENSIDAD_MINIMA_IND_M = 350
        /** Densidad estándar histórica para comparación en Chiloé. */
        const val DENSIDAD_ESTANDAR_IND_M = 380
    }
}
