package cl.aldemar.musselapp.ui.componentes

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NoPhotography
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Lo que se muestra mientras se lee la foto de una muestra. */
private sealed interface EstadoFoto {
    data object Cargando : EstadoFoto
    data class Lista(val imagen: ImageBitmap) : EstadoFoto
    data object NoDisponible : EstadoFoto
}

/**
 * Foto de una muestra leída fuera del hilo principal. Sin foto, o si el archivo no se puede abrir,
 * se avisa con "Sin fotografía": nunca se muestra una imagen de ejemplo como si fuera la evidencia.
 */
@Composable
fun FotoMuestra(
    fotoRuta: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val estado by produceState<EstadoFoto>(
        initialValue = if (fotoRuta == null) EstadoFoto.NoDisponible else EstadoFoto.Cargando,
        fotoRuta,
    ) {
        value = if (fotoRuta == null) {
            EstadoFoto.NoDisponible
        } else {
            withContext(Dispatchers.IO) { decodificarFotoReducida(fotoRuta) }
                ?.let { EstadoFoto.Lista(it) }
                ?: EstadoFoto.NoDisponible
        }
    }

    when (val actual = estado) {
        is EstadoFoto.Lista -> Image(
            bitmap = actual.imagen,
            contentDescription = "Fotografía de la muestra",
            modifier = modifier,
            contentScale = contentScale,
        )
        EstadoFoto.Cargando -> Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainer),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
        }
        EstadoFoto.NoDisponible -> Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainer),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    Icons.Filled.NoPhotography,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(40.dp),
                )
                Text(
                    if (fotoRuta == null) "Sin fotografía" else "No se pudo abrir la fotografía",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
    }
}

/** Lee la foto reducida a [ladoMaxPx] (la original de la cámara pesa varios MB). null si no se puede leer. */
private fun decodificarFotoReducida(ruta: String, ladoMaxPx: Int = 1280): ImageBitmap? {
    val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(ruta, limites)
    if (limites.outWidth <= 0 || limites.outHeight <= 0) return null
    var muestreo = 1
    while (maxOf(limites.outWidth, limites.outHeight) / (muestreo * 2) >= ladoMaxPx) muestreo *= 2
    val opciones = BitmapFactory.Options().apply { inSampleSize = muestreo }
    return BitmapFactory.decodeFile(ruta, opciones)?.asImageBitmap()
}
