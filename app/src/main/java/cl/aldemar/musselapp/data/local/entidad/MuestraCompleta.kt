package cl.aldemar.musselapp.data.local.entidad

import androidx.room.ColumnInfo
import androidx.room.Embedded

/**
 * Resultado de la consulta que une la muestra con su línea, tren, operador
 * y la última revisión. Es lo que necesitan las pantallas.
 */
data class MuestraCompleta(
    @Embedded val muestra: MuestraEntity,
    @ColumnInfo(name = "centro_id") val centroId: String,
    @ColumnInfo(name = "tren_codigo") val trenCodigo: String,
    @ColumnInfo(name = "linea_numero") val lineaNumero: Int,
    @ColumnInfo(name = "operador_nombre") val operadorNombre: String,
    @ColumnInfo(name = "supervisor_nombre") val supervisorNombre: String?,
    @ColumnInfo(name = "comentario_supervisor") val comentarioSupervisor: String?,
    @ColumnInfo(name = "revisada_en") val revisadaEn: Long?,
)
