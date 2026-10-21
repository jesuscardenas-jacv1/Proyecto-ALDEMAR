package cl.aldemar.musselapp.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Cambios de la base entre versiones, sin perder las muestras guardadas en el teléfono. */
object Migraciones {

    /**
     * v1 → v2: se elimina el estado Observado. El supervisor ahora valida o corrige,
     * así que las muestras y revisiones que estaban en Observado pasan a Corregido.
     * Las tablas no cambian: solo los datos.
     */
    val DE_1_A_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("UPDATE muestras SET estado = 'CORREGIDO' WHERE estado = 'OBSERVADO'")
            db.execSQL("UPDATE revisiones SET estado_asignado = 'CORREGIDO' WHERE estado_asignado = 'OBSERVADO'")
        }
    }

    val TODAS = arrayOf(DE_1_A_2)
}
