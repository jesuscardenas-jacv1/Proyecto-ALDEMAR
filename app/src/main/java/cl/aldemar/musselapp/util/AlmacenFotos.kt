package cl.aldemar.musselapp.util

import android.content.Context
import android.net.Uri
import java.io.File

/**
 * Dónde y cómo se guardan las fotos de las muestras.
 *
 * Las fotos confirmadas van a la carpeta privada de la app (`filesDir/fotos/`):
 * otras apps no pueden leerlas y no dependen del almacenamiento externo.
 * La base solo guarda la ruta del archivo (ver docs/base-de-datos.md).
 *
 * Las fotos aún sin confirmar quedan en `cacheDir/pendientes/` (las crea Captura).
 */
object AlmacenFotos {

    const val CARPETA = "fotos"

    /** Carpeta privada de fotos; se crea si no existe. */
    fun carpeta(context: Context): File = File(context.filesDir, CARPETA).apply { mkdirs() }

    /** Archivo nuevo (aún vacío) para una foto de muestra. */
    fun nuevoArchivo(context: Context): File = nuevoArchivo(carpeta(context))

    /** Mueve una foto confirmada (por ejemplo desde `cacheDir/pendientes`) a la carpeta privada. */
    fun guardar(context: Context, temporal: File): File {
        val destino = nuevoArchivo(context)
        temporal.copyTo(destino, overwrite = true)
        temporal.delete()
        return destino
    }

    /** Copia a la carpeta privada una imagen elegida en la galería. Devuelve null si no se pudo leer. */
    fun copiarDesdeUri(context: Context, uri: Uri): File? {
        val destino = nuevoArchivo(context)
        return try {
            context.contentResolver.openInputStream(uri)?.use { entrada ->
                destino.outputStream().use { salida -> entrada.copyTo(salida) }
            } ?: run {
                destino.delete()
                return null
            }
            destino
        } catch (e: Exception) {
            destino.delete()
            null
        }
    }

    /**
     * Borra la foto solo si es un archivo de la app (carpeta privada, caché o la carpeta externa
     * que se usaba antes). Nunca toca la copia de la galería del teléfono.
     */
    fun borrar(context: Context, ruta: String?) {
        if (ruta == null) return
        val propias = listOfNotNull(context.filesDir, context.cacheDir, context.getExternalFilesDir(null))
        val archivo = File(ruta)
        if (estaDentroDe(archivo, propias)) runCatching { archivo.delete() }
    }

    // ── Lógica pura (probada en FotosTest) ───────────────────────────────

    /** Nombre por hora de creación; si ya existe uno igual (dos fotos en el mismo ms) agrega un sufijo. */
    internal fun nuevoArchivo(carpeta: File, ahora: Long = System.currentTimeMillis()): File {
        var archivo = File(carpeta, "muestra_$ahora.jpg")
        var n = 1
        while (archivo.exists()) {
            archivo = File(carpeta, "muestra_${ahora}_$n.jpg")
            n++
        }
        return archivo
    }

    /** true si [archivo] está dentro de alguna de las [carpetas] (evita trucos con "../"). */
    internal fun estaDentroDe(archivo: File, carpetas: List<File>): Boolean {
        val ruta = archivo.canonicalPath
        return carpetas.any { carpeta ->
            val base = carpeta.canonicalPath
            ruta.startsWith(base + File.separator)
        }
    }
}
