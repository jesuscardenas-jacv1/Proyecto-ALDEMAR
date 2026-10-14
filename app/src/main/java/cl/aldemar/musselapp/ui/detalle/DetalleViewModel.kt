package cl.aldemar.musselapp.ui.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.aldemar.musselapp.MusselApplication
import cl.aldemar.musselapp.data.repository.MuestraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetalleViewModel(
    private val muestraId: Long,
    private val repository: MuestraRepository = MusselApplication.contenedor.muestraRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleUiState())
    val uiState: StateFlow<DetalleUiState> = _uiState.asStateFlow()

    init {
        cargarMuestra()
    }

    private fun cargarMuestra() {
        viewModelScope.launch {
            repository.obtenerPorId(muestraId).collect { muestraEncontrada ->
                _uiState.update {
                    it.copy(
                        cargando = false,
                        muestra = muestraEncontrada,
                    )
                }
            }
        }
    }

    fun alternarModalZoom(abierto: Boolean) {
        _uiState.update { it.copy(modalZoomAbierto = abierto) }
    }

    fun mostrarMensaje(mensaje: String) {
        _uiState.update { it.copy(mensajeToast = mensaje) }
    }

    fun limpiarMensaje() {
        _uiState.update { it.copy(mensajeToast = null) }
    }

    class Factory(
        private val muestraId: Long,
        private val repository: MuestraRepository = MusselApplication.contenedor.muestraRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DetalleViewModel(muestraId, repository) as T
        }
    }
}
