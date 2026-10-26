package cl.aldemar.musselapp.data.local

import androidx.room.withTransaction
import cl.aldemar.musselapp.data.local.entidad.CentroEntity
import cl.aldemar.musselapp.data.local.entidad.LineaEntity
import cl.aldemar.musselapp.data.local.entidad.MuestraEntity
import cl.aldemar.musselapp.data.local.entidad.RevisionEntity
import cl.aldemar.musselapp.data.local.entidad.TrenEntity
import cl.aldemar.musselapp.data.local.entidad.UsuarioEntity
import cl.aldemar.musselapp.data.model.CentroCultivo
import cl.aldemar.musselapp.data.model.Rol
import cl.aldemar.musselapp.data.repository.FakeMuestraRepository
import cl.aldemar.musselapp.util.HashPin

/** Datos ficticios que se cargan al crear la base por primera vez (caso académico). */
object DatosIniciales {

    val TRENES = listOf("A", "B", "C")
    const val LINEAS_POR_TREN = 12

    const val RUT_OPERARIO = "111111111"
    const val RUT_SUPERVISOR = "222222222"

    /** PIN de prueba de ambos usuarios; en la base solo se guarda su hash. */
    const val PIN_PRUEBA = "1234"

    fun usuarios() = listOf(
        UsuarioEntity(rut = RUT_OPERARIO, nombre = "Carla Vera", rol = Rol.OPERARIO, pin = HashPin.generar(PIN_PRUEBA)),
        UsuarioEntity(rut = RUT_SUPERVISOR, nombre = "Diego Muñoz", rol = Rol.SUPERVISOR, pin = HashPin.generar(PIN_PRUEBA)),
    )

    suspend fun poblar(db: MusselDatabase, ahora: Long = System.currentTimeMillis()) = db.withTransaction {
        val catalogo = db.catalogoDao()

        catalogo.insertarCentros(CentroCultivo.disponibles.map { CentroEntity(it.id, it.nombre) })
        CentroCultivo.disponibles.forEach { centro ->
            TRENES.forEach { codigo ->
                val trenId = catalogo.insertarTren(TrenEntity(centroId = centro.id, codigo = codigo))
                catalogo.insertarLineas((1..LINEAS_POR_TREN).map { LineaEntity(trenId = trenId, numero = it) })
            }
        }

        db.usuarioDao().insertar(usuarios())

        // Las mismas muestras de prueba que usa FakeMuestraRepository, ahora persistidas
        val muestraDao = db.muestraDao()
        FakeMuestraRepository.muestrasDePrueba(ahora).sortedBy { it.registradaEn }.forEach { m ->
            val lineaId = catalogo.buscarLineaId(m.centroId, m.tren, m.linea) ?: return@forEach
            val id = muestraDao.insertar(
                MuestraEntity(
                    lineaId = lineaId,
                    operadorRut = RUT_OPERARIO,
                    profundidadM = m.profundidadM,
                    tallaMm = m.tallaMm,
                    conteoIndM = m.conteoIndM,
                    observacion = m.observacion,
                    estado = m.estado,
                    registradaEn = m.registradaEn,
                    sincronizada = m.sincronizada,
                    fotoRuta = m.fotoRuta,
                ),
            )
            if (m.revisadaPor != null) {
                muestraDao.insertarRevision(
                    RevisionEntity(
                        muestraId = id,
                        supervisorRut = RUT_SUPERVISOR,
                        estadoAsignado = m.estado,
                        comentario = m.comentarioSupervisor,
                        revisadaEn = m.revisadaEn ?: m.registradaEn,
                    ),
                )
            }
        }
    }
}
