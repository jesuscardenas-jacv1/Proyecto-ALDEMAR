package cl.aldemar.musselapp.ui.muestra

import androidx.lifecycle.ViewModel
import cl.aldemar.musselapp.data.model.CentroCultivo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Lógica del formulario Nueva Muestra.
 * La Screen solo muestra los datos; las reglas de validación viven aquí.
 */
class MuestraViewModel : ViewModel() {

    // _uiState se puede editar dentro del ViewModel
    private val _uiState = MutableStateFlow(MuestraUiState())

    // uiState es de solo lectura para la Screen
    val uiState: StateFlow<MuestraUiState> = _uiState.asStateFlow()

    /** Deja elegido el centro con el que inició sesión, si aún no hay uno. */
    fun iniciar(centroInicial: CentroCultivo?) {
        _uiState.update { if (it.centro == null) it.copy(centro = centroInicial) else it }
    }

    // Al cambiar un campo se borra su error, como en cambiarTitulo de la Clase 7

    fun cambiarCentro(centro: CentroCultivo) {
        _uiState.update { it.copy(centro = centro, errorCentro = null, mensaje = null) }
    }

    fun cambiarTren(codigo: String) {
        _uiState.update { it.copy(tren = codigo, errorTren = null, mensaje = null) }
    }

    fun cambiarLinea(linea: Int) {
        _uiState.update { it.copy(linea = linea, errorLinea = null, mensaje = null) }
    }

    fun cambiarEstrato(profundidadM: Int) {
        _uiState.update { it.copy(profundidadM = profundidadM, errorEstrato = null, mensaje = null) }
    }

    fun guardar() {
        val actual = _uiState.value

        // Reglas de negocio: los cuatro parámetros son obligatorios
        // y deben ser valores de las listas del centro
        val centroInvalido = actual.centro == null
        val trenInvalido = Trenes.none { it.first == actual.tren }
        val lineaInvalida = actual.linea == null || actual.linea !in Lineas
        val estratoInvalido = actual.profundidadM == null || actual.profundidadM !in 0..6

        if (centroInvalido || trenInvalido || lineaInvalida || estratoInvalido) {
            _uiState.update {
                it.copy(
                    errorCentro = if (centroInvalido) "Seleccione el centro de cultivo" else null,
                    errorTren = if (trenInvalido) "Seleccione el tren de cultivo" else null,
                    errorLinea = if (lineaInvalida) "Seleccione la línea de siembra" else null,
                    errorEstrato = if (estratoInvalido) "Seleccione el estrato de profundidad" else null,
                    mensaje = null,
                    parametrosValidos = null
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
                    profundidadM = actual.profundidadM!!
                )
            )
        }
    }

    /** La Screen avisa que ya navegó, para no volver a avanzar al regresar. */
    fun navegacionRealizada() {
        _uiState.update { it.copy(parametrosValidos = null) }
    }

    fun limpiar() {
        // Todos los campos vacíos y sin mensajes
        _uiState.value = MuestraUiState()
    }
}
