package cl.aldemar.musselapp

import cl.aldemar.musselapp.data.model.EstadoMuestra
import cl.aldemar.musselapp.data.model.Muestra
import cl.aldemar.musselapp.ui.historial.CriteriosHistorial
import cl.aldemar.musselapp.ui.historial.FiltroEstado
import cl.aldemar.musselapp.ui.historial.FiltrosHistorial
import cl.aldemar.musselapp.ui.historial.Orden
import cl.aldemar.musselapp.ui.historial.Periodo
import org.junit.Assert.assertEquals
import org.junit.Test

class FiltrosHistorialTest {

    private val ahora = 1_791_400_000_000L // instante fijo para que la prueba no dependa del reloj
    private val dia = 24 * 60 * 60 * 1000L

    private fun muestra(id: Long, linea: Int, tren: String, estado: EstadoMuestra, haceMs: Long, obs: String = "") =
        Muestra(id, "dalcahue", linea, tren, 5, 70, 420, obs, estado, ahora - haceMs, true, operador = "Carla Vera")

    private val muestras = listOf(
        muestra(1, 4, "B", EstadoMuestra.VALIDADO, 60_000),
        muestra(2, 2, "A", EstadoMuestra.OBSERVADO, 120_000, obs = "Epifauna detectada"),
        muestra(3, 7, "C", EstadoMuestra.PENDIENTE, 180_000),
        muestra(4, 9, "B", EstadoMuestra.CORREGIDO, 3 * dia),
        muestra(5, 12, "A", EstadoMuestra.VALIDADO, 20 * dia),
    )

    private fun ids(criterios: CriteriosHistorial) =
        FiltrosHistorial.aplicar(muestras, criterios, ahora).muestras.map { it.id }

    @Test
    fun periodo_filtraPorFecha() {
        assertEquals(listOf(1L, 2L, 3L), ids(CriteriosHistorial(periodo = Periodo.HOY)))
        assertEquals(listOf(1L, 2L, 3L, 4L), ids(CriteriosHistorial(periodo = Periodo.SEMANA)))
        assertEquals(5, ids(CriteriosHistorial(periodo = Periodo.TODO)).size)
    }

    @Test
    fun pendientes_incluyeCorregidas() {
        assertEquals(listOf(3L, 4L), ids(CriteriosHistorial(periodo = Periodo.TODO, filtro = FiltroEstado.PENDIENTES)))
    }

    @Test
    fun busqueda_sinTildesNiMayusculas() {
        assertEquals(listOf(1L), ids(CriteriosHistorial(periodo = Periodo.TODO, busqueda = "LÍNEA 4")))
        assertEquals(listOf(1L), ids(CriteriosHistorial(periodo = Periodo.TODO, busqueda = "l04")))
        assertEquals(listOf(2L), ids(CriteriosHistorial(periodo = Periodo.TODO, busqueda = "epifauna")))
        assertEquals(listOf(1L, 4L), ids(CriteriosHistorial(periodo = Periodo.TODO, busqueda = "tren b")))
        assertEquals(listOf(3L), ids(CriteriosHistorial(periodo = Periodo.TODO, busqueda = "mst-003")))
    }

    @Test
    fun conteosDeChips_respetanPeriodoYBusqueda() {
        val resultado = FiltrosHistorial.aplicar(muestras, CriteriosHistorial(periodo = Periodo.HOY), ahora)
        assertEquals(3, resultado.conteos[FiltroEstado.TODOS])
        assertEquals(1, resultado.conteos[FiltroEstado.VALIDADOS])
        assertEquals(1, resultado.conteos[FiltroEstado.PENDIENTES])
        assertEquals(1, resultado.conteos[FiltroEstado.OBSERVADOS])
    }

    @Test
    fun orden_antiguasPrimero() {
        assertEquals(listOf(5L, 4L, 3L, 2L, 1L), ids(CriteriosHistorial(periodo = Periodo.TODO, orden = Orden.ANTIGUAS)))
    }
}
