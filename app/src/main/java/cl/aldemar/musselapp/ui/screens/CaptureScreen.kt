package cl.aldemar.musselapp.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview as CameraPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import cl.aldemar.musselapp.ui.theme.MusselAppTheme
import java.io.File

@Composable
fun CaptureScreen(
    onContinuar: (String) -> Unit = {}
) {
    val context = LocalContext.current

    var tienePermiso by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    val pedirPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        tienePermiso = concedido
    }

    LaunchedEffect(Unit) {
        if (!tienePermiso) pedirPermiso.launch(Manifest.permission.CAMERA)
    }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    var fotoArchivo by remember { mutableStateOf<File?>(null) }
    var fotoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val foto = fotoBitmap

    // Selector de la galería del teléfono
    val selectorGaleria = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val archivo = copiarDesdeGaleria(context, uri)
            if (archivo != null) {
                fotoArchivo?.delete()
                fotoArchivo = archivo
                fotoBitmap = cargarBitmap(archivo.absolutePath)
            } else {
                Toast.makeText(context, "No se pudo abrir la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Captura de muestra",
            style = MaterialTheme.typography.headlineSmall
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
                .border(1.5.dp, Color.Gray, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            when {
                foto != null -> Image(
                    bitmap = foto.asImageBitmap(),
                    contentDescription = "Foto de la muestra",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                !tienePermiso -> Text("Se necesita permiso de cámara")
                else -> VistaPreviaCamara(
                    imageCapture = imageCapture,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Button(
            onClick = {
                if (foto == null) {
                    tomarFoto(context, imageCapture) { archivo ->
                        fotoArchivo = archivo
                        fotoBitmap = cargarBitmap(archivo.absolutePath)
                    }
                } else {
                    fotoArchivo?.delete()
                    fotoArchivo = null
                    fotoBitmap = null
                }
            },
            enabled = tienePermiso || foto != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(if (foto == null) "Tomar foto" else "Repetir foto")
        }

        OutlinedButton(
            onClick = {
                selectorGaleria.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Elegir de la galería")
        }

        Button(
            onClick = {
                fotoArchivo?.let {
                    Toast.makeText(context, "Foto lista para continuar", Toast.LENGTH_SHORT).show()
                    onContinuar(it.absolutePath)
                }
            },
            enabled = fotoArchivo != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Continuar")
        }
    }
}

@Composable
private fun VistaPreviaCamara(
    imageCapture: ImageCapture,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    // En el panel Preview de Android Studio no hay cámara real
    if (LocalInspectionMode.current) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Vista previa de cámara")
        }
        return
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val futuro = ProcessCameraProvider.getInstance(ctx)

            futuro.addListener({
                val proveedor = futuro.get()
                val vistaPrevia = CameraPreview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                proveedor.unbindAll()
                proveedor.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    vistaPrevia,
                    imageCapture
                )
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )
}

// Crea un archivo nuevo en la carpeta de muestras de la app
private fun nuevoArchivoMuestra(context: Context): File {
    val carpeta = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "muestras")
    carpeta.mkdirs()
    return File(carpeta, "muestra_${System.currentTimeMillis()}.jpg")
}

private fun tomarFoto(
    context: Context,
    imageCapture: ImageCapture,
    onGuardada: (File) -> Unit
) {
    val archivo = nuevoArchivoMuestra(context)
    val opciones = ImageCapture.OutputFileOptions.Builder(archivo).build()

    imageCapture.takePicture(
        opciones,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                onGuardada(archivo)
            }

            override fun onError(exception: ImageCaptureException) {
                Toast.makeText(context, "No se pudo tomar la foto", Toast.LENGTH_SHORT).show()
            }
        }
    )
}

// Copia la imagen elegida en la galería a la carpeta de la app
private fun copiarDesdeGaleria(context: Context, uri: Uri): File? {
    return try {
        val archivo = nuevoArchivoMuestra(context)
        context.contentResolver.openInputStream(uri)?.use { entrada ->
            archivo.outputStream().use { salida -> entrada.copyTo(salida) }
        } ?: return null
        archivo
    } catch (e: Exception) {
        null
    }
}

// Lee la foto reducida y la gira según la orientación que guardó la cámara
private fun cargarBitmap(ruta: String): Bitmap? {
    val opciones = BitmapFactory.Options().apply { inSampleSize = 4 }
    val bitmap = BitmapFactory.decodeFile(ruta, opciones) ?: return null

    val orientacion = ExifInterface(ruta).getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL
    )
    val grados = when (orientacion) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> 0f
    }
    if (grados == 0f) return bitmap

    val matriz = Matrix().apply { postRotate(grados) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matriz, true)
}

@Preview(showBackground = true)
@Composable
fun CaptureScreenPreview() {
    MusselAppTheme {
        CaptureScreen()
    }
}