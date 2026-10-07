package cl.aldemar.musselapp.data.repository

import cl.aldemar.musselapp.data.model.Muestra
import kotlinx.coroutines.flow.StateFlow

/** Acceso a las muestras registradas en el dispositivo. */
interface MuestraRepository {
    val muestras: StateFlow<List<Muestra>>

    /** Envía al servidor las muestras guardadas localmente y devuelve cuántas se enviaron. */
    suspend fun sincronizar(): Int
}
