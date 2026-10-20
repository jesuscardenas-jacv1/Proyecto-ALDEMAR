package cl.aldemar.musselapp

import androidx.compose.ui.text.AnnotatedString
import cl.aldemar.musselapp.ui.login.RutVisualTransformation
import org.junit.Assert.assertEquals
import org.junit.Test

class RutVisualTransformationTest {

    private fun mostrar(rut: String) = RutVisualTransformation.filter(AnnotatedString(rut))

    @Test
    fun formateaMientrasSeEscribe() {
        assertEquals("1", mostrar("1").text.text)
        assertEquals("1-2", mostrar("12").text.text)
        assertEquals("1.234-5", mostrar("12345").text.text)
        assertEquals("11.111.111-1", mostrar("111111111").text.text)
        assertEquals("15.482.901-K", mostrar("15482901K").text.text)
    }

    @Test
    fun cursorAlFinal_quedaAlFinalDelTextoConFormato() {
        val mapeo = mostrar("111111111").offsetMapping
        assertEquals(12, mapeo.originalToTransformed(9))
        assertEquals(9, mapeo.transformedToOriginal(12))
    }

    @Test
    fun cursorAlMedio_saltaLosPuntosYElGuion() {
        // "11.111.111-1": tras 2 dígitos el cursor queda antes del primer punto
        val mapeo = mostrar("111111111").offsetMapping
        assertEquals(0, mapeo.originalToTransformed(0))
        assertEquals(2, mapeo.originalToTransformed(2))
        assertEquals(4, mapeo.originalToTransformed(3))
        assertEquals(10, mapeo.originalToTransformed(8))
        assertEquals(2, mapeo.transformedToOriginal(3))
        assertEquals(8, mapeo.transformedToOriginal(11))
    }
}
