package cl.aldemar.musselapp.ui.revision

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.aldemar.musselapp.MusselApplication
import cl.aldemar.musselapp.data.model.EstadoMuestra
import cl.aldemar.musselapp.data.model.Usuario
import cl.aldemar.musselapp.data.repository.MuestraRepository
import kotlinx.coroutines.CancellationException
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
                // Validado y Corregido son dictámenes finales: la cola tiene solo las pendientes
                val pendientes = todas.filter { it.estado == EstadoMuestra.PENDIENTE }
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

    /**
     * Deja seleccionada la muestra que se abrió desde el Detalle.
     * Si ya no está pendiente, la cola mantiene su selección habitual.
     */
    fun enfocarMuestra(id: Long) {
        _uiState.update { actual ->
            if (actual.muestraSeleccionadaId == id) actual
            else actual.copy(muestraSeleccionadaId = id, estadoElegido = null, comentarioTecnico = "", errorComentario = null)
        }
    }

    fun guardarDictamen(supervisor: Usuario, onExito: () -> Unit = {}) {
        val estadoActual = _uiState.value
        if (estadoActual.guardando) return // evita registrar dos veces el mismo dictamen
        val muestra = estadoActual.muestraActual ?: return
        val nuevoEstado = estadoActual.estadoElegido

        if (nuevoEstado == null) {
            _uiState.update { it.copy(errorComentario = "Debes seleccionar un dictamen: Validar o Corregir") }
            return
        }

        if (nuevoEstado == EstadoMuestra.CORREGIDO && estadoActual.comentarioTecnico.isBlank()) {
            _uiState.update { it.copy(errorComentario = "Debes indicar qué se corrigió en la muestra") }
            return
        }

        // Solo se guarda lo que escribió el supervisor; no se inventa un comentario por él
        val comentario = estadoActual.comentarioTecnico.trim().ifBlank { null }

        _uiState.update { it.copy(guardando = true) }
        viewModelScope.launch {
            try {
                repository.actualizarEstado(
                    id = muestra.id,
                    nuevoEstado = nuevoEstado,
                    comentario = comentario,
                    supervisor = supervisor,
                )
                _uiState.update {
                    it.copy(
                        guardando = false,
                        estadoElegido = null,
                        comentarioTecnico = "",
                        errorComentario = null,
                        mensajeToast = "Dictamen registrado para muestra ${muestra.codigo}",
                    )
                }
                onExito()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Antes un error aquí cerraba la app; ahora se avisa y el dictamen escrito se conserva
                _uiState.update {
                    it.copy(
                        guardando = false,
                        mensajeToast = "No se pudo guardar el dictamen: ${e.message ?: "error de la base de datos"}",
                    )
                }
            }
        }
    }

    fun limpiarMensaje() {
        _uiState.update { it.copy(mensajeToast = null) }
    }
}
