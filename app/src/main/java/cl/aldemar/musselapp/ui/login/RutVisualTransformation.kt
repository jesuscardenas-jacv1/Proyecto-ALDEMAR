package cl.aldemar.musselapp.ui.login

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import cl.aldemar.musselapp.util.RutValidator

/**
 * Muestra el RUT con puntos y guion mientras se escribe ("12345678" → "1.234.567-8"),
 * pero el campo guarda solo dígitos y K. Así el cursor no salta al insertar los puntos.
 */
object RutVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val formateado = RutValidator.formatear(text.text)
        return TransformedText(AnnotatedString(formateado), mapeo(text.text.length, formateado))
    }

    /** Relaciona cada posición del texto escrito con su posición en el texto con formato. */
    internal fun mapeo(largoOriginal: Int, formateado: String): OffsetMapping {
        // posiciones[i] = índice en el texto con formato del carácter escrito número i
        val posiciones = formateado.indices.filter { formateado[it] != '.' && formateado[it] != '-' }
        return object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = when {
                offset <= 0 -> 0
                offset >= largoOriginal -> formateado.length
                else -> posiciones[offset - 1] + 1
            }

            override fun transformedToOriginal(offset: Int): Int =
                posiciones.count { it < offset }.coerceAtMost(largoOriginal)
        }
    }
}
