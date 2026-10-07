package cl.aldemar.musselapp.ui.historial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.aldemar.musselapp.data.repository.FakeMuestraRepository
import cl.aldemar.musselapp.data.repository.MuestraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HistorialViewModel(
    private val muestraRepository: MuestraRepository = FakeMuestraRepository.instancia,
) : ViewModel() {

    private data class EstadoLocal(
        val criterios: CriteriosHistorial = CriteriosHistorial(),
        val sincronizando: Boolean = false,
        val mensaje: String? = null,
    )

    private val local = MutableStateFlow(EstadoLocal())

    val uiState: StateFlow<HistorialUiState> =
        combine(muestraRepository.muestras, local) { todas, estado ->
            val resultado = FiltrosHistorial.aplicar(todas, estado.criterios)
            HistorialUiState(
                criterios = estado.criterios,
                muestras = resultado.muestras,
                conteos = resultado.conteos,
                tallaPromedioMm = resultado.tallaPromedioMm,
                pendientesSincronizar = todas.count { !it.sincronizada },
                sincronizando = estado.sincronizando,
                mensaje = estado.mensaje,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistorialUiState())

    private fun criterios(cambio: (CriteriosHistorial) -> CriteriosHistorial) =
        local.update { it.copy(criterios = cambio(it.criterios)) }

    fun onBusquedaChange(texto: String) = criterios { it.copy(busqueda = texto) }
    fun onFiltroChange(filtro: FiltroEstado) = criterios { it.copy(filtro = filtro) }
    fun onPeriodoChange(periodo: Periodo) = criterios { it.copy(periodo = periodo) }
    fun onAlternarOrden() = criterios {
        it.copy(orden = if (it.orden == Orden.RECIENTES) Orden.ANTIGUAS else Orden.RECIENTES)
    }
    fun onRestablecerFiltros() = criterios { CriteriosHistorial() }

    fun sincronizar() {
        if (local.value.sincronizando) return
        local.update { it.copy(sincronizando = true) }
        viewModelScope.launch {
            val enviadas = muestraRepository.sincronizar()
            local.update {
                it.copy(
                    sincronizando = false,
                    mensaje = if (enviadas == 0) "Todo estaba sincronizado" else "$enviadas muestras sincronizadas",
                )
            }
        }
    }

    fun onMensajeMostrado() = local.update { it.copy(mensaje = null) }
}
