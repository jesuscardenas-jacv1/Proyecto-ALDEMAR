package cl.aldemar.musselapp

import cl.aldemar.musselapp.util.Textos
import org.junit.Assert.assertEquals
import org.junit.Test

class TextosTest {

    @Test
    fun resultadoSincronizacion_usaSingularYPlural() {
        assertEquals("Todo estaba sincronizado", Textos.resultadoSincronizacion(0))
        assertEquals("1 muestra sincronizada", Textos.resultadoSincronizacion(1))
        assertEquals("3 muestras sincronizadas", Textos.resultadoSincronizacion(3))
    }
}
