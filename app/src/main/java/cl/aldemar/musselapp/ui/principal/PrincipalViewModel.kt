package cl.aldemar.musselapp.ui.principal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.aldemar.musselapp.data.repository.FakeMuestraRepository
import cl.aldemar.musselapp.data.repository.MuestraRepository
import cl.aldemar.musselapp.util.Fechas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PrincipalViewModel(
    private val muestraRepository: MuestraRepository = FakeMuestraRepository.instancia,
) : ViewModel() {

    private data class EstadoLocal(val sincronizando: Boolean = false, val mensaje: String? = null)

    private val local = MutableStateFlow(EstadoLocal())

    val uiState: StateFlow<PrincipalUiState> =
        combine(muestraRepository.muestras, local) { todas, estado ->
            // La Principal resume solo la jornada de hoy; el Historial muestra todo
            val muestras = todas.filter { it.registradaEn >= Fechas.inicioDelDia() }
            PrincipalUiState(
                resumen = ResumenJornada.desde(muestras),
                recientes = muestras.sortedByDescending { it.registradaEn }.take(CANTIDAD_RECIENTES),
                sincronizando = estado.sincronizando,
                mensaje = estado.mensaje,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PrincipalUiState())

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

    private companion object {
        const val CANTIDAD_RECIENTES = 3
    }
}
