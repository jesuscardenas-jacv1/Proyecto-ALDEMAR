package cl.aldemar.musselapp

import cl.aldemar.musselapp.util.AlmacenFotos
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/** Reglas de nombres y de borrado seguro de las fotos de muestras. */
class AlmacenFotosTest {

    @get:Rule
    val tmp = TemporaryFolder()

    @Test
    fun nuevoArchivo_quedaEnLaCarpetaConNombreDeMuestra() {
        val carpeta = tmp.newFolder(AlmacenFotos.CARPETA)
        val archivo = AlmacenFotos.nuevoArchivo(carpeta, ahora = 123L)
        assertEquals(carpeta, archivo.parentFile)
        assertEquals("muestra_123.jpg", archivo.name)
    }

    @Test
    fun nuevoArchivo_noPisaUnaFotoDelMismoInstante() {
        val carpeta = tmp.newFolder(AlmacenFotos.CARPETA)
        val primera = AlmacenFotos.nuevoArchivo(carpeta, ahora = 123L).apply { writeText("foto 1") }
        val segunda = AlmacenFotos.nuevoArchivo(carpeta, ahora = 123L)
        assertNotEquals(primera, segunda)
        assertEquals("muestra_123_1.jpg", segunda.name)
    }

    @Test
    fun estaDentroDe_soloAceptaArchivosDeLasCarpetasDeLaApp() {
        val app = tmp.newFolder("app")
        val fotos = File(app, "fotos").apply { mkdirs() }
        val externo = tmp.newFolder("galeria")

        assertTrue(AlmacenFotos.estaDentroDe(File(fotos, "muestra_1.jpg"), listOf(app)))
        assertFalse(AlmacenFotos.estaDentroDe(File(externo, "foto.jpg"), listOf(app)))
        // Una ruta con "../" que sale de la carpeta no cuenta como propia
        assertFalse(AlmacenFotos.estaDentroDe(File(fotos, "../../galeria/foto.jpg"), listOf(app)))
        // La carpeta misma no es un archivo que se pueda borrar
        assertFalse(AlmacenFotos.estaDentroDe(app, listOf(app)))
    }
}
