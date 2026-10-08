package cl.aldemar.musselapp.data.repository

import cl.aldemar.musselapp.data.model.EstadoMuestra
import cl.aldemar.musselapp.data.model.EstadoMuestra.OBSERVADO
import cl.aldemar.musselapp.data.model.EstadoMuestra.PENDIENTE
import cl.aldemar.musselapp.data.model.EstadoMuestra.VALIDADO
import cl.aldemar.musselapp.data.model.Muestra
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Muestras ficticias del día en curso, mientras no exista persistencia (Room). */
class FakeMuestraRepository(
    ahora: Long = System.currentTimeMillis(),
    private val latenciaMs: Long = 1500,
) : MuestraRepository {

    private val _muestras = MutableStateFlow(muestrasDePrueba(ahora))
    override val muestras: StateFlow<List<Muestra>> = _muestras.asStateFlow()

    override suspend fun sincronizar(): Int {
        delay(latenciaMs) // Simula el envío por el enlace satelital
        val pendientes = _muestras.value.count { !it.sincronizada }
        _muestras.update { lista -> lista.map { it.copy(sincronizada = true) } }
        return pendientes
    }

    companion object {
        /** Instancia compartida por las pantallas mientras no haya inyección de dependencias. */
        val instancia: FakeMuestraRepository by lazy { FakeMuestraRepository() }

        private const val MINUTO = 60_000L
        private const val DIA = 24 * 60 * MINUTO

        private const val OPERARIO = "Carla Vera"
        private const val SUPERVISOR = "Diego Muñoz"

        private fun muestrasDePrueba(ahora: Long): List<Muestra> {
            fun muestra(
                id: Long, linea: Int, tren: String, prof: Int, talla: Int, conteo: Int,
                obs: String, estado: EstadoMuestra, haceMin: Long, sincronizada: Boolean = true,
                comentario: String? = null,
            ) = Muestra(
                id = id, centroId = "dalcahue", linea = linea, tren = tren, profundidadM = prof,
                tallaMm = talla, conteoIndM = conteo, observacion = obs, estado = estado,
                registradaEn = ahora - haceMin * MINUTO, sincronizada = sincronizada,
                operador = OPERARIO,
                revisadaPor = if (estado == VALIDADO || estado == OBSERVADO) SUPERVISOR else null,
                comentarioSupervisor = comentario,
            )

            val hoy = listOf(
                muestra(14, 4, "B", 6, 72, 450, "Mytilus chilensis", VALIDADO, 12, sincronizada = false),
                muestra(13, 2, "A", 9, 52, 280, "Epifauna detectada", OBSERVADO, 102, sincronizada = false,
                    comentario = "Requiere reconteo: densidad bajo el rango mínimo estándar (< 350 ind/m)."),
                muestra(12, 7, "C", 4, 65, 410, "Muestreo preliminar", PENDIENTE, 147, sincronizada = false),
                muestra(11, 1, "A", 5, 70, 430, "Mytilus chilensis", VALIDADO, 175, sincronizada = false),
                muestra(10, 3, "B", 7, 74, 465, "Mytilus chilensis", VALIDADO, 210),
                muestra(9, 5, "C", 6, 69, 420, "Buena fijación", VALIDADO, 240),
                muestra(8, 6, "A", 8, 61, 380, "Talla irregular", PENDIENTE, 265),
                muestra(7, 8, "B", 5, 71, 440, "Mytilus chilensis", VALIDADO, 290),
                muestra(6, 4, "A", 6, 68, 415, "Mytilus chilensis", VALIDADO, 320),
                muestra(5, 2, "C", 7, 66, 400, "Conteo repetido", PENDIENTE, 350),
            )
            // Días anteriores: solo aparecen en el Historial (la Principal muestra la jornada)
            val anteriores = listOf(
                muestra(4, 3, "A", 4, 70, 465, "Mytilus chilensis", VALIDADO, DIA / MINUTO + 90),
                muestra(3, 9, "B", 6, 58, 330, "Densidad baja", OBSERVADO, DIA / MINUTO + 200,
                    comentario = "Repetir conteo en el tramo superior de la cuerda."),
                muestra(2, 11, "C", 5, 67, 425, "Mytilus chilensis", VALIDADO, 3 * DIA / MINUTO + 60),
                muestra(1, 12, "A", 3, 63, 405, "Muestreo de control", VALIDADO, 6 * DIA / MINUTO + 30),
            )
            return hoy + anteriores
        }
    }
}
