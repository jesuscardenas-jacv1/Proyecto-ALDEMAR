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

    private data class EstadoLocal(
        val sincronizando: Boolean = false,
        val mensaje: String? = null,
        /** Centro con el que se inició sesión; null resume todos los centros. */
        val centroId: String? = null,
    )

    private val local = MutableStateFlow(EstadoLocal())

    val uiState: StateFlow<PrincipalUiState> =
        combine(muestraRepository.muestras, local) { todas, estado ->
            // Todo se resume para el centro de la sesión, salvo la cola de sincronización:
            // "Sincronizar" envía las muestras de todos los centros guardadas en el teléfono
            val delCentro = estado.centroId?.let { id -> todas.filter { it.centroId == id } } ?: todas
            // Las métricas resumen solo la jornada de hoy; las recientes y la cola
            // de revisión incluyen días anteriores
            val deHoy = delCentro.filter { it.registradaEn >= Fechas.inicioDelDia() }
            PrincipalUiState(
                resumen = ResumenJornada.desde(deHoy, delCentro)
                    .copy(pendientesSincronizar = todas.count { !it.sincronizada }),
                recientes = delCentro.sortedByDescending { it.registradaEn }.take(CANTIDAD_RECIENTES),
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

    /** Resume solo las muestras del centro con el que se inició sesión. */
    fun mostrarCentro(centroId: String) = local.update { it.copy(centroId = centroId) }

    private companion object {
        const val CANTIDAD_RECIENTES = 3
    }
}
