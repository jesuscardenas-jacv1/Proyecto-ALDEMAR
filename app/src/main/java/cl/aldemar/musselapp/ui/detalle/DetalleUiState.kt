package cl.aldemar.musselapp.ui.detalle

import cl.aldemar.musselapp.data.model.Muestra

data class DetalleUiState(
    val cargando: Boolean = true,
    val muestra: Muestra? = null,
    val mensajeToast: String? = null,
    val modalZoomAbierto: Boolean = false,
)
