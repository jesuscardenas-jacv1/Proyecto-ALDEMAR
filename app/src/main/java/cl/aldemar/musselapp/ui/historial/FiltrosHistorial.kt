package cl.aldemar.musselapp.ui.historial

import cl.aldemar.musselapp.data.model.EstadoMuestra
import cl.aldemar.musselapp.data.model.Muestra
import cl.aldemar.musselapp.util.Fechas
import java.text.Normalizer

/** Chips de estado del Historial. "Pendientes" incluye las muestras corregidas a la espera de revisión. */
enum class FiltroEstado(val etiqueta: String, val estados: Set<EstadoMuestra>) {
    TODOS("Todos", EstadoMuestra.entries.toSet()),
    VALIDADOS("Validados", setOf(EstadoMuestra.VALIDADO)),
    PENDIENTES("Pendientes", setOf(EstadoMuestra.PENDIENTE, EstadoMuestra.CORREGIDO)),
    OBSERVADOS("Observados", setOf(EstadoMuestra.OBSERVADO)),
}

enum class Periodo(val etiqueta: String, val dias: Int?) {
    HOY("Hoy", 1),
    SEMANA("Últimos 7 días", 7),
    TODO("Todo el historial", null),
}

enum class Orden(val etiqueta: String) {
    RECIENTES("Recientes"),
    ANTIGUAS("Antiguas"),
}

/** Criterios elegidos por el usuario en el Historial. */
data class CriteriosHistorial(
    val busqueda: String = "",
    val filtro: FiltroEstado = FiltroEstado.TODOS,
    val periodo: Periodo = Periodo.HOY,
    val orden: Orden = Orden.RECIENTES,
)

/** Resultado de aplicar los criterios: la lista visible y los conteos de cada chip. */
data class ResultadoHistorial(
    val muestras: List<Muestra>,
    val conteos: Map<FiltroEstado, Int>,
    val tallaPromedioMm: Double?,
)

object FiltrosHistorial {

    fun aplicar(todas: List<Muestra>, criterios: CriteriosHistorial, ahora: Long = System.currentTimeMillis()): ResultadoHistorial {
        val desde = criterios.periodo.dias?.let { Fechas.diasAtras(it, ahora) }
        // Se busca la frase completa: "línea 4" no debe coincidir con "MST-004"
        val frase = normalizar(criterios.busqueda).trim().replace(Regex("\\s+"), " ")

        // Periodo y búsqueda definen el universo; los chips muestran cuántas hay de cada estado en él
        val universo = todas.filter { m ->
            (desde == null || m.registradaEn >= desde) &&
                (frase.isEmpty() || frase in textoBuscable(m))
        }
        val conteos = FiltroEstado.entries.associateWith { f -> universo.count { it.estado in f.estados } }
        val visibles = universo
            .filter { it.estado in criterios.filtro.estados }
            .let { lista ->
                when (criterios.orden) {
                    Orden.RECIENTES -> lista.sortedByDescending { it.registradaEn }
                    Orden.ANTIGUAS -> lista.sortedBy { it.registradaEn }
                }
            }
        return ResultadoHistorial(
            muestras = visibles,
            conteos = conteos,
            tallaPromedioMm = visibles.takeIf { it.isNotEmpty() }?.map { it.tallaMm }?.average(),
        )
    }

    /** Texto donde se busca: código, línea (con y sin cero), tren, observación, operador, estado y fecha. */
    private fun textoBuscable(m: Muestra): String = normalizar(
        listOf(
            m.codigo, "#${m.codigo}", m.codigoLinea,
            "linea ${m.linea}", "linea %02d".format(m.linea),
            "tren ${m.tren}", m.observacion, m.operador, m.estado.etiqueta,
            Fechas.relativa(m.registradaEn),
        ).joinToString(" "),
    )

    /** Minúsculas y sin tildes: "Línea" y "linea" coinciden. */
    fun normalizar(texto: String): String =
        Normalizer.normalize(texto.lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "")
}
