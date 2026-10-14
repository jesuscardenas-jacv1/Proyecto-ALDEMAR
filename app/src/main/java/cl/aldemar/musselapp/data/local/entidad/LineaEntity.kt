package cl.aldemar.musselapp.data.local.entidad

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Línea de siembra de un tren (tabla `lineas`). */
@Entity(
    tableName = "lineas",
    foreignKeys = [
        ForeignKey(
            entity = TrenEntity::class,
            parentColumns = ["id"],
            childColumns = ["tren_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["tren_id", "numero"], unique = true)],
)
data class LineaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "tren_id") val trenId: Long,
    val numero: Int,
)
