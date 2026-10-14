package cl.aldemar.musselapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import cl.aldemar.musselapp.data.local.dao.CatalogoDao
import cl.aldemar.musselapp.data.local.dao.MuestraDao
import cl.aldemar.musselapp.data.local.dao.UsuarioDao
import cl.aldemar.musselapp.data.local.entidad.CentroEntity
import cl.aldemar.musselapp.data.local.entidad.LineaEntity
import cl.aldemar.musselapp.data.local.entidad.MuestraEntity
import cl.aldemar.musselapp.data.local.entidad.RevisionEntity
import cl.aldemar.musselapp.data.local.entidad.TrenEntity
import cl.aldemar.musselapp.data.local.entidad.UsuarioEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Base de datos local SQLite de MusselApp (Room). */
@Database(
    entities = [
        CentroEntity::class,
        TrenEntity::class,
        LineaEntity::class,
        UsuarioEntity::class,
        MuestraEntity::class,
        RevisionEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class MusselDatabase : RoomDatabase() {

    abstract fun catalogoDao(): CatalogoDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun muestraDao(): MuestraDao

    companion object {
        const val NOMBRE = "musselapp.db"

        /**
         * Crea la base en el almacenamiento de la app. La primera vez que se crea
         * se cargan los datos ficticios de [DatosIniciales] en segundo plano.
         */
        fun crear(context: Context, alcance: CoroutineScope): MusselDatabase {
            lateinit var instancia: MusselDatabase
            instancia = Room.databaseBuilder(context.applicationContext, MusselDatabase::class.java, NOMBRE)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        alcance.launch { DatosIniciales.poblar(instancia) }
                    }
                })
                // En desarrollo: si cambia el esquema se recrea la base (se pierden los datos de prueba)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
            return instancia
        }
    }
}
