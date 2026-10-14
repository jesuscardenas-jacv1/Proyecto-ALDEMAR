package cl.aldemar.musselapp.ui.revision

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.aldemar.musselapp.MusselApplication
import cl.aldemar.musselapp.data.model.EstadoMuestra
import cl.aldemar.musselapp.data.repository.MuestraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RevisionViewModel(
    private val repository: MuestraRepository = MusselApplication.contenedor.muestraRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RevisionUiState())
    val uiState: StateFlow<RevisionUiState> = _uiState.asStateFlow()

    init {
        observarMuestras()
    }

    private fun observarMuestras() {
        viewModelScope.launch {
            repository.muestras.collect { todas ->
                val pendientes = todas.filter { it.estado == EstadoMuestra.PENDIENTE || it.estado == EstadoMuestra.OBSERVADO }
                _uiState.update { estadoActual ->
                    val actualValido = pendientes.any { it.id == estadoActual.muestraSeleccionadaId }
                    val nuevaSeleccionada = if (actualValido) estadoActual.muestraSeleccionadaId else pendientes.firstOrNull()?.id
                    estadoActual.copy(
                        cargando = false,
                        muestrasPendientes = pendientes,
                        muestraSeleccionadaId = nuevaSeleccionada,
                    )
                }
            }
        }
    }

    fun seleccionarMuestra(id: Long) {
        _uiState.update {
            it.copy(
                muestraSeleccionadaId = id,
                estadoElegido = null,
                comentarioTecnico = "",
                errorComentario = null,
            )
        }
    }

    fun elegirEstado(estado: EstadoMuestra) {
        _uiState.update { it.copy(estadoElegido = estado, errorComentario = null) }
    }

    fun actualizarComentario(texto: String) {
        _uiState.update { it.copy(comentarioTecnico = texto, errorComentario = null) }
    }

    fun agregarMotivoRapido(motivo: String) {
        _uiState.update { actual ->
            val nuevo = if (actual.comentarioTecnico.isBlank()) motivo else "${actual.comentarioTecnico}. $motivo"
            actual.copy(comentarioTecnico = nuevo, errorComentario = null)
        }
    }

    fun alternarModalZoom(abierto: Boolean) {
        _uiState.update { it.copy(modalZoomAbierto = abierto) }
    }

    fun guardarDictamen(supervisorNombre: String, onExito: () -> Unit = {}) {
        val estadoActual = _uiState.value
        val muestra = estadoActual.muestraActual ?: return
        val nuevoEstado = estadoActual.estadoElegido

        if (nuevoEstado == null) {
            _uiState.update { it.copy(errorComentario = "Debes seleccionar un dictamen: Validar u Observar") }
            return
        }

        if (nuevoEstado == EstadoMuestra.OBSERVADO && estadoActual.comentarioTecnico.isBlank()) {
            _uiState.update { it.copy(errorComentario = "Debes ingresar una justificación técnica para observar la muestra") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true) }
            val comentarioFinal = if (estadoActual.comentarioTecnico.isNotBlank()) {
                estadoActual.comentarioTecnico
            } else if (nuevoEstado == EstadoMuestra.VALIDADO) {
                "Muestra validada sin anomalías biométricas."
            } else null

            repository.actualizarEstado(
                id = muestra.id,
                nuevoEstado = nuevoEstado,
                comentario = comentarioFinal,
                revisadaPor = supervisorNombre,
            )

            _uiState.update {
                it.copy(
                    guardando = false,
                    estadoElegido = null,
                    comentarioTecnico = "",
                    errorComentario = null,
                    mensajeToast = "Dictamen registrado para muestra #${muestra.codigo}",
                )
            }
            onExito()
        }
    }

    fun limpiarMensaje() {
        _uiState.update { it.copy(mensajeToast = null) }
    }
}
