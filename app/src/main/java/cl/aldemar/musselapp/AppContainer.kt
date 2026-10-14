package cl.aldemar.musselapp

import android.content.Context
import cl.aldemar.musselapp.data.local.MusselDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Contenedor manual de dependencias: crea una sola vez la base de datos
 * (y en la fase 2, los repositorios Room) para toda la app.
 */
class AppContainer(context: Context) {

    /** Alcance de la app para trabajos que deben sobrevivir a las pantallas (ej. datos iniciales). */
    private val alcanceApp = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: MusselDatabase by lazy { MusselDatabase.crear(context, alcanceApp) }
}
