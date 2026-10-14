package cl.aldemar.musselapp.data.local.entidad

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Tren de cultivo (longline) de un centro (tabla `trenes`). */
@Entity(
    tableName = "trenes",
    foreignKeys = [
        ForeignKey(
            entity = CentroEntity::class,
            parentColumns = ["id"],
            childColumns = ["centro_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["centro_id", "codigo"], unique = true)],
)
data class TrenEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "centro_id") val centroId: String,
    /** Letra del tren dentro del centro: "A", "B", "C". */
    val codigo: String,
)
