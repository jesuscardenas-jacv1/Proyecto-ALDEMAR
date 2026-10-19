package cl.aldemar.musselapp.ui.principal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.aldemar.musselapp.MusselApplication
import cl.aldemar.musselapp.data.repository.MuestraRepository
import cl.aldemar.musselapp.util.Fechas
import cl.aldemar.musselapp.util.Textos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PrincipalViewModel(
    private val muestraRepository: MuestraRepository = MusselApplication.contenedor.muestraRepository,
) : ViewModel() {

    private data class EstadoLocal(val sincronizando: Boolean = false, val mensaje: String? = null)

    private val local = MutableStateFlow(EstadoLocal())

    val uiState: StateFlow<PrincipalUiState> =
        combine(muestraRepository.muestras, local) { todas, estado ->
            // Las métricas resumen solo la jornada de hoy; las recientes y las colas
            // de revisión y sincronización incluyen días anteriores
            val deHoy = todas.filter { it.registradaEn >= Fechas.inicioDelDia() }
            PrincipalUiState(
                resumen = ResumenJornada.desde(deHoy, todas),
                recientes = todas.sortedByDescending { it.registradaEn }.take(CANTIDAD_RECIENTES),
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
                    mensaje = Textos.resultadoSincronizacion(enviadas),
                )
            }
        }
    }

    fun onMensajeMostrado() = local.update { it.copy(mensaje = null) }

    private companion object {
        const val CANTIDAD_RECIENTES = 3
    }
}
