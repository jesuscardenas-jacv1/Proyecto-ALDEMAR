package cl.aldemar.musselapp.ui.login

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Sailing
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.aldemar.musselapp.data.model.Rol
import cl.aldemar.musselapp.ui.theme.MusselAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

private const val DOS_PI = (2 * PI).toFloat()

/**
 * Transición breve al elegir el perfil, antes de pedir las credenciales.
 * Operario: el barco navega sobre las olas. Supervisor: el ícono aparece con pulsos.
 */
@Composable
fun AnimacionPerfil(rol: Rol, onTerminada: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary)
            .semantics { contentDescription = "Ingresando como ${rol.etiqueta}" },
    ) {
        when (rol) {
            Rol.OPERARIO -> EscenaBarco(onTerminada)
            Rol.SUPERVISOR -> EscenaSupervisor(onTerminada)
        }
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                rol.etiqueta,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
            )
            Text(
                if (rol == Rol.OPERARIO) "Zarpando hacia el centro de cultivo…" else "Preparando la revisión…",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
            )
        }
    }
}

@Composable
private fun EscenaBarco(onTerminada: () -> Unit) {
    val alTerminar by rememberUpdatedState(onTerminada)
    val travesia = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        travesia.animateTo(1f, tween(durationMillis = 1700, easing = FastOutSlowInEasing))
        alTerminar()
    }

    val oleaje = rememberInfiniteTransition(label = "oleaje")
    val fase by oleaje.animateFloat(
        initialValue = 0f,
        targetValue = DOS_PI,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing)),
        label = "fase",
    )

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val tamanoBarco = 64.dp
        val lineaAgua = maxHeight * 0.72f
        // El barco entra por la izquierda y sale por la derecha
        val x = -tamanoBarco + (maxWidth + tamanoBarco) * travesia.value
        val vaiven = 4.dp * sin(fase)

        Canvas(Modifier.fillMaxSize()) {
            val base = size.height * 0.72f
            dibujarOla(base - 6.dp.toPx(), 7.dp.toPx(), fase + 1.6f, Color.White.copy(alpha = 0.12f))
            dibujarOla(base + 4.dp.toPx(), 6.dp.toPx(), fase, Color.White.copy(alpha = 0.22f))
        }

        Icon(
            Icons.Filled.Sailing,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .offset(x = x, y = lineaAgua - tamanoBarco + 10.dp + vaiven)
                .size(tamanoBarco)
                .graphicsLayer { rotationZ = 7f * sin(fase + 0.8f) },
        )

        // Ola delantera: tapa la quilla para que el barco parezca flotar
        Canvas(Modifier.fillMaxSize()) {
            dibujarOla(size.height * 0.72f + 12.dp.toPx(), 5.dp.toPx(), fase + 3.2f, Color.White.copy(alpha = 0.35f))
        }
    }
}

/** Ola sinusoidal rellena desde [base] hasta el fondo. */
private fun DrawScope.dibujarOla(base: Float, amplitud: Float, fase: Float, color: Color) {
    val longitud = size.width / 1.5f
    val ola = Path().apply {
        moveTo(0f, size.height)
        var x = 0f
        while (x <= size.width) {
            lineTo(x, base + amplitud * sin(x / longitud * DOS_PI + fase))
            x += 6f
        }
        lineTo(size.width, size.height)
        close()
    }
    drawPath(ola, color)
}

@Composable
private fun EscenaSupervisor(onTerminada: () -> Unit) {
    val alTerminar by rememberUpdatedState(onTerminada)
    val escala = remember { Animatable(0f) }
    val pulso1 = remember { Animatable(0f) }
    val pulso2 = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        launch { escala.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) }
        launch { pulso1.animateTo(1f, tween(1000, easing = LinearOutSlowInEasing)) }
        launch {
            delay(300)
            pulso2.animateTo(1f, tween(1000, easing = LinearOutSlowInEasing))
        }
        delay(1500)
        alTerminar()
    }

    Box(Modifier.fillMaxSize().padding(top = 36.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(150.dp)) {
            listOf(pulso1.value, pulso2.value).forEach { p ->
                if (p > 0f) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.45f * (1f - p)),
                        radius = size.minDimension / 2 * (0.35f + 0.65f * p),
                        center = Offset(size.width / 2, size.height / 2),
                        style = Stroke(width = 3.dp.toPx()),
                    )
                }
            }
        }
        Icon(
            Icons.Filled.FactCheck,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .size(56.dp)
                .graphicsLayer {
                    scaleX = escala.value
                    scaleY = escala.value
                },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AnimacionOperarioPreview() {
    MusselAppTheme { AnimacionPerfil(rol = Rol.OPERARIO, onTerminada = {}) }
}

@Preview(showBackground = true)
@Composable
private fun AnimacionSupervisorPreview() {
    MusselAppTheme { AnimacionPerfil(rol = Rol.SUPERVISOR, onTerminada = {}) }
}
