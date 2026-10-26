package cl.aldemar.musselapp.data.repository

import cl.aldemar.musselapp.data.model.EstadoMuestra
import cl.aldemar.musselapp.data.model.Muestra
import cl.aldemar.musselapp.data.model.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/** Acceso a las muestras registradas en el dispositivo. */
interface MuestraRepository {
    val muestras: StateFlow<List<Muestra>>

    /** Obtiene una muestra por su ID como flujo reactivo. */
    fun obtenerPorId(id: Long): Flow<Muestra?>

    /**
     * Registra el dictamen del [supervisor] (se identifica por su RUT).
     * Lanza una excepción si no se puede guardar; quien llama debe avisar al usuario.
     */
    suspend fun actualizarEstado(
        id: Long,
        nuevoEstado: EstadoMuestra,
        comentario: String?,
        supervisor: Usuario,
    )

    /** Agrega una nueva muestra al repositorio. */
    suspend fun agregarMuestra(muestra: Muestra)

    /** Envía al servidor las muestras guardadas localmente y devuelve cuántas se enviaron. */
    suspend fun sincronizar(): Int
}
