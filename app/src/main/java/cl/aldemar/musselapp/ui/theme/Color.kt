package cl.aldemar.musselapp.ui.theme

import androidx.compose.ui.graphics.Color

// Paleta MusselApp (README · Identidad visual)
val Principal = Color(0xFF0F172A)   // TopAppBar, botones de acción clave
val Secundario = Color(0xFF334155)  // Botones secundarios, navegación, íconos
val Fondo = Color(0xFFF1F5F9)       // Fondo general (evita deslumbramiento en terreno)
val Texto = Color(0xFF000000)       // Títulos y datos de alto contraste
val Adicional = Color(0xFF475569)   // Bordes, divisores y textos secundarios (contraste AA sobre Fondo y campos)

// Tonos de apoyo para superficies (campos y tarjetas)
val Superficie = Color(0xFFFFFFFF)
val SuperficieCampo = Color(0xFFE2E8F0)
val Error = Color(0xFFB91C1C)

// Estados de una muestra (chips del historial y la pantalla principal)
val EstadoValidadoFondo = Color(0xFFDBEAFE)
val EstadoValidadoTexto = Color(0xFF1E3A8A)

// Pendiente y Corregido comparten color para no saturar la pantalla.
// Se distinguen por la etiqueta y el ícono.
val EstadoEnEsperaFondo = Color(0xFFFEF3C7)
val EstadoEnEsperaTexto = Color(0xFF92400E)

// Franja lateral de las tarjetas del Historial según el estado
val AcentoValidado = Color(0xFF16A34A)
val AcentoPendiente = Color(0xFFD97706)

// Alertas que no son un estado (por ejemplo, densidad bajo el mínimo)
val AlertaFondo = Color(0xFFFEE2E2)
val AlertaTexto = Color(0xFFB91C1C)
