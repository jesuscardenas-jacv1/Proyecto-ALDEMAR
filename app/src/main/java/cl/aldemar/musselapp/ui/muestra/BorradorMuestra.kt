package cl.aldemar.musselapp.ui.muestra

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.aldemar.musselapp.MusselApplication
import cl.aldemar.musselapp.data.model.EstadoMuestra
import cl.aldemar.musselapp.data.model.Muestra
import cl.aldemar.musselapp.data.model.Usuario
import cl.aldemar.musselapp.data.repository.MuestraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Datos de la muestra que se está registrando, compartidos entre
 * Nueva Muestra, Captura y Vista previa.
 */
data class BorradorMuestra(
    val parametros: ParametrosMuestra? = null,
    val fotoRuta: String? = null,
    // Conteo y talla los ingresa el operario en Vista previa
    val conteoIndM: Int? = null,
    val tallaMm: Int? = null,
    // TODO: se completa cuando exista el paso 2 (Biometría y Fotos); hoy no se guarda en la base
    val fouling: String? = null,
    val observacion: String = "",
)

/** Rangos aceptados al ingresar las mediciones (evitan errores de tipeo, no son límites biológicos). */
const val CONTEO_MAXIMO_IND_M = 5000
const val TALLA_MAXIMA_MM = 200

/**
 * Revisa que el borrador tenga todo lo obligatorio para guardarse.
 * Devuelve el primer problema encontrado, o null si se puede guardar.
 */
fun validarBorrador(borrador: BorradorMuestra): String? = when {
    borrador.parametros == null -> "Faltan centro, tren, línea y estrato"
    borrador.fotoRuta == null -> "Falta la fotografía de la muestra"
    borrador.conteoIndM == null -> "Ingresa el conteo de individuos por metro"
    borrador.conteoIndM !in 1..CONTEO_MAXIMO_IND_M -> "El conteo debe estar entre 1 y $CONTEO_MAXIMO_IND_M ind/m"
    borrador.tallaMm == null -> "Ingresa la talla media en mm"
    borrador.tallaMm !in 1..TALLA_MAXIMA_MM -> "La talla debe estar entre 1 y $TALLA_MAXIMA_MM mm"
    else -> null
}

class BorradorMuestraViewModel(
    private val repository: MuestraRepository = MusselApplication.contenedor.muestraRepository,
    /** Borra el archivo de una foto que ya no se usa (inyectable para las pruebas). */
    private val borrarFoto: (String) -> Unit = { MusselApplication.contenedor.borrarFoto(it) },
) : ViewModel() {

    private val _borrador = MutableStateFlow(BorradorMuestra())
    val borrador: StateFlow<BorradorMuestra> = _borrador.asStateFlow()

    private val _guardando = MutableStateFlow(false)
    val guardando: StateFlow<Boolean> = _guardando.asStateFlow()

    fun guardarParametros(parametros: ParametrosMuestra) {
        _borrador.update { it.copy(parametros = parametros) }
    }

    fun guardarFoto(ruta: String) = cambiarFoto(ruta)

    /** Cambia la foto desde Vista previa; null significa que se eliminó. */
    fun actualizarFoto(ruta: String?) = cambiarFoto(ruta)

    // Al repetir o eliminar la foto, la anterior se borra para que no quede un archivo sin muestra
    private fun cambiarFoto(ruta: String?) {
        val anterior = _borrador.value.fotoRuta
        _borrador.update { it.copy(fotoRuta = ruta) }
        if (anterior != null && anterior != ruta) borrarFoto(anterior)
    }

    /** Corrige centro, tren, línea o estrato sin volver al formulario. */
    fun actualizarParametros(parametros: ParametrosMuestra) {
        _borrador.update { it.copy(parametros = parametros) }
    }

    fun actualizarObservacion(texto: String) {
        _borrador.update { it.copy(observacion = texto) }
    }

    /** null significa que el operario borró el valor. */
    fun actualizarConteo(conteoIndM: Int?) {
        _borrador.update { it.copy(conteoIndM = conteoIndM) }
    }

    fun actualizarTalla(tallaMm: Int?) {
        _borrador.update { it.copy(tallaMm = tallaMm) }
    }

    /**
     * Guarda la muestra en la base local a nombre de [operador].
     * Si se guarda, limpia el borrador y llama a [alGuardar];
     * si falta un dato o la base lo rechaza, llama a [alFallar] con el motivo.
     */
    fun confirmar(
        operador: Usuario,
        alGuardar: () -> Unit,
        alFallar: (String) -> Unit,
    ) {
        if (_guardando.value) return
        val actual = _borrador.value
        validarBorrador(actual)?.let { problema ->
            alFallar(problema)
            return
        }
        // validarBorrador ya comprobó que estos datos no son null
        val parametros = actual.parametros!!
        val nueva = Muestra(
            id = 0, // lo asigna la base al insertar
            centroId = parametros.centroId,
            linea = parametros.linea,
            tren = parametros.tren,
            profundidadM = parametros.profundidadM,
            tallaMm = actual.tallaMm!!,
            conteoIndM = actual.conteoIndM!!,
            observacion = actual.observacion.trim(),
            estado = EstadoMuestra.PENDIENTE,
            registradaEn = System.currentTimeMillis(),
            sincronizada = false,
            operador = operador.nombre,
            operadorRut = operador.rut,
            fotoRuta = actual.fotoRuta,
        )

        _guardando.value = true
        viewModelScope.launch {
            try {
                repository.agregarMuestra(nueva)
                limpiar()
                alGuardar()
            } catch (e: Exception) {
                // Antes un error aquí cerraba la app; ahora se avisa y el borrador se conserva
                alFallar("No se pudo guardar la muestra: ${e.message ?: "error de la base de datos"}")
            } finally {
                _guardando.value = false
            }
        }
    }

    /**
     * Abandona la muestra a medio registrar (por ejemplo al cerrar sesión) y borra su foto.
     * No usar después de guardar: esa foto ya pertenece a la muestra (para eso está [limpiar]).
     */
    fun descartar() {
        _borrador.value.fotoRuta?.let(borrarFoto)
        limpiar()
    }

    /** Se llama al terminar el muestreo, para empezar el siguiente desde cero. La foto se conserva. */
    fun limpiar() {
        _borrador.value = BorradorMuestra()
    }
}
