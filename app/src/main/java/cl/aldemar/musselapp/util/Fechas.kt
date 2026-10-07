package cl.aldemar.musselapp.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Utilidades de fecha (java.util para mantener compatibilidad con minSdk 24). */
object Fechas {

    private val LOCALE_CL = Locale("es", "CL")
    private const val DIA_MS = 24 * 60 * 60 * 1000L

    /** Instante en que comenzó el día de [ahora] (00:00 hora local). */
    fun inicioDelDia(ahora: Long = System.currentTimeMillis()): Long =
        Calendar.getInstance().apply {
            timeInMillis = ahora
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    fun hora(instante: Long): String = SimpleDateFormat("HH:mm", LOCALE_CL).format(Date(instante))

    /** "Hoy 10:45", "Ayer 16:20" o "14/10 09:30". */
    fun relativa(instante: Long, ahora: Long = System.currentTimeMillis()): String {
        val hoy = inicioDelDia(ahora)
        return when {
            instante >= hoy -> "Hoy ${hora(instante)}"
            instante >= hoy - DIA_MS -> "Ayer ${hora(instante)}"
            else -> SimpleDateFormat("dd/MM HH:mm", LOCALE_CL).format(Date(instante))
        }
    }

    /** "7 oct." */
    fun corta(instante: Long): String = SimpleDateFormat("d MMM", LOCALE_CL).format(Date(instante))

    fun diasAtras(dias: Int, ahora: Long = System.currentTimeMillis()): Long = inicioDelDia(ahora) - (dias - 1) * DIA_MS
}
