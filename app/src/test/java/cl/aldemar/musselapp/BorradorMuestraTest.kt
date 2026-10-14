package cl.aldemar.musselapp

import cl.aldemar.musselapp.ui.muestra.BorradorMuestra
import cl.aldemar.musselapp.ui.muestra.CONTEO_MAXIMO_IND_M
import cl.aldemar.musselapp.ui.muestra.ParametrosMuestra
import cl.aldemar.musselapp.ui.muestra.TALLA_MAXIMA_MM
import cl.aldemar.musselapp.ui.muestra.validarBorrador
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/** Reglas para poder guardar una muestra: ya no se inventan conteo ni talla. */
class BorradorMuestraTest {

    private val completo = BorradorMuestra(
        parametros = ParametrosMuestra("dalcahue", "B", 4, 3),
        fotoRuta = "/fotos/muestra.jpg",
        conteoIndM = 420,
        tallaMm = 65,
    )

    @Test
    fun borradorCompleto_sePuedeGuardar() {
        assertNull(validarBorrador(completo))
    }

    @Test
    fun sinConteoNiTalla_noSePuedeGuardar() {
        assertNotNull(validarBorrador(completo.copy(conteoIndM = null)))
        assertNotNull(validarBorrador(completo.copy(tallaMm = null)))
    }

    @Test
    fun sinParametrosOFoto_noSePuedeGuardar() {
        assertNotNull(validarBorrador(completo.copy(parametros = null)))
        assertNotNull(validarBorrador(completo.copy(fotoRuta = null)))
    }

    @Test
    fun valoresFueraDeRango_noSePuedenGuardar() {
        assertNotNull(validarBorrador(completo.copy(conteoIndM = 0)))
        assertNotNull(validarBorrador(completo.copy(conteoIndM = CONTEO_MAXIMO_IND_M + 1)))
        assertNotNull(validarBorrador(completo.copy(tallaMm = 0)))
        assertNotNull(validarBorrador(completo.copy(tallaMm = TALLA_MAXIMA_MM + 1)))
    }
}
