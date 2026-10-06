package cl.aldemar.musselapp

import cl.aldemar.musselapp.data.model.Rol
import cl.aldemar.musselapp.data.repository.FakeAuthRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeAuthRepositoryTest {

    private val repository = FakeAuthRepository(latenciaMs = 0)

    @Test
    fun credencialesCorrectas_devuelveUsuario() = runBlocking {
        val resultado = repository.login("11.111.111-1", "1234", Rol.OPERARIO)
        assertEquals(Rol.OPERARIO, resultado.getOrThrow().rol)
    }

    @Test
    fun contrasenaIncorrecta_falla() = runBlocking {
        val resultado = repository.login("11.111.111-1", "0000", Rol.OPERARIO)
        assertTrue(resultado.isFailure)
    }

    @Test
    fun rolDistinto_falla() = runBlocking {
        val resultado = repository.login("11.111.111-1", "1234", Rol.SUPERVISOR)
        assertTrue(resultado.isFailure)
    }
}
