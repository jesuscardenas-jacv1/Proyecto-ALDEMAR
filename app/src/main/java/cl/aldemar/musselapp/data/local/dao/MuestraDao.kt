package cl.aldemar.musselapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import cl.aldemar.musselapp.data.local.entidad.MuestraCompleta
import cl.aldemar.musselapp.data.local.entidad.MuestraEntity
import cl.aldemar.musselapp.data.local.entidad.RevisionEntity
import cl.aldemar.musselapp.data.model.EstadoMuestra
import kotlinx.coroutines.flow.Flow

/** Une la muestra con línea, tren, operador y su revisión más reciente. */
private const val SELECT_COMPLETA = """
    SELECT m.*,
           t.centro_id AS centro_id,
           t.codigo AS tren_codigo,
           l.numero AS linea_numero,
           op.nombre AS operador_nombre,
           sup.nombre AS supervisor_nombre,
           r.comentario AS comentario_supervisor
    FROM muestras m
    JOIN lineas l ON l.id = m.linea_id
    JOIN trenes t ON t.id = l.tren_id
    JOIN usuarios op ON op.rut = m.operador_rut
    LEFT JOIN revisiones r ON r.id = (
        SELECT r2.id FROM revisiones r2
        WHERE r2.muestra_id = m.id
        ORDER BY r2.revisada_en DESC, r2.id DESC
        LIMIT 1
    )
    LEFT JOIN usuarios sup ON sup.rut = r.supervisor_rut
"""

/** Muestras y sus revisiones. */
@Dao
interface MuestraDao {

    @Query("$SELECT_COMPLETA ORDER BY m.registrada_en DESC")
    fun observarTodas(): Flow<List<MuestraCompleta>>

    @Query("$SELECT_COMPLETA WHERE m.id = :id")
    fun observarPorId(id: Long): Flow<MuestraCompleta?>

    @Insert
    suspend fun insertar(muestra: MuestraEntity): Long

    @Insert
    suspend fun insertarRevision(revision: RevisionEntity): Long

    @Query("UPDATE muestras SET estado = :estado, sincronizada = 0 WHERE id = :id")
    suspend fun actualizarEstado(id: Long, estado: EstadoMuestra)

    /** Cambia el estado y deja el dictamen en el historial de revisiones, en una sola transacción. */
    @Transaction
    suspend fun registrarRevision(
        muestraId: Long,
        estado: EstadoMuestra,
        comentario: String?,
        supervisorRut: String,
        revisadaEn: Long,
    ) {
        actualizarEstado(muestraId, estado)
        insertarRevision(
            RevisionEntity(
                muestraId = muestraId,
                supervisorRut = supervisorRut,
                estadoAsignado = estado,
                comentario = comentario,
                revisadaEn = revisadaEn,
            ),
        )
    }

    @Query("SELECT * FROM revisiones WHERE muestra_id = :muestraId ORDER BY revisada_en DESC, id DESC")
    fun observarRevisiones(muestraId: Long): Flow<List<RevisionEntity>>

    /** Simula el envío al servidor: marca todo como sincronizado y devuelve cuántas se enviaron. */
    @Query("UPDATE muestras SET sincronizada = 1 WHERE sincronizada = 0")
    suspend fun marcarSincronizadas(): Int

    @Query("SELECT COUNT(*) FROM muestras")
    suspend fun contar(): Int
}
