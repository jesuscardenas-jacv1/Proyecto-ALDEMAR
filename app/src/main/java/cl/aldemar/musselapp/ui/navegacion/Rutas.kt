package cl.aldemar.musselapp.ui.navegacion

import cl.aldemar.musselapp.ui.principal.Destino

/**
 * Rutas de navegación de MusselApp (inventario de pantallas 3.1).
 * Cada integrante reemplaza la pantalla provisoria de su ruta en MusselNavHost.
 */
object Rutas {
    const val LOGIN = "login"
    const val PRINCIPAL = "principal"
    const val MUESTRA = "muestra"
    const val CAPTURA = "captura"
    const val VISTA_PREVIA = "vista_previa"
    const val HISTORIAL = "historial"
    const val REVISION = "revision"

    const val ARG_MUESTRA_ID = "muestraId"
    const val DETALLE = "detalle/{$ARG_MUESTRA_ID}"
    fun detalle(muestraId: Long) = "detalle/$muestraId"
}

/** Ruta a la que lleva cada opción del menú de la Pantalla Principal. */
val Destino.ruta: String
    get() = when (this) {
        Destino.MUESTRAS -> Rutas.PRINCIPAL
        Destino.NUEVA_MUESTRA -> Rutas.MUESTRA
        Destino.HISTORIAL -> Rutas.HISTORIAL
        Destino.SUPERVISION -> Rutas.REVISION
    }
