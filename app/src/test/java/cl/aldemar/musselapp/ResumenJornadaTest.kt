package cl.aldemar.musselapp

import cl.aldemar.musselapp.data.model.EstadoMuestra
import cl.aldemar.musselapp.data.model.Muestra
import cl.aldemar.musselapp.ui.principal.ResumenJornada
import org.junit.Assert.assertEquals
import org.junit.Test

class ResumenJornadaTest {

    private fun muestra(linea: Int, talla: Int, conteo: Int, estado: EstadoMuestra, sincronizada: Boolean = true) =
        Muestra(linea.toLong(), "dalcahue", linea, "A", 5, talla, conteo, "", estado, 0L, sincronizada)

    @Test
    fun calculaPromediosYConteos() {
        val resumen = ResumenJornada.desde(
            listOf(
                muestra(1, 70, 400, EstadoMuestra.VALIDADO),
                muestra(1, 74, 460, EstadoMuestra.OBSERVADO, sincronizada = false),
                muestra(2, 66, 430, EstadoMuestra.PENDIENTE),
            ),
        )
        assertEquals(3, resumen.totalMuestras)
        assertEquals(70, resumen.tallaPromedioMm)
        assertEquals(430, resumen.conteoPromedioIndM)
        assertEquals(2, resumen.lineasMuestreadas)
        assertEquals(1, resumen.validadas)
        assertEquals(1, resumen.pendientes)
        assertEquals(1, resumen.observadas)
        assertEquals(1, resumen.pendientesSincronizar)
        assertEquals("Calibre 70-80", resumen.calibre)
        assertEquals("Densidad óptima", resumen.densidad)
    }

    @Test
    fun sinMuestras_noFalla() {
        val resumen = ResumenJornada.desde(emptyList())
        assertEquals(0, resumen.tallaPromedioMm)
        assertEquals("Sin datos", resumen.calibre)
        assertEquals(0f, resumen.avanceLineas)
    }
}
