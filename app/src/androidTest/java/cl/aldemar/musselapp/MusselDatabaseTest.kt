package cl.aldemar.musselapp

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.aldemar.musselapp.data.local.DatosIniciales
import cl.aldemar.musselapp.data.local.MusselDatabase
import cl.aldemar.musselapp.data.local.aModelo
import cl.aldemar.musselapp.data.local.entidad.MuestraEntity
import cl.aldemar.musselapp.data.model.EstadoMuestra
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MusselDatabaseTest {

    private lateinit var db: MusselDatabase

    @Before
    fun crearBase() = runBlocking {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), MusselDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        DatosIniciales.poblar(db)
    }

    @After
    fun cerrarBase() = db.close()

    @Test
    fun datosIniciales_cargaCatalogoUsuariosYMuestras() = runBlocking {
        assertEquals(4, db.catalogoDao().observarCentros().first().size)
        assertNotNull(db.catalogoDao().buscarLineaId("dalcahue", "B", 12))
        assertEquals("Carla Vera", db.usuarioDao().buscarPorRut(DatosIniciales.RUT_OPERARIO)?.nombre)
        assertEquals(14, db.muestraDao().contar())
    }

    @Test
    fun muestraCompleta_traeLineaTrenOperadorYUltimaRevision() = runBlocking {
        val muestras = db.muestraDao().observarTodas().first().map { it.aModelo() }
        assertTrue(muestras.zipWithNext().all { (a, b) -> a.registradaEn >= b.registradaEn })
        val corregida = muestras.first { it.estado == EstadoMuestra.CORREGIDO }
        assertEquals("Carla Vera", corregida.operador)
        assertEquals("Diego Muñoz", corregida.revisadaPor)
        assertNotNull(corregida.comentarioSupervisor)
    }

    @Test
    fun registrarRevision_cambiaEstadoYGuardaHistorial() = runBlocking {
        val dao = db.muestraDao()
        val pendiente = dao.observarTodas().first().first { it.muestra.estado == EstadoMuestra.PENDIENTE }
        val id = pendiente.muestra.id

        dao.registrarRevision(id, EstadoMuestra.CORREGIDO, "Repetir conteo", DatosIniciales.RUT_SUPERVISOR, 1_000L)
        dao.registrarRevision(id, EstadoMuestra.VALIDADO, "Conteo corregido", DatosIniciales.RUT_SUPERVISOR, 2_000L)

        val actualizada = dao.observarPorId(id).first()!!.aModelo()
        assertEquals(EstadoMuestra.VALIDADO, actualizada.estado)
        assertEquals("Conteo corregido", actualizada.comentarioSupervisor)
        assertEquals(2, dao.observarRevisiones(id).first().size)
    }

    @Test
    fun insertarMuestra_generaIdYQuedaPendienteDeSincronizar() = runBlocking {
        val lineaId = db.catalogoDao().buscarLineaId("calbuco", "A", 3)!!
        val id = db.muestraDao().insertar(
            MuestraEntity(
                lineaId = lineaId, operadorRut = DatosIniciales.RUT_OPERARIO, profundidadM = 4,
                tallaMm = 66, conteoIndM = 410, observacion = "Prueba", estado = EstadoMuestra.PENDIENTE,
                registradaEn = System.currentTimeMillis(), sincronizada = false, fotoRuta = "fotos/prueba.jpg",
            ),
        )
        val nueva = db.muestraDao().observarPorId(id).first()!!.aModelo()
        assertEquals("calbuco", nueva.centroId)
        assertEquals("A", nueva.tren)
        assertEquals(3, nueva.linea)
        assertEquals("fotos/prueba.jpg", nueva.fotoRuta)
        assertTrue(db.muestraDao().marcarSincronizadas() >= 1)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun claveForanea_impideMuestraConLineaInexistente() = runBlocking<Unit> {
        db.muestraDao().insertar(
            MuestraEntity(
                lineaId = 99_999, operadorRut = DatosIniciales.RUT_OPERARIO, profundidadM = 4,
                tallaMm = 66, conteoIndM = 410, observacion = "", estado = EstadoMuestra.PENDIENTE,
                registradaEn = 0, sincronizada = false, fotoRuta = null,
            ),
        )
    }
}
