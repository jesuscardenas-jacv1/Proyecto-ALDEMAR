package cl.aldemar.musselapp.data.local.entidad

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Centro de cultivo de choritos (tabla `centros`). */
@Entity(tableName = "centros")
data class CentroEntity(
    @PrimaryKey val id: String,
    val nombre: String,
)
