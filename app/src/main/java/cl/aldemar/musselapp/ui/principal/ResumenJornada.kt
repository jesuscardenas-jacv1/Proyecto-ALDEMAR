package cl.aldemar.musselapp.ui.principal

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
        const val LINEAS_POR_CENTRO = 12

        fun desde(muestras: List<Muestra>, lineasTotales: Int = LINEAS_POR_CENTRO) = ResumenJornada(
            totalMuestras = muestras.size,
            tallaPromedioMm = muestras.map { it.tallaMm }.average().takeUnless { it.isNaN() }?.roundToInt() ?: 0,
            conteoPromedioIndM = muestras.map { it.conteoIndM }.average().takeUnless { it.isNaN() }?.roundToInt() ?: 0,
            lineasMuestreadas = muestras.map { it.linea }.distinct().size,
            lineasTotales = lineasTotales,
            validadas = muestras.count { it.estado == EstadoMuestra.VALIDADO },
            pendientes = muestras.count { it.estado == EstadoMuestra.PENDIENTE || it.estado == EstadoMuestra.CORREGIDO },
            observadas = muestras.count { it.estado == EstadoMuestra.OBSERVADO },
            pendientesSincronizar = muestras.count { !it.sincronizada },
            ultimaRegistradaEn = muestras.maxOfOrNull { it.registradaEn },
        )
    }
}
