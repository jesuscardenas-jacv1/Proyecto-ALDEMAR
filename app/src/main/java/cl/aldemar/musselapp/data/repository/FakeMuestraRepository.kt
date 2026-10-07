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

        private fun muestrasDePrueba(ahora: Long): List<Muestra> {
            fun muestra(
                id: Long, linea: Int, tren: String, prof: Int, talla: Int, conteo: Int,
                obs: String, estado: EstadoMuestra, haceMin: Long, sincronizada: Boolean = true,
            ) = Muestra(id, "dalcahue", linea, tren, prof, talla, conteo, obs, estado, ahora - haceMin * MINUTO, sincronizada)

            return listOf(
                muestra(10, 4, "B", 6, 72, 450, "Mytilus chilensis", VALIDADO, 12, sincronizada = false),
                muestra(9, 2, "A", 9, 52, 280, "Epifauna detectada", OBSERVADO, 102, sincronizada = false),
                muestra(8, 7, "C", 4, 65, 410, "Muestreo preliminar", PENDIENTE, 147, sincronizada = false),
                muestra(7, 1, "A", 5, 70, 430, "Mytilus chilensis", VALIDADO, 175, sincronizada = false),
                muestra(6, 3, "B", 7, 74, 465, "Mytilus chilensis", VALIDADO, 210),
                muestra(5, 5, "C", 6, 69, 420, "Buena fijación", VALIDADO, 240),
                muestra(4, 6, "A", 8, 61, 380, "Talla irregular", PENDIENTE, 265),
                muestra(3, 8, "B", 5, 71, 440, "Mytilus chilensis", VALIDADO, 290),
                muestra(2, 4, "A", 6, 68, 415, "Mytilus chilensis", VALIDADO, 320),
                muestra(1, 2, "C", 7, 66, 400, "Conteo repetido", PENDIENTE, 350),
            )
        }
    }
}
