package cl.aldemar.musselapp

import cl.aldemar.musselapp.util.RutValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RutValidatorTest {

    @Test
    fun rutConDigitoVerificadorCorrecto_esValido() {
        assertTrue(RutValidator.esValido("11.111.111-1"))
        assertTrue(RutValidator.esValido("222222222"))
        assertTrue(RutValidator.esValido("12.345.670-k"))
    }

    @Test
    fun rutConDigitoVerificadorIncorrecto_noEsValido() {
        assertFalse(RutValidator.esValido("11.111.111-2"))
        assertFalse(RutValidator.esValido("14.892.403-8"))
        assertFalse(RutValidator.esValido(""))
        assertFalse(RutValidator.esValido("K"))
    }

    @Test
    fun formatear_agregaPuntosYGuion() {
        assertEquals("11.111.111-1", RutValidator.formatear("111111111"))
        assertEquals("12.345.670-K", RutValidator.formatear("12345670k"))
    }
}
