package cl.aldemar.musselapp.data.local.entidad

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import cl.aldemar.musselapp.data.model.EstadoMuestra

/**
 * Muestra registrada en terreno (tabla `muestras`).
 * La foto no se guarda en la base: se guarda como archivo privado de la app y aquí solo su ruta.
 */
@Entity(
    tableName = "muestras",
    foreignKeys = [
        ForeignKey(
            entity = LineaEntity::class,
            parentColumns = ["id"],
            childColumns = ["linea_id"],
            onDelete = ForeignKey.RESTRICT,
        ),
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["rut"],
            childColumns = ["operador_rut"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("linea_id"), Index("operador_rut"), Index("estado"), Index("registrada_en")],
)
data class MuestraEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "linea_id") val lineaId: Long,
    @ColumnInfo(name = "operador_rut") val operadorRut: String,
    @ColumnInfo(name = "profundidad_m") val profundidadM: Int,
    @ColumnInfo(name = "talla_mm") val tallaMm: Int,
    @ColumnInfo(name = "conteo_ind_m") val conteoIndM: Int,
    val observacion: String,
    val estado: EstadoMuestra,
    @ColumnInfo(name = "registrada_en") val registradaEn: Long,
    /** false mientras la muestra no se envía al servidor (sincronización futura). */
    val sincronizada: Boolean,
    @ColumnInfo(name = "foto_ruta") val fotoRuta: String?,
)
