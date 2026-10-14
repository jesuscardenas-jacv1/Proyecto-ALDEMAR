package cl.aldemar.musselapp

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.aldemar.musselapp.data.local.DatosIniciales
import cl.aldemar.musselapp.data.local.MusselDatabase
import cl.aldemar.musselapp.data.model.EstadoMuestra
import cl.aldemar.musselapp.data.model.Muestra
import cl.aldemar.musselapp.data.model.Rol
import cl.aldemar.musselapp.data.repository.RoomAuthRepository
import cl.aldemar.musselapp.data.repository.RoomMuestraRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RepositoriosRoomTest {

    private lateinit var db: MusselDatabase
    private lateinit var alcance: CoroutineScope
    private lateinit var repositorio: RoomMuestraRepository

    @Before
    fun preparar() = runBlocking {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), MusselDatabase::class.java).build()
        DatosIniciales.poblar(db)
        alcance = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        repositorio = RoomMuestraRepository(db, alcance, latenciaSincronizacionMs = 0)
    }

    @After
    fun cerrar() = runBlocking {
        // Esperar a que termine la consulta en vivo del repositorio antes de cerrar la base;
        // si no, una consulta en curso puede ejecutarse con la base cerrada y botar el proceso.
        alcance.coroutineContext.job.cancelAndJoin()
        db.close()
    }

    @Test
    fun agregarMuestra_quedaGuardadaConIdGeneradoYPendiente() = runBlocking {
        val antes = db.muestraDao().contar()
        repositorio.agregarMuestra(
            Muestra(
                id = 777, centroId = "quellon", linea = 5, tren = "C", profundidadM = 4, tallaMm = 64,
                conteoIndM = 415, observacion = "Desde la app", estado = EstadoMuestra.PENDIENTE,
                registradaEn = System.currentTimeMillis(), sincronizada = false,
                operador = "Carla Vera", operadorRut = "11.111.111-1", fotoRuta = "fotos/x.jpg",
            ),
        )
        assertEquals(antes + 1, db.muestraDao().contar())
        val nueva = db.muestraDao().observarTodas().first().first().muestra
        assertTrue(nueva.id != 777L)
        assertEquals("fotos/x.jpg", nueva.fotoRuta)
        assertEquals(false, nueva.sincronizada)
    }

    @Test
    fun actualizarEstado_porNombreDelSupervisor_guardaDictamen() = runBlocking {
        val id = db.muestraDao().observarTodas().first().first { it.muestra.estado == EstadoMuestra.PENDIENTE }.muestra.id
        repositorio.actualizarEstado(id, EstadoMuestra.VALIDADO, "Conforme", revisadaPor = "Diego Muñoz")
        val muestra = repositorio.obtenerPorId(id).first()!!
        assertEquals(EstadoMuestra.VALIDADO, muestra.estado)
        assertEquals("Diego Muñoz", muestra.revisadaPor)
        assertEquals("Conforme", muestra.comentarioSupervisor)
    }

    @Test
    fun sincronizar_marcaLasPendientes() = runBlocking {
        assertTrue(repositorio.sincronizar() > 0)
        assertEquals(0, repositorio.sincronizar())
    }

    @Test
    fun login_contraLaTablaUsuarios() = runBlocking {
        val auth = RoomAuthRepository(db.usuarioDao(), latenciaMs = 0)
        assertEquals("Carla Vera", auth.login("11.111.111-1", "1234", Rol.OPERARIO).getOrThrow().nombre)
        assertTrue(auth.login("11.111.111-1", "0000", Rol.OPERARIO).isFailure)
        assertTrue(auth.login("11.111.111-1", "1234", Rol.SUPERVISOR).isFailure)
        assertTrue(auth.login("99.999.999-9", "1234", Rol.OPERARIO).isFailure)
    }
}
