package cl.aldemar.musselapp

import cl.aldemar.musselapp.data.model.EstadoMuestra
import cl.aldemar.musselapp.ui.theme.colores
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/** Pendiente y Corregido se ven iguales; Validado y Observado se distinguen de ellos. */
class ColoresEstadoTest {

    @Test
    fun pendienteYCorregido_compartenColor() {
        assertEquals(EstadoMuestra.PENDIENTE.colores, EstadoMuestra.CORREGIDO.colores)
    }

    @Test
    fun soloHayTresColoresDeEstado() {
        val distintos = EstadoMuestra.entries.map { it.colores }.toSet()
        assertEquals(3, distintos.size)
        assertNotEquals(EstadoMuestra.PENDIENTE.colores, EstadoMuestra.VALIDADO.colores)
        assertNotEquals(EstadoMuestra.PENDIENTE.colores, EstadoMuestra.OBSERVADO.colores)
    }
}
