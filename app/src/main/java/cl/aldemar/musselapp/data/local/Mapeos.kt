package cl.aldemar.musselapp.data.local

import cl.aldemar.musselapp.data.local.entidad.MuestraCompleta
import cl.aldemar.musselapp.data.local.entidad.UsuarioEntity
import cl.aldemar.musselapp.data.model.Muestra
import cl.aldemar.musselapp.data.model.Usuario
import cl.aldemar.musselapp.util.RutValidator

// Conversión de las filas de Room a los modelos que usan las pantallas.

fun MuestraCompleta.aModelo() = Muestra(
    id = muestra.id,
    centroId = centroId,
    linea = lineaNumero,
    tren = trenCodigo,
    profundidadM = muestra.profundidadM,
    tallaMm = muestra.tallaMm,
    conteoIndM = muestra.conteoIndM,
    observacion = muestra.observacion,
    estado = muestra.estado,
    registradaEn = muestra.registradaEn,
    sincronizada = muestra.sincronizada,
    operador = operadorNombre,
    operadorRut = muestra.operadorRut,
    revisadaPor = supervisorNombre,
    comentarioSupervisor = comentarioSupervisor,
    fotoRuta = muestra.fotoRuta,
)

fun UsuarioEntity.aModelo() = Usuario(
    rut = RutValidator.formatear(rut),
    nombre = nombre,
    rol = rol,
)
