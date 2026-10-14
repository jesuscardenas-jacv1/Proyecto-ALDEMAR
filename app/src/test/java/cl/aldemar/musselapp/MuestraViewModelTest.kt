package cl.aldemar.musselapp

import cl.aldemar.musselapp.data.model.CentroCultivo
import cl.aldemar.musselapp.ui.muestra.MuestraViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/** Pruebas mínimas de la guía de validación de formularios (Clase 8). */
class MuestraViewModelTest {

    private fun completarFormulario(vm: MuestraViewModel) {
        vm.cambiarCentro(CentroCultivo.disponibles.first())
        vm.cambiarTren("B")
        vm.cambiarLinea(4)
        vm.cambiarEstrato(3)
    }

    @Test
    fun guardarVacio_muestraTodosLosErrores() {
        val vm = MuestraViewModel()
        vm.guardar()
        val s = vm.uiState.value
        assertNotNull(s.errorCentro)
        assertNotNull(s.errorTren)
        assertNotNull(s.errorLinea)
        assertNotNull(s.errorEstrato)
        assertNull(s.mensaje)
        assertNull(s.parametrosValidos)
    }

    @Test
    fun guardarIncompleto_soloMarcaLosCamposFaltantes() {
        val vm = MuestraViewModel()
        vm.iniciar(CentroCultivo.disponibles.first())
        vm.cambiarTren("A")
        vm.guardar()
        val s = vm.uiState.value
        assertNull(s.errorCentro)
        assertNull(s.errorTren)
        assertNotNull(s.errorLinea)
        assertNotNull(s.errorEstrato)
        assertNull(s.parametrosValidos)
    }

    @Test
    fun corregirUnCampo_quitaSuError() {
        val vm = MuestraViewModel()
        vm.guardar()
        vm.cambiarLinea(2)
        assertNull(vm.uiState.value.errorLinea)
        assertNotNull(vm.uiState.value.errorTren)
    }

    @Test
    fun guardarCompleto_confirmaYEntregaParametros() {
        val vm = MuestraViewModel()
        completarFormulario(vm)
        vm.guardar()
        val s = vm.uiState.value
        assertNull(s.errorCentro)
        assertNull(s.errorTren)
        assertNull(s.errorLinea)
        assertNull(s.errorEstrato)
        assertEquals("Parámetros registrados correctamente", s.mensaje)
        val p = s.parametrosValidos!!
        assertEquals("dalcahue", p.centroId)
        assertEquals("B", p.tren)
        assertEquals(4, p.linea)
        assertEquals(3, p.profundidadM)
    }

    @Test
    fun limpiar_borraValoresErroresYMensajes() {
        val vm = MuestraViewModel()
        vm.guardar()
        completarFormulario(vm)
        vm.guardar()
        vm.limpiar()
        val s = vm.uiState.value
        assertNull(s.centro)
        assertNull(s.tren)
        assertNull(s.linea)
        assertNull(s.profundidadM)
        assertNull(s.errorCentro)
        assertNull(s.mensaje)
        assertNull(s.parametrosValidos)
    }

    @Test
    fun iniciar_noReemplazaUnCentroYaElegido() {
        val vm = MuestraViewModel()
        val otro = CentroCultivo.disponibles[1]
        vm.cambiarCentro(otro)
        vm.iniciar(CentroCultivo.disponibles.first())
        assertEquals(otro, vm.uiState.value.centro)
    }
}
