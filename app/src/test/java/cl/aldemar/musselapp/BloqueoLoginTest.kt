package cl.aldemar.musselapp

import cl.aldemar.musselapp.data.repository.BloqueoLogin
import cl.aldemar.musselapp.data.repository.BloqueoLogin.Companion.BLOQUEO_BASE_MS
import cl.aldemar.musselapp.data.repository.BloqueoLogin.Companion.BLOQUEO_MAXIMO_MS
import cl.aldemar.musselapp.data.repository.IntentosEnMemoria
import org.junit.Assert.assertEquals
import org.junit.Test

class BloqueoLoginTest {

    private var ahora = 1_000_000L
    private val bloqueo = BloqueoLogin(IntentosEnMemoria()) { ahora }
    private val rut = "222222222"

    private fun fallar(veces: Int) = (1..veces).map { bloqueo.registrarFallo(rut) }.last()

    @Test
    fun cuatroFallos_noBloquean() {
        fallar(4)
        assertEquals(0L, bloqueo.msRestantes(rut))
        assertEquals(1, bloqueo.intentosRestantes(rut))
    }

    @Test
    fun quintoFallo_bloqueaUnMinuto() {
        assertEquals(BLOQUEO_BASE_MS, fallar(5))
        assertEquals(BLOQUEO_BASE_MS, bloqueo.msRestantes(rut))
        ahora += BLOQUEO_BASE_MS
        assertEquals(0L, bloqueo.msRestantes(rut))
    }

    @Test
    fun cadaBloqueoDuplicaLaEspera_hastaElMaximo() {
        assertEquals(BLOQUEO_BASE_MS, fallar(5))
        assertEquals(2 * BLOQUEO_BASE_MS, fallar(5))
        assertEquals(4 * BLOQUEO_BASE_MS, fallar(5))
        assertEquals(BLOQUEO_MAXIMO_MS, BloqueoLogin.duracionBloqueo(20))
    }

    @Test
    fun ingresoCorrecto_reiniciaLaCuenta() {
        fallar(4)
        bloqueo.registrarExito(rut)
        assertEquals(5, bloqueo.intentosRestantes(rut))
        assertEquals(0L, fallar(4))
    }

    @Test
    fun losIntentosSonPorRut() {
        fallar(5)
        assertEquals(0L, bloqueo.msRestantes("111111111"))
    }
}
