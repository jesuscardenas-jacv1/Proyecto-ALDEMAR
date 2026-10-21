package cl.aldemar.musselapp

import cl.aldemar.musselapp.data.repository.FakeMuestraRepository
import cl.aldemar.musselapp.ui.muestra.BorradorMuestraViewModel
import cl.aldemar.musselapp.ui.muestra.ParametrosMuestra
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Las fotos que dejan de usarse se borran; la de una muestra guardada no. */
class BorradorMuestraViewModelTest {

    private val borradas = mutableListOf<String>()

    private fun vm() = BorradorMuestraViewModel(
        repository = FakeMuestraRepository(latenciaMs = 0),
        borrarFoto = { borradas += it },
    )

    @Test
    fun repetirFoto_borraLaAnterior() {
        val vm = vm()
        vm.guardarFoto("/fotos/primera.jpg")
        vm.guardarFoto("/fotos/segunda.jpg")
        assertEquals(listOf("/fotos/primera.jpg"), borradas)
        assertEquals("/fotos/segunda.jpg", vm.borrador.value.fotoRuta)
    }

    @Test
    fun eliminarFoto_laBorra() {
        val vm = vm()
        vm.guardarFoto("/fotos/primera.jpg")
        vm.actualizarFoto(null)
        assertEquals(listOf("/fotos/primera.jpg"), borradas)
        assertNull(vm.borrador.value.fotoRuta)
    }

    @Test
    fun mismaFoto_noSeBorra() {
        val vm = vm()
        vm.guardarFoto("/fotos/primera.jpg")
        vm.actualizarFoto("/fotos/primera.jpg")
        assertTrue(borradas.isEmpty())
    }

    @Test
    fun descartar_borraLaFotoYVaciaElBorrador() {
        val vm = vm()
        vm.guardarParametros(ParametrosMuestra("dalcahue", "B", 4, 3))
        vm.guardarFoto("/fotos/primera.jpg")
        vm.descartar()
        assertEquals(listOf("/fotos/primera.jpg"), borradas)
        assertNull(vm.borrador.value.parametros)
        assertNull(vm.borrador.value.fotoRuta)
    }

    @Test
    fun limpiar_conservaLaFoto() {
        // limpiar() se usa después de guardar: la foto ya pertenece a la muestra
        val vm = vm()
        vm.guardarFoto("/fotos/primera.jpg")
        vm.limpiar()
        assertTrue(borradas.isEmpty())
        assertNull(vm.borrador.value.fotoRuta)
    }
}
