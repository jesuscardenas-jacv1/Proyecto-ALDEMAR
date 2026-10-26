package cl.aldemar.musselapp.ui.muestra

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.aldemar.musselapp.MusselApplication
import cl.aldemar.musselapp.data.local.dao.CatalogoDao
import cl.aldemar.musselapp.data.model.CentroCultivo
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Lógica del formulario Nueva Muestra.
 * La Screen solo muestra los datos; las reglas de validación viven aquí.
 */
class MuestraViewModel(
    private val catalogoDao: CatalogoDao? = runCatching { MusselApplication.contenedor.database.catalogoDao() }.getOrNull(),
) : ViewModel() {

    // _uiState se puede editar dentro del ViewModel
    private val _uiState = MutableStateFlow(MuestraUiState())

    // uiState es de solo lectura para la Screen
    val uiState: StateFlow<MuestraUiState> = _uiState.asStateFlow()

    private var jobTrenes: Job? = null
    private var jobLineas: Job? = null

    /** Deja elegido el centro con el que inició sesión, si aún no hay uno. */
    fun iniciar(centroInicial: CentroCultivo?) {
        if (_uiState.value.centro == null && centroInicial != null) {
            cambiarCentro(centroInicial)
        }
    }

    fun cambiarCentro(centro: CentroCultivo) {
        _uiState.update {
            it.copy(
                centro = centro,
                tren = null,
                linea = null,
                lineasDisponibles = emptyList(),
                errorCentro = null,
                mensaje = null,
            )
        }
        cargarTrenes(centro.id)
    }

    private fun cargarTrenes(centroId: String) {
        jobTrenes?.cancel()
        val dao = catalogoDao
        if (dao == null) {
            _uiState.update { it.copy(trenesDisponibles = Trenes.map { t -> t.first }) }
            return
        }
        jobTrenes = viewModelScope.launch {
            dao.observarCodigosTren(centroId).collect { codigos ->
                val disponibles = codigos.ifEmpty { Trenes.map { it.first } }
                _uiState.update { it.copy(trenesDisponibles = disponibles) }
            }
        }
    }

    fun cambiarTren(codigo: String) {
        _uiState.update {
            it.copy(
                tren = codigo,
                linea = null,
                errorTren = null,
                mensaje = null,
            )
        }
        val centroId = _uiState.value.centro?.id
        if (centroId != null) {
            cargarLineas(centroId, codigo)
        } else {
            _uiState.update { it.copy(lineasDisponibles = Lineas) }
        }
    }

    private fun cargarLineas(centroId: String, trenCodigo: String) {
        jobLineas?.cancel()
        val dao = catalogoDao
        if (dao == null) {
            _uiState.update { it.copy(lineasDisponibles = Lineas) }
            return
        }
        jobLineas = viewModelScope.launch {
            dao.observarNumerosLinea(centroId, trenCodigo).collect { numeros ->
                val disponibles = numeros.ifEmpty { Lineas }
                _uiState.update { it.copy(lineasDisponibles = disponibles) }
            }
        }
    }

    fun cambiarLinea(linea: Int) {
        _uiState.update { it.copy(linea = linea, errorLinea = null, mensaje = null) }
    }

    fun cambiarEstrato(profundidadM: Int) {
        _uiState.update { it.copy(profundidadM = profundidadM, errorEstrato = null, mensaje = null) }
    }

    fun guardar() {
        val actual = _uiState.value

        val trenesValidos = actual.trenesDisponibles.ifEmpty { Trenes.map { it.first } }
        val lineasValidas = actual.lineasDisponibles.ifEmpty { Lineas }

        // Reglas de negocio: los cuatro parámetros son obligatorios
        // y deben ser valores válidos del catálogo del centro
        val centroInvalido = actual.centro == null
        val trenInvalido = actual.tren == null || actual.tren !in trenesValidos
        val lineaInvalida = actual.linea == null || actual.linea !in lineasValidas
        val estratoInvalido = actual.profundidadM == null || actual.profundidadM !in 0..6

        if (centroInvalido || trenInvalido || lineaInvalida || estratoInvalido) {
            _uiState.update {
                it.copy(
                    errorCentro = if (centroInvalido) "Seleccione el centro de cultivo" else null,
                    errorTren = if (trenInvalido) "Seleccione el tren de cultivo" else null,
                    errorLinea = if (lineaInvalida) "Seleccione la línea de siembra" else null,
                    errorEstrato = if (estratoInvalido) "Seleccione el estrato de profundidad" else null,
                    mensaje = null,
                    parametrosValidos = null,
                )
            }
            return
        }

        // Si todo está bien, se arma el resultado y se confirma
        _uiState.update {
            it.copy(
                errorCentro = null,
                errorTren = null,
                errorLinea = null,
                errorEstrato = null,
                mensaje = "Parámetros registrados correctamente",
                parametrosValidos = ParametrosMuestra(
                    centroId = actual.centro!!.id,
                    tren = actual.tren!!,
                    linea = actual.linea!!,
                    profundidadM = actual.profundidadM!!,
                ),
            )
        }
    }

    /** La Screen avisa que ya navegó, para no volver a avanzar al regresar. */
    fun navegacionRealizada() {
        _uiState.update { it.copy(parametrosValidos = null) }
    }

    fun limpiar() {
        jobTrenes?.cancel()
        jobLineas?.cancel()
        // Todos los campos vacíos y sin mensajes
        _uiState.value = MuestraUiState()
    }
}
