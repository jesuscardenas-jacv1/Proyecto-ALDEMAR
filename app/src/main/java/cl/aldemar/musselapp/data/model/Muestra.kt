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
) {
    val codigoLinea: String get() = "L%02d".format(linea)
}
