package com.example.pizzeriaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pizzeriaapp.ui.PantallaVeterinaria
import com.example.pizzeriaapp.ui.VeterinariaViewModel
import com.example.pizzeriaapp.ui.theme.VeterinariaTheme

// Conecta la lógica (VeterinariaViewModel) con la pantalla: observa el estado y reenvía las acciones del usuario
class MainActivity : ComponentActivity() {

    private val viewModel: VeterinariaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VeterinariaTheme {
                val estado = viewModel.estado.observeAsState().value
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (estado != null) {
                        PantallaVeterinaria(
                            encabezado = { Greeting() },
                            estado = estado,
                            onConfirmarPedido = { nombre, correo, telefono, direccion, cantidades ->
                                viewModel.confirmarPedido(nombre, correo, telefono, direccion, cantidades)
                            },
                            onRegistrarConsulta = { mascota -> viewModel.registrarConsulta(mascota, "Control de rutina") },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
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
