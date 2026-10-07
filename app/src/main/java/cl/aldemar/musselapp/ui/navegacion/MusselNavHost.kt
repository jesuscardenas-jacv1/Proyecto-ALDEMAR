package cl.aldemar.musselapp.ui.navegacion

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.aldemar.musselapp.ui.login.LoginRoute
import cl.aldemar.musselapp.ui.login.LoginViewModel
import cl.aldemar.musselapp.ui.principal.PrincipalRoute

/**
 * Grafo de navegación de la app.
 *
 * Para integrar una pantalla nueva, reemplazar la PantallaEnConstruccion de su ruta
 * por la pantalla real, sin cambiar el nombre de la ruta.
 */
@Composable
fun MusselNavHost(
    navController: NavHostController = rememberNavController(),
    loginViewModel: LoginViewModel = viewModel(),
) {
    val loginState by loginViewModel.uiState.collectAsStateWithLifecycle()
    val usuario = loginState.usuario

    // La sesión decide entre Login y el resto de la app
    LaunchedEffect(usuario != null) {
        val rutaActual = navController.currentDestination?.route
        if (usuario != null && rutaActual == Rutas.LOGIN) {
            navController.navigate(Rutas.PRINCIPAL) {
                popUpTo(Rutas.LOGIN) { inclusive = true }
            }
        } else if (usuario == null && rutaActual != null && rutaActual != Rutas.LOGIN) {
            navController.navigate(Rutas.LOGIN) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

    // Íconos de la barra de estado: oscuros sobre el fondo claro del Login,
    // claros sobre las barras superiores oscuras del resto de las pantallas
    val rutaVisible = navController.currentBackStackEntryAsState().value?.destination?.route
    val view = LocalView.current
    if (!LocalInspectionMode.current) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                rutaVisible == null || rutaVisible == Rutas.LOGIN
        }
    }

    fun volver() {
        navController.popBackStack()
    }

    NavHost(navController = navController, startDestination = Rutas.LOGIN) {

        composable(Rutas.LOGIN) {
            LoginRoute(viewModel = loginViewModel)
        }

        composable(Rutas.PRINCIPAL) {
            val sesion = usuario ?: return@composable
            PrincipalRoute(
                usuario = sesion,
                centro = loginState.centro,
                onCerrarSesion = loginViewModel::onCerrarSesion,
                onNavegar = { destino -> navController.navigate(destino.ruta) { launchSingleTop = true } },
                onVerMuestra = { id -> navController.navigate(Rutas.detalle(id)) },
            )
        }

        // ── Registro de muestra (Jesús Cárdenas) ──────────────────────────
        composable(Rutas.MUESTRA) {
            PantallaEnConstruccion(
                titulo = "Nueva Muestra",
                objetivo = "Formulario para iniciar un muestreo: centro, tren, línea, fecha, hora y tramo.",
                responsable = "Jesús Cárdenas",
                onVolver = ::volver,
                accion = AccionProvisoria("Continuar a Captura") { navController.navigate(Rutas.CAPTURA) },
            )
        }

        composable(Rutas.CAPTURA) {
            PantallaEnConstruccion(
                titulo = "Captura",
                objetivo = "Fotografiar la muestra o seleccionar una imagen del dispositivo.",
                responsable = "Jesús Cárdenas",
                onVolver = ::volver,
                accion = AccionProvisoria("Continuar a Vista previa") { navController.navigate(Rutas.VISTA_PREVIA) },
            )
        }

        composable(Rutas.VISTA_PREVIA) {
            PantallaEnConstruccion(
                titulo = "Vista previa",
                objetivo = "Revisar la fotografía y los datos del formulario antes de confirmar el muestreo.",
                responsable = "Jesús Cárdenas",
                onVolver = ::volver,
                accion = AccionProvisoria("Confirmar y volver al inicio") {
                    navController.popBackStack(Rutas.PRINCIPAL, inclusive = false)
                },
            )
        }

        // ── Consulta (Ramón Osorio) ───────────────────────────────────────
        composable(Rutas.HISTORIAL) {
            PantallaEnConstruccion(
                titulo = "Historial",
                objetivo = "Listado de las muestras registradas, con filtros por centro, línea, fecha o estado.",
                responsable = "Ramón Osorio",
                onVolver = ::volver,
                accion = AccionProvisoria("Ver detalle de ejemplo") { navController.navigate(Rutas.detalle(10)) },
            )
        }

        // ── Detalle y revisión (Lucas Saldivia) ───────────────────────────
        composable(
            route = Rutas.DETALLE,
            arguments = listOf(navArgument(Rutas.ARG_MUESTRA_ID) { type = NavType.LongType }),
        ) { entrada ->
            val muestraId = entrada.arguments?.getLong(Rutas.ARG_MUESTRA_ID) ?: 0L
            PantallaEnConstruccion(
                titulo = "Detalle de muestra #$muestraId",
                objetivo = "Ver en detalle los datos ingresados de la muestra seleccionada.",
                responsable = "Lucas Saldivia",
                onVolver = ::volver,
            )
        }

        composable(Rutas.REVISION) {
            PantallaEnConstruccion(
                titulo = "Revisión del Supervisor",
                objetivo = "Cambiar el estado de las muestras (Pendiente, Observado, Corregido, Validado) y agregar comentarios técnicos.",
                responsable = "Lucas Saldivia",
                onVolver = ::volver,
            )
        }
    }
}
