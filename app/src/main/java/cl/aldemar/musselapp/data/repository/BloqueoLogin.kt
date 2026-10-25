package cl.aldemar.musselapp.data.repository

import android.content.Context
import kotlin.math.min

/** Intentos fallidos de un RUT y hasta cuándo queda bloqueado (epoch ms). */
data class Intentos(val fallidos: Int = 0, val bloqueadoHasta: Long = 0L)

/** Dónde se guardan los intentos. Debe sobrevivir a cerrar la app, o el bloqueo se salta reiniciándola. */
interface RegistroIntentos {
    fun leer(rut: String): Intentos
    fun guardar(rut: String, intentos: Intentos)
    fun borrar(rut: String)
}

/**
 * Limita los intentos de PIN por RUT: cada [MAX_INTENTOS] fallos seguidos bloquea el ingreso,
 * primero 1 minuto y luego el doble cada vez (máximo 15). Un ingreso correcto reinicia la cuenta.
 *
 * Usa el reloj del teléfono: alguien que adelante la hora puede acortar la espera. Para un PIN
 * de 4 dígitos igual hace inviable probar las 10 000 combinaciones.
 */
class BloqueoLogin(
    private val registro: RegistroIntentos,
    private val reloj: () -> Long = System::currentTimeMillis,
) {

    /** Milisegundos que faltan para poder intentar de nuevo (0 si no está bloqueado). */
    fun msRestantes(rut: String): Long = (registro.leer(rut).bloqueadoHasta - reloj()).coerceAtLeast(0)

    /** Intentos que quedan antes del próximo bloqueo. */
    fun intentosRestantes(rut: String): Int = MAX_INTENTOS - registro.leer(rut).fallidos % MAX_INTENTOS

    /** Registra un PIN incorrecto y devuelve cuántos ms queda bloqueado (0 si aún no corresponde). */
    fun registrarFallo(rut: String): Long {
        val actual = registro.leer(rut)
        val fallidos = actual.fallidos + 1
        val bloqueoMs = if (fallidos % MAX_INTENTOS == 0) duracionBloqueo(fallidos / MAX_INTENTOS) else 0L
        val hasta = if (bloqueoMs > 0) reloj() + bloqueoMs else actual.bloqueadoHasta
        registro.guardar(rut, Intentos(fallidos, hasta))
        return bloqueoMs
    }

    fun registrarExito(rut: String) = registro.borrar(rut)

    companion object {
        const val MAX_INTENTOS = 5
        const val BLOQUEO_BASE_MS = 60_000L
        const val BLOQUEO_MAXIMO_MS = 15 * 60_000L

        /** 1 min el primer bloqueo, 2 el segundo, 4, 8 y luego 15 como tope. */
        fun duracionBloqueo(numeroDeBloqueo: Int): Long =
            min(BLOQUEO_BASE_MS shl (numeroDeBloqueo - 1).coerceIn(0, 10), BLOQUEO_MAXIMO_MS)
    }
}

/** Guarda los intentos en las preferencias privadas de la app (excluidas del respaldo). */
class PreferenciasIntentos(context: Context) : RegistroIntentos {

    private val prefs = context.applicationContext.getSharedPreferences("bloqueo_login", Context.MODE_PRIVATE)

    override fun leer(rut: String) = Intentos(
        fallidos = prefs.getInt("fallidos_$rut", 0),
        bloqueadoHasta = prefs.getLong("hasta_$rut", 0L),
    )

    override fun guardar(rut: String, intentos: Intentos) {
        prefs.edit()
            .putInt("fallidos_$rut", intentos.fallidos)
            .putLong("hasta_$rut", intentos.bloqueadoHasta)
            .apply()
    }

    override fun borrar(rut: String) {
        prefs.edit().remove("fallidos_$rut").remove("hasta_$rut").apply()
    }
}

/** Para pruebas y vistas previas: los intentos viven solo en memoria. */
class IntentosEnMemoria : RegistroIntentos {
    private val datos = mutableMapOf<String, Intentos>()
    override fun leer(rut: String) = datos[rut] ?: Intentos()
    override fun guardar(rut: String, intentos: Intentos) { datos[rut] = intentos }
    override fun borrar(rut: String) { datos.remove(rut) }
}
