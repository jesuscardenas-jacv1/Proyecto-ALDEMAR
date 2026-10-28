package cl.aldemar.musselapp.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import cl.aldemar.musselapp.data.model.Muestra
import java.io.File
import java.io.FileOutputStream

/**
 * Ficha técnica de una muestra en PDF (A4) y las acciones del Detalle:
 * descargar, imprimir y compartir. El contenido viene de [FichaMuestra].
 *
 * [generar] lee la foto y escribe un archivo: llamarlo fuera del hilo principal.
 */
object FichaPdf {

    // Hoja A4 en puntos (1/72 de pulgada)
    private const val ANCHO = 595
    private const val ALTO = 842
    private const val MARGEN = 40f
    private const val ALTO_MAX_FOTO = 260f

    /** Authority del FileProvider declarado en el AndroidManifest. */
    private fun authority(context: Context) = "${context.packageName}.archivos"

    /** Crea el PDF en la caché de la app (`cacheDir/fichas/`) y devuelve el archivo. */
    fun generar(context: Context, muestra: Muestra): File {
        val documento = PdfDocument()
        try {
            val pagina = documento.startPage(PdfDocument.PageInfo.Builder(ANCHO, ALTO, 1).create())
            dibujar(pagina.canvas, muestra)
            documento.finishPage(pagina)

            val carpeta = File(context.cacheDir, "fichas").apply { mkdirs() }
            val archivo = File(carpeta, FichaMuestra.nombreArchivo(muestra))
            FileOutputStream(archivo).use { documento.writeTo(it) }
            return archivo
        } finally {
            documento.close()
        }
    }

    /** Copia el PDF al destino que eligió el usuario (selector "Guardar como" de Android). */
    fun guardarEn(context: Context, pdf: File, destino: Uri) {
        val salida = context.contentResolver.openOutputStream(destino)
            ?: error("No se pudo abrir el destino")
        salida.use { s -> pdf.inputStream().use { it.copyTo(s) } }
    }

