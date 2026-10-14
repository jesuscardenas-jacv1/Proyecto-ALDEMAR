package cl.aldemar.musselapp.ui.revision

import cl.aldemar.musselapp.data.model.EstadoMuestra
import cl.aldemar.musselapp.data.model.Muestra

data class RevisionUiState(
    val cargando: Boolean = true,
    val guardando: Boolean = false,
    val muestrasPendientes: List<Muestra> = emptyList(),
    val muestraSeleccionadaId: Long? = null,
    val estadoElegido: EstadoMuestra? = null,
    val comentarioTecnico: String = "",
    val errorComentario: String? = null,
    val mensajeToast: String? = null,
    val modalZoomAbierto: Boolean = false,
) {
    val muestraActual: Muestra?
        get() = muestrasPendientes.find { it.id == muestraSeleccionadaId }
            ?: muestrasPendientes.firstOrNull()
}
