package cl.aldemar.musselapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.aldemar.musselapp.ui.login.LoginRoute
import cl.aldemar.musselapp.ui.login.LoginViewModel
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
    var usuario by rememberSaveable { mutableStateOf<String?>(null) }

    if (usuario == null) {
        LoginRoute(
            onLoginExitoso = { usuario = "${it.nombre} · ${it.rol.etiqueta}" },
            viewModel = loginViewModel,
        )
    } else {
        // Temporal: se reemplaza por la Pantalla Principal (feature/home)
        SesionIniciada(
            descripcion = usuario!!,
            onCerrarSesion = {
                loginViewModel.onCerrarSesion()
                usuario = null
            },
        )
    }
}

@Composable
private fun SesionIniciada(descripcion: String, onCerrarSesion: () -> Unit) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        ) {
            Text("Sesión iniciada", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(descripcion, style = MaterialTheme.typography.bodyLarge)
            Button(onClick = onCerrarSesion) { Text("Cerrar sesión") }
        }
    }
}
