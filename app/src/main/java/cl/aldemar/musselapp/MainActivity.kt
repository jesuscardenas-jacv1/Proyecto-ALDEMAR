package cl.aldemar.musselapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.aldemar.musselapp.ui.login.LoginRoute
import cl.aldemar.musselapp.ui.login.LoginViewModel
import cl.aldemar.musselapp.ui.principal.PrincipalRoute
import cl.aldemar.musselapp.ui.theme.MusselAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MusselAppTheme {
                MusselApp()
            }
        }
    }
}

@Composable
fun MusselApp() {
    val loginViewModel: LoginViewModel = viewModel()
    val loginState by loginViewModel.uiState.collectAsStateWithLifecycle()
    val usuario = loginState.usuario

    if (usuario == null) {
        LoginRoute(viewModel = loginViewModel)
    } else {
        PrincipalRoute(
            usuario = usuario,
            centro = loginState.centro,
            onCerrarSesion = loginViewModel::onCerrarSesion,
        )
    }
}
