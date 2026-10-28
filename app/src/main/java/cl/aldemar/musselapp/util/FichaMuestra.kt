package cl.aldemar.musselapp.util

import cl.aldemar.musselapp.data.model.CentroCultivo
import cl.aldemar.musselapp.data.model.Muestra
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Contenido de la ficha técnica de una muestra: lo usan el PDF (descargar e imprimir)
 * y el texto que acompaña al compartir. No depende de Android, así se prueba en la JVM.
 */
object FichaMuestra {

    /** Pie de la ficha: el caso es académico y los datos son ficticios. */
    const val PIE = "MusselApp · Caso académico AquaSample / ALDEMAR SpA · Datos ficticios"

    fun titulo(muestra: Muestra): String = "Ficha técnica · ${muestra.nombre}"

    /** Nombre del archivo PDF, por ejemplo "Ficha_Muestra_012.pdf". */
    fun nombreArchivo(muestra: Muestra): String = "Ficha_Muestra_%03d.pdf".format(muestra.id)

    /** Filas "etiqueta → valor" de la ficha, en el orden en que se muestran. */
    fun filas(muestra: Muestra, zona: TimeZone = TimeZone.getDefault()): List<Pair<String, String>> = listOf(
        "Centro de cultivo" to nombreCentro(muestra.centroId),
        "Tren / Línea" to "Tren ${muestra.tren} · ${muestra.codigoLinea}",
        "Profundidad" to "${muestra.profundidadM} m",
        "Conteo" to "${muestra.conteoIndM} ind/m",
        "Talla media" to "${muestra.tallaMm} mm",
        "Rendimiento estimado" to "${muestra.rendimientoKgM} kg/m",
        "Estado" to muestra.estado.etiqueta,
        "Registrada" to fecha(muestra.registradaEn, zona),
        "Operario" to muestra.operador.ifBlank { "—" },
        "Revisada por" to (muestra.revisadaPor ?: "Sin revisión"),
        "Comentario del supervisor" to (muestra.comentarioSupervisor?.takeIf { it.isNotBlank() } ?: "—"),
        "Observación de terreno" to muestra.observacion.ifBlank { "—" },
        "Sincronizada" to if (muestra.sincronizada) "Sí" else "No (pendiente de envío)",
    )

    /** Resumen en texto para compartir junto con el PDF. */
    fun textoCompartir(muestra: Muestra, zona: TimeZone = TimeZone.getDefault()): String =
        buildString {
            appendLine(titulo(muestra))
            filas(muestra, zona).forEach { (etiqueta, valor) -> appendLine("$etiqueta: $valor") }
            append(PIE)
        }

    private fun nombreCentro(id: String): String =
        CentroCultivo.disponibles.firstOrNull { it.id == id }?.nombre ?: id

    private fun fecha(instante: Long, zona: TimeZone): String =
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CL"))
            .apply { timeZone = zona }
            .format(Date(instante))
}
