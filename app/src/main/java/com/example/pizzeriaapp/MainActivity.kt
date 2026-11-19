package com.example.pizzeriaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pizzeriaapp.ui.PantallaVeterinaria
import com.example.pizzeriaapp.ui.theme.VeterinariaTheme

// Entrada de la app: solo conecta el tema, el encabezado y la pantalla del módulo ui
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VeterinariaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PantallaVeterinaria(
                        encabezado = { Greeting() },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

// Mensaje de bienvenida (texto en res/values/strings.xml)
@Composable
fun Greeting(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Text(
            text = stringResource(R.string.mensaje_bienvenida),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(text = stringResource(R.string.nombre_veterinaria), style = MaterialTheme.typography.titleMedium)
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    VeterinariaTheme {
        Greeting()
    }
}
