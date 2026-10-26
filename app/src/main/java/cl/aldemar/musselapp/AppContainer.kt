package cl.aldemar.musselapp

import android.content.Context
import cl.aldemar.musselapp.data.local.MusselDatabase
import cl.aldemar.musselapp.data.repository.AuthRepository
import cl.aldemar.musselapp.data.repository.BloqueoLogin
import cl.aldemar.musselapp.data.repository.PreferenciasIntentos
import cl.aldemar.musselapp.data.repository.MuestraRepository
import cl.aldemar.musselapp.data.repository.RoomAuthRepository
import cl.aldemar.musselapp.data.repository.RoomMuestraRepository
import cl.aldemar.musselapp.util.AlmacenFotos
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Contenedor manual de dependencias: crea una sola vez la base de datos
 * y los repositorios que usan todas las pantallas.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    /** Alcance de la app para trabajos que deben sobrevivir a las pantallas (ej. datos iniciales). */
    private val alcanceApp = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: MusselDatabase by lazy { MusselDatabase.crear(context, alcanceApp) }

    val muestraRepository: MuestraRepository by lazy { RoomMuestraRepository(database, alcanceApp) }

    val authRepository: AuthRepository by lazy {
        RoomAuthRepository(database.usuarioDao(), bloqueo = BloqueoLogin(PreferenciasIntentos(appContext)))
    }

    /** Borra una foto de la app (los ViewModels no tienen Context). */
    fun borrarFoto(ruta: String?) = AlmacenFotos.borrar(appContext, ruta)
}
