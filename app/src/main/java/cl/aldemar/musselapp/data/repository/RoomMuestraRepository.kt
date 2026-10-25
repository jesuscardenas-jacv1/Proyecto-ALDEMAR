package cl.aldemar.musselapp.data.repository

import cl.aldemar.musselapp.data.local.MusselDatabase
import cl.aldemar.musselapp.data.local.aModelo
import cl.aldemar.musselapp.data.local.entidad.MuestraEntity
import cl.aldemar.musselapp.data.model.EstadoMuestra
import cl.aldemar.musselapp.data.model.Muestra
import cl.aldemar.musselapp.data.model.Usuario
import cl.aldemar.musselapp.util.RutValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Repositorio de muestras sobre la base Room. Reemplaza a [FakeMuestraRepository]
 * en la app; el Fake queda para las pruebas y las vistas previas.
 */
class RoomMuestraRepository(
    private val db: MusselDatabase,
    alcance: CoroutineScope,
    private val latenciaSincronizacionMs: Long = 1500,
) : MuestraRepository {

    private val muestraDao = db.muestraDao()
    private val usuarioDao = db.usuarioDao()

    /** Lista viva: Room vuelve a emitir cada vez que cambia una tabla de la consulta. */
    override val muestras: StateFlow<List<Muestra>> =
        muestraDao.observarTodas()
            .map { filas -> filas.map { it.aModelo() } }
            .stateIn(alcance, SharingStarted.Eagerly, emptyList())

    override fun obtenerPorId(id: Long): Flow<Muestra?> =
        muestraDao.observarPorId(id).map { it?.aModelo() }

    override suspend fun actualizarEstado(
        id: Long,
        nuevoEstado: EstadoMuestra,
        comentario: String?,
        supervisor: Usuario,
    ) {
        // Se busca por RUT (único); el nombre puede repetirse entre usuarios
        val supervisorRut = buscarRut(supervisor.rut)
            ?: error("Supervisor no registrado: ${supervisor.rut}")
        muestraDao.registrarRevision(id, nuevoEstado, comentario, supervisorRut, System.currentTimeMillis())
    }

    /** El id de [muestra] se ignora: lo genera la base al insertar. */
    override suspend fun agregarMuestra(muestra: Muestra) {
        val lineaId = db.catalogoDao().buscarLineaId(muestra.centroId, muestra.tren, muestra.linea)
            ?: error("No existe la línea ${muestra.linea} del tren ${muestra.tren} en ${muestra.centroId}")
        val operadorRut = buscarRut(muestra.operadorRut)
            ?: error("Operario no registrado: ${muestra.operadorRut}")
        muestraDao.insertar(
            MuestraEntity(
                lineaId = lineaId,
                operadorRut = operadorRut,
                profundidadM = muestra.profundidadM,
                tallaMm = muestra.tallaMm,
                conteoIndM = muestra.conteoIndM,
                observacion = muestra.observacion,
                estado = muestra.estado,
                registradaEn = muestra.registradaEn,
                sincronizada = false,
                fotoRuta = muestra.fotoRuta,
            ),
        )
    }

    override suspend fun sincronizar(): Int {
        delay(latenciaSincronizacionMs) // Simula el envío por el enlace satelital
        return muestraDao.marcarSincronizadas()
    }

    /** Acepta un RUT con o sin formato y devuelve el RUT normalizado si el usuario existe. */
    private suspend fun buscarRut(rut: String): String? =
        usuarioDao.buscarPorRut(RutValidator.normalizar(rut))?.rut
}
