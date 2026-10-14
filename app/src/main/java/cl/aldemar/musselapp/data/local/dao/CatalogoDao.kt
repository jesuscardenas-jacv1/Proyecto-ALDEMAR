package cl.aldemar.musselapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.aldemar.musselapp.data.local.entidad.CentroEntity
import cl.aldemar.musselapp.data.local.entidad.LineaEntity
import cl.aldemar.musselapp.data.local.entidad.TrenEntity
import kotlinx.coroutines.flow.Flow

/** Centros, trenes y líneas: la estructura física del cultivo. */
@Dao
interface CatalogoDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarCentros(centros: List<CentroEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarTren(tren: TrenEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarLineas(lineas: List<LineaEntity>)

    @Query("SELECT * FROM centros ORDER BY nombre")
    fun observarCentros(): Flow<List<CentroEntity>>

    @Query("SELECT * FROM trenes WHERE centro_id = :centroId ORDER BY codigo")
    fun observarTrenes(centroId: String): Flow<List<TrenEntity>>

    @Query("SELECT * FROM lineas WHERE tren_id = :trenId ORDER BY numero")
    fun observarLineas(trenId: Long): Flow<List<LineaEntity>>

    /** Busca la línea a partir de lo que elige el operario en el formulario. */
    @Query(
        """
        SELECT l.id FROM lineas l
        JOIN trenes t ON t.id = l.tren_id
        WHERE t.centro_id = :centroId AND t.codigo = :trenCodigo AND l.numero = :numero
        """,
    )
    suspend fun buscarLineaId(centroId: String, trenCodigo: String, numero: Int): Long?
}
