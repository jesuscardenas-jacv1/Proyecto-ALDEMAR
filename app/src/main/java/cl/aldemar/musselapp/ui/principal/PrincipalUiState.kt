package cl.aldemar.musselapp.ui.principal

import cl.aldemar.musselapp.data.model.Muestra

/** Estado de la Pantalla Principal (Dashboard). */
data class PrincipalUiState(
    val resumen: ResumenJornada = ResumenJornada.desde(emptyList()),
    val recientes: List<Muestra> = emptyList(),
    val sincronizando: Boolean = false,
    val mensaje: String? = null,
)
