package cl.aldemar.musselapp.ui.componentes

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.aldemar.musselapp.R
import cl.aldemar.musselapp.data.model.EstadoMuestra
import cl.aldemar.musselapp.data.model.Rol
import cl.aldemar.musselapp.data.model.Usuario
import cl.aldemar.musselapp.ui.principal.Destino
import cl.aldemar.musselapp.ui.theme.EstadoCorregidoFondo
import cl.aldemar.musselapp.ui.theme.EstadoCorregidoTexto
import cl.aldemar.musselapp.ui.theme.EstadoObservadoFondo
import cl.aldemar.musselapp.ui.theme.EstadoObservadoTexto
import cl.aldemar.musselapp.ui.theme.EstadoPendienteFondo
import cl.aldemar.musselapp.ui.theme.EstadoPendienteTexto
import cl.aldemar.musselapp.ui.theme.EstadoValidadoFondo
import cl.aldemar.musselapp.ui.theme.EstadoValidadoTexto

// Componentes compartidos por las pantallas con sesión iniciada (Principal, Historial, ...)

/** Barra superior oscura común a las pantallas con sesión iniciada. */
@Composable
fun BarraSuperiorMussel(
    seccion: String,
    usuario: Usuario,
    pendientesSincronizar: Int,
    sincronizando: Boolean,
    onSincronizar: () -> Unit,
    onCerrarSesion: () -> Unit,
) {
    var menuAbierto by remember { mutableStateOf(false) }
    Surface(color = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                Image(painterResource(R.drawable.logo_musselapp), contentDescription = "Logo MusselApp", modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("MusselApp", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        " • $seccion",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (pendientesSincronizar == 0) Color(0xFF4ADE80) else Color(0xFFFBBF24)),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (pendientesSincronizar == 0) "ONLINE / SINCRONIZADO" else "ONLINE / $pendientesSincronizar EN COLA",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
                    )
                }
            }
            IconButton(onClick = onSincronizar, enabled = !sincronizando) {
                IconoGiratorio(Icons.Filled.Sync, girando = sincronizando, descripcion = "Sincronizar", tinte = MaterialTheme.colorScheme.onPrimary)
            }
            Box {
                IconButton(onClick = { menuAbierto = true }) {
                    Avatar(nombre = usuario.nombre, tamano = 36, claro = true)
                }
                DropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
                    DropdownMenuItem(
                        text = { Text("Cerrar sesión") },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                        onClick = {
                            menuAbierto = false
                            onCerrarSesion()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun IconoGiratorio(icono: ImageVector, girando: Boolean, descripcion: String?, tinte: Color) {
    val giro = rememberInfiniteTransition(label = "giro")
    val angulo by giro.animateFloat(
        initialValue = 0f,
        targetValue = -360f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Restart),
        label = "angulo",
    )
    Icon(
        icono,
        contentDescription = descripcion,
        tint = tinte,
        modifier = Modifier.graphicsLayer { rotationZ = if (girando) angulo else 0f },
    )
}

/** Avatar con las iniciales del usuario (la app no guarda fotos de personas). */
@Composable
fun Avatar(nombre: String, tamano: Int, claro: Boolean) {
    val iniciales = nombre.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
    Box(
        modifier = Modifier
            .size(tamano.dp)
            .clip(CircleShape)
            .background(if (claro) Color.White else MaterialTheme.colorScheme.secondary),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            iniciales,
            color = if (claro) MaterialTheme.colorScheme.primary else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (tamano * 0.38f).sp,
        )
    }
}

/** Barra inferior con el menú de opciones según el rol. */
@Composable
fun NavegacionInferior(rol: Rol, seleccionado: Destino, onNavegar: (Destino) -> Unit) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        Destino.disponiblesPara(rol).forEach { destino ->
            NavigationBarItem(
                selected = destino == seleccionado,
                onClick = { if (destino != seleccionado) onNavegar(destino) },
                icon = { Icon(destino.icono, contentDescription = null) },
                label = { Text(destino.etiqueta) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = EstadoValidadoFondo,
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }
    }
}

/** Botón flotante "+" para registrar una muestra rápida (solo operario). */
@Composable
fun BotonNuevaMuestra(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = RoundedCornerShape(16.dp),
    ) {
        Icon(Icons.Filled.Add, contentDescription = "Registrar muestra rápida")
    }
}

@Composable
fun ChipEstado(estado: EstadoMuestra) {
    val (fondo, texto, icono) = when (estado) {
        EstadoMuestra.VALIDADO -> Triple(EstadoValidadoFondo, EstadoValidadoTexto, Icons.Filled.CheckCircle)
        EstadoMuestra.OBSERVADO -> Triple(EstadoObservadoFondo, EstadoObservadoTexto, Icons.Filled.Warning)
        EstadoMuestra.CORREGIDO -> Triple(EstadoCorregidoFondo, EstadoCorregidoTexto, Icons.Filled.EditNote)
        EstadoMuestra.PENDIENTE -> Triple(EstadoPendienteFondo, EstadoPendienteTexto, Icons.Filled.Schedule)
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(fondo)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icono, contentDescription = null, tint = texto, modifier = Modifier.size(12.dp))
        Spacer(Modifier.width(4.dp))
        Text(estado.etiqueta.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = texto)
    }
}
