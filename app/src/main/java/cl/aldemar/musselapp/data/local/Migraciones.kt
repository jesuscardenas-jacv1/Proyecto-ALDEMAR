package cl.aldemar.musselapp.data.local

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import cl.aldemar.musselapp.util.HashPin

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

    /**
     * v2 → v3: el PIN deja de guardarse en texto plano. Cada PIN existente se reemplaza
     * por su hash (PBKDF2). Las tablas no cambian: solo los datos de la columna `pin`.
     */
    val DE_2_A_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            val pendientes = mutableListOf<Pair<String, String>>()
            db.query("SELECT rut, pin FROM usuarios").use { c ->
                while (c.moveToNext()) {
                    val rut = c.getString(0)
                    val pin = c.getString(1)
                    // Un PIN vacío no permite ingresar de todos modos: se deja tal cual
                    if (pin.isNotEmpty() && !HashPin.esHash(pin)) pendientes += rut to pin
                }
            }
            pendientes.forEach { (rut, pin) ->
                val valores = ContentValues().apply { put("pin", HashPin.generar(pin)) }
                db.update("usuarios", SQLiteDatabase.CONFLICT_NONE, valores, "rut = ?", arrayOf(rut))
            }
        }
    }

    val TODAS = arrayOf(DE_1_A_2, DE_2_A_3)
}
