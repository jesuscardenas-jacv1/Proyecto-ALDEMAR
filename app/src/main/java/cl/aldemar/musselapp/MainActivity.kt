package cl.aldemar.musselapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cl.aldemar.musselapp.ui.navegacion.MusselNavHost
import cl.aldemar.musselapp.ui.theme.MusselAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MusselAppTheme {
                MusselNavHost()
            }
        }
    }
}
