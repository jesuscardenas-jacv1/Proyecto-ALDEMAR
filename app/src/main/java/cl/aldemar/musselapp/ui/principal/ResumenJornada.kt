package cl.aldemar.musselapp.ui.principal

import cl.aldemar.musselapp.data.local.DatosIniciales
import cl.aldemar.musselapp.data.model.EstadoMuestra
import cl.aldemar.musselapp.data.model.Muestra
import kotlin.math.roundToInt

/** Métricas de la jornada calculadas a partir de las muestras registradas. */
data class ResumenJornada(
    val totalMuestras: Int,
    val tallaPromedioMm: Int,
    val conteoPromedioIndM: Int,
    val lineasMuestreadas: Int,
    val lineasTotales: Int,
    val validadas: Int,
    val pendientes: Int,
    val observadas: Int,
    /** Muestras de cualquier día que esperan revisión del supervisor. */
    val pendientesRevision: Int,
    /** Muestras de cualquier día que aún no se envían: la cola no se reinicia cada día. */
    val pendientesSincronizar: Int,
    val ultimaRegistradaEn: Long?,
) {
    /** Rango de calibre comercial según la talla promedio. */
    val calibre: String
        get() = when {
            totalMuestras == 0 -> "Sin datos"
            tallaPromedioMm < 50 -> "Bajo calibre"
            else -> {
                val desde = tallaPromedioMm / 10 * 10
                "Calibre $desde-${desde + 10}"
            }
        }

    val densidad: String
        get() = when {
            totalMuestras == 0 -> "Sin datos"
            conteoPromedioIndM < 300 -> "Densidad baja"
            conteoPromedioIndM > 500 -> "Densidad alta"
            else -> "Densidad óptima"
        }

    val avanceLineas: Float
        get() = if (lineasTotales == 0) 0f else lineasMuestreadas.toFloat() / lineasTotales

    companion object {
        /** Cada centro tiene los trenes A, B y C con 12 líneas cada uno (36 en total). */
        val LINEAS_POR_CENTRO = DatosIniciales.TRENES.size * DatosIniciales.LINEAS_POR_TREN

        /**
         * [muestras] son las de la jornada (métricas y contadores); [todas] sirven para
         * la cola de revisión y de sincronización, que arrastran muestras de días anteriores.
         */
        fun desde(
            muestras: List<Muestra>,
            todas: List<Muestra> = muestras,
            lineasTotales: Int = LINEAS_POR_CENTRO,
        ) = ResumenJornada(
            totalMuestras = muestras.size,
            tallaPromedioMm = muestras.map { it.tallaMm }.average().takeUnless { it.isNaN() }?.roundToInt() ?: 0,
            conteoPromedioIndM = muestras.map { it.conteoIndM }.average().takeUnless { it.isNaN() }?.roundToInt() ?: 0,
            // Una línea se identifica por tren + número: la L04 del tren A y la del tren B son distintas
            lineasMuestreadas = muestras.map { it.tren to it.linea }.distinct().size,
            lineasTotales = lineasTotales,
            validadas = muestras.count { it.estado == EstadoMuestra.VALIDADO },
            pendientes = muestras.count { it.esperaRevision },
            observadas = muestras.count { it.estado == EstadoMuestra.OBSERVADO },
            pendientesRevision = todas.count { it.esperaRevision },
            pendientesSincronizar = todas.count { !it.sincronizada },
            ultimaRegistradaEn = todas.maxOfOrNull { it.registradaEn },
        )
    }
}

private val Muestra.esperaRevision: Boolean
    get() = estado == EstadoMuestra.PENDIENTE || estado == EstadoMuestra.CORREGIDO
