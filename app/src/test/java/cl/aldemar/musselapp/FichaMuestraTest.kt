package cl.aldemar.musselapp

import cl.aldemar.musselapp.data.model.EstadoMuestra
import cl.aldemar.musselapp.data.model.Muestra
import cl.aldemar.musselapp.util.FichaMuestra
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone

/** Contenido de la ficha técnica que se descarga, imprime y comparte. */
class FichaMuestraTest {

    private val utc = TimeZone.getTimeZone("UTC")

    private val muestra = Muestra(
        id = 12, centroId = "dalcahue", linea = 4, tren = "B", profundidadM = 6,
        tallaMm = 65, conteoIndM = 410, observacion = "", estado = EstadoMuestra.CORREGIDO,
        registradaEn = 0L, sincronizada = false, operador = "Carla Vera",
        revisadaPor = "Diego Muñoz", comentarioSupervisor = "Conteo ajustado",
    )

    @Test
    fun nombreYTitulo_usanElCodigoDeLaMuestra() {
        assertEquals("Ficha_Muestra_012.pdf", FichaMuestra.nombreArchivo(muestra))
        assertEquals("Ficha técnica · Muestra 012", FichaMuestra.titulo(muestra))
    }

    @Test
    fun filas_traenLosDatosDeLaMuestra() {
        val filas = FichaMuestra.filas(muestra, utc).toMap()
        assertTrue(filas.getValue("Centro de cultivo").startsWith("Centro Canal Dalcahue"))
        assertEquals("Tren B · L04", filas["Tren / Línea"])
        assertEquals("410 ind/m", filas["Conteo"])
        assertEquals("Corregido", filas["Estado"])
        assertEquals("01/01/1970 00:00", filas["Registrada"])
        assertEquals("Conteo ajustado", filas["Comentario del supervisor"])
        // Campos vacíos se muestran con un guion, no en blanco
        assertEquals("—", filas["Observación de terreno"])
        assertEquals("No (pendiente de envío)", filas["Sincronizada"])
    }

    @Test
    fun textoCompartir_incluyeTituloDatosYPie() {
        val texto = FichaMuestra.textoCompartir(muestra, utc)
        assertTrue(texto.startsWith(FichaMuestra.titulo(muestra)))
        assertTrue(texto.contains("Revisada por: Diego Muñoz"))
        assertTrue(texto.endsWith(FichaMuestra.PIE))
    }
}
