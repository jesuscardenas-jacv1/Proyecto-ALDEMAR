package cl.aldemar.musselapp

import android.app.Application

/** Punto de entrada de la app: mantiene el contenedor de dependencias. */
class MusselApplication : Application() {

    lateinit var contenedor: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        contenedor = AppContainer(this)
    }
}
