package cl.aldemar.musselapp

import cl.aldemar.musselapp.data.repository.FakeMuestraRepository
import cl.aldemar.musselapp.ui.muestra.BorradorMuestra
import cl.aldemar.musselapp.ui.muestra.BorradorMuestraViewModel
import cl.aldemar.musselapp.ui.muestra.ParametrosMuestra
import org.junit.Assert.assertEquals
import org.junit.Test

/** Una muestra nueva no hereda los datos de un registro que se abandonó. */
class BorradorLimpiezaTest {

    private fun nuevoViewModel() = BorradorMuestraViewModel(FakeMuestraRepository())

    @Test
    fun limpiar_dejaElBorradorVacio() {
        val vm = nuevoViewModel()
        vm.guardarParametros(ParametrosMuestra("dalcahue", "B", 4, 3))
        vm.actualizarConteo(420)
        vm.actualizarTalla(65)
        vm.actualizarObservacion("nota vieja")

        vm.limpiar()

        assertEquals(BorradorMuestra(), vm.borrador.value)
    }

    @Test
    fun conteoYTalla_seCambianPorSeparado() {
        val vm = nuevoViewModel()
        vm.actualizarConteo(300)
        vm.actualizarTalla(70)
        vm.actualizarConteo(null)

        assertEquals(null, vm.borrador.value.conteoIndM)
        assertEquals(70, vm.borrador.value.tallaMm)
    }
}
