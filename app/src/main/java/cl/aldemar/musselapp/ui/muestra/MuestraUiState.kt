package cl.aldemar.musselapp.ui.muestra

import cl.aldemar.musselapp.data.model.CentroCultivo

/**
 * Estado del formulario Nueva Muestra (paso 1: Parámetros).
 * Reúne los valores elegidos, el error de cada campo y el mensaje de confirmación,
 * igual que FormUiState de la Clase 7.
 */
data class MuestraUiState(
    val centro: CentroCultivo? = null,
    val tren: String? = null, // código del tren: "A", "B" o "C"
    val linea: Int? = null,
    val profundidadM: Int? = null,
    val errorCentro: String? = null,
    val errorTren: String? = null,
    val errorLinea: String? = null,
    val errorEstrato: String? = null,
    val mensaje: String? = null,
    // Se llena solo cuando todo es válido; la Screen lo usa para avanzar a Captura
    val parametrosValidos: ParametrosMuestra? = null,
)