    /** Abre el menú de compartir de Android con el PDF adjunto y un resumen en texto. */
    fun compartir(context: Context, pdf: File, muestra: Muestra) {
        val uri = FileProvider.getUriForFile(context, authority(context), pdf)
        val envio = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, FichaMuestra.titulo(muestra))
            putExtra(Intent.EXTRA_TEXT, FichaMuestra.textoCompartir(muestra))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(envio, "Compartir ficha de la ${muestra.nombre}"))
    }

    /** Envía el PDF al servicio de impresión de Android (también permite "Guardar como PDF"). */
    fun imprimir(context: Context, pdf: File, muestra: Muestra) {
        val impresion = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        impresion.print(
            FichaMuestra.titulo(muestra),
            AdaptadorPdf(pdf),
            PrintAttributes.Builder().setMediaSize(PrintAttributes.MediaSize.ISO_A4).build(),
        )
    }

    // ── Dibujo de la página ──────────────────────────────────────────────

    private fun dibujar(canvas: Canvas, muestra: Muestra) {
        val anchoUtil = ANCHO - 2 * MARGEN

        // Encabezado con el color principal de la app (#0F172A)
        val fondoEncabezado = Paint().apply { color = Color.parseColor("#0F172A") }
        canvas.drawRect(0f, 0f, ANCHO.toFloat(), 90f, fondoEncabezado)
        val titulo = TextPaint().apply {
            color = Color.WHITE
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        canvas.drawText(FichaMuestra.titulo(muestra), MARGEN, 45f, titulo)
        val subtitulo = TextPaint(titulo).apply {
            textSize = 11f
            typeface = Typeface.DEFAULT
            color = Color.parseColor("#CBD5E1")
        }
        canvas.drawText("MusselApp · Registro y trazabilidad de muestras", MARGEN, 68f, subtitulo)
        var y = 115f

        // Foto de la muestra, si tiene
        cargarFoto(muestra.fotoRuta, anchoUtil.toInt())?.let { foto ->
            val escala = minOf(anchoUtil / foto.width, ALTO_MAX_FOTO / foto.height)
            val ancho = foto.width * escala
            val alto = foto.height * escala
            canvas.drawBitmap(foto, null, RectF(MARGEN, y, MARGEN + ancho, y + alto), null)
            y += alto + 20f
        }

        // Filas de datos: etiqueta a la izquierda, valor (con salto de línea) a la derecha
        val etiqueta = TextPaint().apply {
            color = Color.parseColor("#334155")
            textSize = 10.5f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        val valor = TextPaint(etiqueta).apply {
            color = Color.BLACK
            typeface = Typeface.DEFAULT
        }
        val divisor = Paint().apply { color = Color.parseColor("#E2E8F0") }
        val anchoEtiqueta = 160f
        val anchoValor = (anchoUtil - anchoEtiqueta).toInt()

        for ((nombre, dato) in FichaMuestra.filas(muestra)) {
            val bloque = bloqueTexto(dato, valor, anchoValor)
            canvas.drawText(nombre, MARGEN, y + 11f, etiqueta)
            canvas.save()
            canvas.translate(MARGEN + anchoEtiqueta, y)
            bloque.draw(canvas)
            canvas.restore()
            y += maxOf(bloque.height.toFloat(), 14f) + 8f
            canvas.drawLine(MARGEN, y - 4f, ANCHO - MARGEN, y - 4f, divisor)
        }

        val pie = TextPaint(valor).apply {
            textSize = 9f
            color = Color.parseColor("#64748B")
        }
        canvas.drawText(FichaMuestra.PIE, MARGEN, ALTO - MARGEN / 2, pie)
    }

    private fun bloqueTexto(texto: String, pintura: TextPaint, ancho: Int): StaticLayout =
        StaticLayout.Builder.obtain(texto, 0, texto.length, pintura, ancho)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .build()

    /** Lee la foto reducida al ancho de la página y la gira según su EXIF. */
    private fun cargarFoto(ruta: String?, anchoMax: Int): Bitmap? {
        if (ruta == null || !File(ruta).exists()) return null
        return try {
            val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(ruta, limites)
            var reduccion = 1
            while (limites.outWidth / (reduccion * 2) >= anchoMax * 2) reduccion *= 2
            val bitmap = BitmapFactory.decodeFile(ruta, BitmapFactory.Options().apply { inSampleSize = reduccion })
                ?: return null
            val grados = when (ExifInterface(ruta).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
            if (grados == 0f) bitmap
            else Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply { postRotate(grados) }, true)
        } catch (e: Exception) {
            null // Sin foto la ficha se genera igual
        }
    }

    /** Entrega un PDF ya generado al servicio de impresión. */
    private class AdaptadorPdf(private val pdf: File) : PrintDocumentAdapter() {

        override fun onLayout(
            oldAttributes: PrintAttributes?,
            newAttributes: PrintAttributes?,
            cancellationSignal: CancellationSignal?,
            callback: PrintDocumentAdapter.LayoutResultCallback,
            extras: Bundle?,
        ) {
            if (cancellationSignal?.isCanceled == true) {
                callback.onLayoutCancelled()
                return
            }
            val info = PrintDocumentInfo.Builder(pdf.name)
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(1)
                .build()
            callback.onLayoutFinished(info, true)
        }

        override fun onWrite(
            pages: Array<out PageRange>?,
            destination: ParcelFileDescriptor,
            cancellationSignal: CancellationSignal?,
            callback: PrintDocumentAdapter.WriteResultCallback,
        ) {
            try {
                pdf.inputStream().use { entrada ->
                    FileOutputStream(destination.fileDescriptor).use { entrada.copyTo(it) }
                }
                callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
            } catch (e: Exception) {
                callback.onWriteFailed(e.message)
            }
        }
    }
}
