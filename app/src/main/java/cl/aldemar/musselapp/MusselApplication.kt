package cl.aldemar.musselapp

import android.app.Application

/** Punto de entrada de la app: crea el contenedor de dependencias al arrancar. */
class MusselApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        contenedor = AppContainer(this)
    }

    companion object {
        /** Base de datos y repositorios compartidos por los ViewModels. */
        lateinit var contenedor: AppContainer
            private set
    }
}
