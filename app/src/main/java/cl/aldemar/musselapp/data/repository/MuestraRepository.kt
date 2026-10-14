package cl.aldemar.musselapp.data.repository

import cl.aldemar.musselapp.data.model.EstadoMuestra
import cl.aldemar.musselapp.data.model.Muestra
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/** Acceso a las muestras registradas en el dispositivo. */
interface MuestraRepository {
    val muestras: StateFlow<List<Muestra>>

    /** Obtiene una muestra por su ID como flujo reactivo. */
    fun obtenerPorId(id: Long): Flow<Muestra?>

    /** Actualiza el estado y dictamen técnico de una muestra. */
    suspend fun actualizarEstado(
        id: Long,
        nuevoEstado: EstadoMuestra,
        comentario: String?,
        revisadaPor: String,
    )

    /** Agrega una nueva muestra al repositorio. */
    suspend fun agregarMuestra(muestra: Muestra)

    /** Envía al servidor las muestras guardadas localmente y devuelve cuántas se enviaron. */
    suspend fun sincronizar(): Int
}
