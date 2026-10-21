package cl.aldemar.musselapp

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cl.aldemar.musselapp.data.local.Migraciones
import cl.aldemar.musselapp.data.local.MusselDatabase
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * La migración v1 → v2 convierte Observado en Corregido sin perder la muestra.
 * Usa los esquemas exportados en app/schemas (1.json y 2.json).
 */
@RunWith(AndroidJUnit4::class)
class MigracionesTest {

    private val nombre = "migracion-test.db"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        MusselDatabase::class.java,
    )

    @Test
    fun de1a2_observadoPasaACorregido() {
        // Base en versión 1 con una muestra observada y su revisión
        helper.createDatabase(nombre, 1).apply {
            execSQL("INSERT INTO centros (id, nombre) VALUES ('dalcahue', 'Centro Dalcahue')")
            execSQL("INSERT INTO trenes (id, centro_id, codigo) VALUES (1, 'dalcahue', 'A')")
            execSQL("INSERT INTO lineas (id, tren_id, numero) VALUES (1, 1, 4)")
            execSQL("INSERT INTO usuarios (rut, nombre, rol, pin) VALUES ('111111111', 'Carla Vera', 'OPERARIO', '1234')")
            execSQL("INSERT INTO usuarios (rut, nombre, rol, pin) VALUES ('222222222', 'Diego Muñoz', 'SUPERVISOR', '1234')")
            execSQL(
                "INSERT INTO muestras (id, linea_id, operador_rut, profundidad_m, talla_mm, conteo_ind_m, " +
                    "observacion, estado, registrada_en, sincronizada, foto_ruta) " +
                    "VALUES (1, 1, '111111111', 5, 60, 300, 'Densidad baja', 'OBSERVADO', 0, 0, NULL)",
            )
            execSQL(
                "INSERT INTO muestras (id, linea_id, operador_rut, profundidad_m, talla_mm, conteo_ind_m, " +
                    "observacion, estado, registrada_en, sincronizada, foto_ruta) " +
                    "VALUES (2, 1, '111111111', 5, 70, 420, '', 'VALIDADO', 0, 0, NULL)",
            )
            execSQL(
                "INSERT INTO revisiones (id, muestra_id, supervisor_rut, estado_asignado, comentario, revisada_en) " +
                    "VALUES (1, 1, '222222222', 'OBSERVADO', 'Repetir conteo', 0)",
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(nombre, 2, true, Migraciones.DE_1_A_2)

        db.query("SELECT estado FROM muestras ORDER BY id").use { c ->
            c.moveToNext(); assertEquals("CORREGIDO", c.getString(0))
            c.moveToNext(); assertEquals("VALIDADO", c.getString(0)) // las demás no cambian
        }
        db.query("SELECT estado_asignado, comentario FROM revisiones").use { c ->
            c.moveToNext()
            assertEquals("CORREGIDO", c.getString(0))
            assertEquals("Repetir conteo", c.getString(1))
        }
    }
}
