package cl.aldemar.musselapp.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.aldemar.musselapp.ui.theme.MusselAppTheme

@Composable
fun CaptureScreen(
    onTomarFoto: () -> Unit = {},
    onElegirGaleria: () -> Unit = {},
    onContinuar: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Captura de muestra",
            style = MaterialTheme.typography.headlineSmall
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .border(1.5.dp, Color.Gray, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("Aún no hay foto")
        }

        Button(
            onClick = onTomarFoto,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Tomar foto")
        }

        OutlinedButton(
            onClick = onElegirGaleria,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Elegir de la galería")
        }

        Button(
            onClick = onContinuar,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Continuar")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CaptureScreenPreview() {
    MusselAppTheme {
        CaptureScreen()
    }
}