package cl.aldemar.musselapp.data.local.entidad

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import cl.aldemar.musselapp.data.model.EstadoMuestra

/**
 * Dictamen del supervisor sobre una muestra (tabla `revisiones`).
 * Se guarda cada revisión, no solo la última, para mantener la trazabilidad.
 */
@Entity(
    tableName = "revisiones",
    foreignKeys = [
        ForeignKey(
            entity = MuestraEntity::class,
            parentColumns = ["id"],
            childColumns = ["muestra_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["rut"],
            childColumns = ["supervisor_rut"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("muestra_id"), Index("supervisor_rut")],
)
data class RevisionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "muestra_id") val muestraId: Long,
    @ColumnInfo(name = "supervisor_rut") val supervisorRut: String,
    @ColumnInfo(name = "estado_asignado") val estadoAsignado: EstadoMuestra,
    val comentario: String?,
    @ColumnInfo(name = "revisada_en") val revisadaEn: Long,
)
