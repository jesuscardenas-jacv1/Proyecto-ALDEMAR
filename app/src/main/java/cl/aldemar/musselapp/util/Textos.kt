package cl.aldemar.musselapp.util

/** Mensajes que dependen de una cantidad (singular o plural). */
object Textos {

    fun resultadoSincronizacion(enviadas: Int): String = when (enviadas) {
        0 -> "Todo estaba sincronizado"
        1 -> "1 muestra sincronizada"
        else -> "$enviadas muestras sincronizadas"
    }
}
