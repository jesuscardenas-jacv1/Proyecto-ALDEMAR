package cl.aldemar.musselapp.util

/** Validación y formato del RUT chileno (módulo 11). */
object RutValidator {

    /** Deja solo dígitos y la K final en mayúscula: "11.111.111-1" → "111111111". */
    fun normalizar(rut: String): String =
        rut.uppercase().filter { it.isDigit() || it == 'K' }

    fun esValido(rut: String): Boolean {
        val limpio = normalizar(rut)
        if (limpio.length < 2) return false
        val cuerpo = limpio.dropLast(1)
        if (!cuerpo.all { it.isDigit() }) return false
        return calcularDv(cuerpo) == limpio.last()
    }

    fun calcularDv(cuerpo: String): Char {
        var suma = 0
        var multiplicador = 2
        for (digito in cuerpo.reversed()) {
            suma += digito.digitToInt() * multiplicador
            multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
        }
        return when (val dv = 11 - suma % 11) {
            11 -> '0'
            10 -> 'K'
            else -> dv.digitToChar()
        }
    }

    /** Formatea con puntos y guion: "111111111" → "11.111.111-1". */
    fun formatear(rut: String): String {
        val limpio = normalizar(rut)
        if (limpio.length < 2) return limpio
        val cuerpo = limpio.dropLast(1).reversed().chunked(3).joinToString(".").reversed()
        return "$cuerpo-${limpio.last()}"
    }
}
