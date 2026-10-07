package cl.aldemar.musselapp.ui.historial

import cl.aldemar.musselapp.data.model.Muestra

/** Estado de la pantalla Historial. */
data class HistorialUiState(
    val criterios: CriteriosHistorial = CriteriosHistorial(),
    val muestras: List<Muestra> = emptyList(),
    val conteos: Map<FiltroEstado, Int> = emptyMap(),
    val tallaPromedioMm: Double? = null,
    val pendientesSincronizar: Int = 0,
    val actualizadoEn: Long = System.currentTimeMillis(),
    val sincronizando: Boolean = false,
    val mensaje: String? = null,
)
