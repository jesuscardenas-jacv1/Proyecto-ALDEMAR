package cl.aldemar.musselapp.ui.muestra

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Datos de la muestra que se está registrando, compartidos entre
 * Nueva Muestra, Captura y Vista previa.
 */
data class BorradorMuestra(
    val parametros: ParametrosMuestra? = null,
    val fotoRuta: String? = null,
    // TODO: se completan cuando exista el paso 2 (Biometría y Fotos)
    val conteoIndM: Int? = null,
    val tallaMm: Int? = null,
    val fouling: String? = null,
    val observacion: String = "",
)

class BorradorMuestraViewModel : ViewModel() {

    private val _borrador = MutableStateFlow(BorradorMuestra())
    val borrador: StateFlow<BorradorMuestra> = _borrador.asStateFlow()

    fun guardarParametros(parametros: ParametrosMuestra) {
        _borrador.update { it.copy(parametros = parametros) }
    }

    fun guardarFoto(ruta: String) {
        _borrador.update { it.copy(fotoRuta = ruta) }
    }

    /** Cambia la foto desde Vista previa; null significa que se eliminó. */
    fun actualizarFoto(ruta: String?) {
        _borrador.update { it.copy(fotoRuta = ruta) }
    }

    /** Corrige centro, tren, línea o estrato sin volver al formulario. */
    fun actualizarParametros(parametros: ParametrosMuestra) {
        _borrador.update { it.copy(parametros = parametros) }
    }

    fun actualizarObservacion(texto: String) {
        _borrador.update { it.copy(observacion = texto) }
    }

    /** Se llama al terminar el muestreo, para empezar el siguiente desde cero. */
    fun limpiar() {
        _borrador.value = BorradorMuestra()
    }
}
