package cl.aldemar.musselapp.util

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Guarda el PIN como hash (PBKDF2) en vez de texto plano.
 *
 * Formato guardado: "pbkdf2$<iteraciones>$<sal en hex>$<hash en hex>".
 * Se usa HmacSHA1 porque está disponible desde Android 7 (minSdk 24);
 * HmacSHA256 recién existe desde Android 8. No depende de Android: se prueba en la JVM.
 */
object HashPin {

    private const val PREFIJO = "pbkdf2"
    private const val ALGORITMO = "PBKDF2WithHmacSHA1"
    private const val ITERACIONES = 10_000
    private const val BYTES_SAL = 16
    private const val BITS_HASH = 256

    private val aleatorio = SecureRandom()

    /** Devuelve el texto que se guarda en la columna `pin` de la tabla `usuarios`. */
    fun generar(pin: String): String {
        require(pin.isNotEmpty()) { "El PIN no puede estar vacío" }
        val sal = ByteArray(BYTES_SAL).also(aleatorio::nextBytes)
        val hash = derivar(pin, sal, ITERACIONES)
        return listOf(PREFIJO, ITERACIONES.toString(), sal.aHex(), hash.aHex()).joinToString("$")
    }

    /** true si [pin] corresponde a [guardado]. Un valor guardado mal formado nunca coincide. */
    fun verificar(pin: String, guardado: String): Boolean {
        if (pin.isEmpty()) return false
        val partes = guardado.split("$")
        if (partes.size != 4 || partes[0] != PREFIJO) return false
        val iteraciones = partes[1].toIntOrNull()?.takeIf { it > 0 } ?: return false
        val sal = partes[2].desdeHex() ?: return false
        val esperado = partes[3].desdeHex() ?: return false
        val calculado = derivar(pin, sal, iteraciones, esperado.size * 8)
        // Comparación en tiempo constante: no revela cuántos bytes coinciden
        return MessageDigest.isEqual(calculado, esperado)
    }

    /** true si [guardado] ya es un hash (y no un PIN antiguo en texto plano). */
    fun esHash(guardado: String): Boolean = guardado.startsWith("$PREFIJO$")

    private fun derivar(pin: String, sal: ByteArray, iteraciones: Int, bits: Int = BITS_HASH): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), sal, iteraciones, bits)
        try {
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun ByteArray.aHex(): String = joinToString("") { "%02x".format(it) }

    private fun String.desdeHex(): ByteArray? {
        if (isEmpty() || length % 2 != 0) return null
        val bytes = ByteArray(length / 2)
        for (i in bytes.indices) {
            val valor = substring(i * 2, i * 2 + 2).toIntOrNull(16) ?: return null
            bytes[i] = valor.toByte()
        }
        return bytes
    }
}
