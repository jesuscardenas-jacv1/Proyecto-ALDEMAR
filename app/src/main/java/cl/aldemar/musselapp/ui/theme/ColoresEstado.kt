package cl.aldemar.musselapp.ui.theme

import androidx.compose.ui.graphics.Color
import cl.aldemar.musselapp.data.model.EstadoMuestra

/**
 * Colores de un estado de muestra. Todas las pantallas los toman de aquí
 * para que un mismo estado se vea igual en Principal, Historial, Detalle y Revisión.
 */
data class ColoresEstado(
    /** Fondo suave de chips y avisos. */
    val fondo: Color,
    /** Texto e íconos sobre [fondo]. */
    val texto: Color,
    /** Franjas y bordes de tarjetas. */
    val acento: Color,
)

val EstadoMuestra.colores: ColoresEstado
    get() = when (this) {
        EstadoMuestra.VALIDADO -> ColoresEstado(EstadoValidadoFondo, EstadoValidadoTexto, AcentoValidado)
        EstadoMuestra.OBSERVADO -> ColoresEstado(EstadoObservadoFondo, EstadoObservadoTexto, AcentoObservado)
        // Los dos esperan revisión: mismo color, distinto ícono y etiqueta
        EstadoMuestra.PENDIENTE, EstadoMuestra.CORREGIDO ->
            ColoresEstado(EstadoEnEsperaFondo, EstadoEnEsperaTexto, AcentoPendiente)
    }
