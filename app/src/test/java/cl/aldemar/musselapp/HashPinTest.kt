package cl.aldemar.musselapp

import cl.aldemar.musselapp.util.HashPin
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** El PIN se guarda como hash y solo coincide con el PIN correcto. */
class HashPinTest {

    @Test
    fun hash_noContieneElPinYVerificaElCorrecto() {
        val guardado = HashPin.generar("1234")
        assertFalse(guardado.contains("1234"))
        assertTrue(HashPin.esHash(guardado))
        assertTrue(HashPin.verificar("1234", guardado))
    }

    @Test
    fun pinIncorrecto_noCoincide() {
        val guardado = HashPin.generar("1234")
        assertFalse(HashPin.verificar("0000", guardado))
        assertFalse(HashPin.verificar("", guardado))
    }

    @Test
    fun mismoPin_generaHashDistinto() {
        // Cada hash lleva su propia sal: dos usuarios con el mismo PIN no se delatan
        assertNotEquals(HashPin.generar("1234"), HashPin.generar("1234"))
    }

    @Test
    fun valorMalFormadoOPinEnTextoPlano_nuncaCoincide() {
        assertFalse(HashPin.esHash("1234"))
        assertFalse(HashPin.verificar("1234", "1234"))
        assertFalse(HashPin.verificar("1234", "pbkdf2\$abc\$zz\$00"))
        assertFalse(HashPin.verificar("1234", ""))
    }
}
