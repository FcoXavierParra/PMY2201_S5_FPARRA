package com.example.pizzeriaapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.livedata.observeAsState
import com.example.pizzeriaapp.modelo.Producto
import com.example.pizzeriaapp.utils.formatearPesos
import com.example.pizzeriaapp.utils.formatearTelefono
import com.example.pizzeriaapp.utils.validarCorreo

@Composable
fun PantallaVeterinaria(
    encabezado: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VeterinariaViewModel = viewModel()
) {
    val estado: EstadoVeterinaria = viewModel.estado.observeAsState().value ?: return

    var nombre by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var telefono by rememberSaveable { mutableStateOf("") }
    var direccion by rememberSaveable { mutableStateOf("") }
    val cantidades = remember { mutableStateMapOf<Producto, Int>() }

    val formularioCompleto = nombre.isNotBlank() && validarCorreo(correo) != null &&
        formatearTelefono(telefono) != null && direccion.isNotBlank()
    val hayProductos = cantidades.values.sum() > 0

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { encabezado() }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Resumen", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                    Text("Clientes registrados: ${estado.resumen.totalClientes}")
                    Text("Pedidos realizados: ${estado.resumen.totalPedidos}")
                    Text("Último cliente registrado: ${estado.resumen.ultimoCliente ?: "—"}")
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Titulo("Nuevo pedido")
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre del dueño") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = correo, onValueChange = { correo = it }, label = { Text("Correo") }, modifier = Modifier.fillMaxWidth(), singleLine = true, isError = correo.isNotBlank() && validarCorreo(correo) == null)
                OutlinedTextField(value = telefono, onValueChange = { telefono = it }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth(), singleLine = true, isError = telefono.isNotBlank() && formatearTelefono(telefono) == null)
                OutlinedTextField(value = direccion, onValueChange = { direccion = it }, label = { Text("Dirección") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        }

        items(estado.stock.keys.toList(), key = { it.nombre }) { producto ->
            FilaProducto(
                producto = producto,
                stock = estado.stock[producto] ?: 0,
                cantidad = cantidades[producto] ?: 0,
                alCambiar = { nueva -> cantidades[producto] = nueva.coerceIn(0, 10) }
            )
        }

        item {
            Column {
                Button(
                    onClick = {
                        viewModel.confirmarPedido(nombre, correo, telefono, direccion, cantidades.toMap())
                    },
                    enabled = formularioCompleto && hayProductos,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Confirmar pedido") }
                estado.mensaje?.let { Text(it, modifier = Modifier.padding(top = 8.dp)) }
            }
        }

        item { Titulo("Dueños (${estado.clientes.size})") }
        items(estado.clientes, key = { it.correo }) { cliente ->
            Text("${cliente.nombre} · ${cliente.correo} · ${cliente.telefono}")
        }

        item { Titulo("Mascotas (${estado.mascotas.size})") }
        items(estado.mascotas, key = { "${it.nombre}-${it.dueno.correo}" }) { mascota ->
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("${mascota.nombre} (${mascota.especie}) · dueño: ${mascota.dueno.nombre}", modifier = Modifier.padding(end = 8.dp))
                OutlinedButton(onClick = { viewModel.registrarConsulta(mascota, "Control de rutina") }) {
                    Text("Registrar consulta")
                }
            }
        }

        item { Titulo("Consultas (${estado.consultas.size})") }
        items(estado.consultas, key = { "${it.fecha}-${it.mascota.nombre}-${it.motivo}" }) { consulta ->
            Text(consulta.toString())
        }

        item { Titulo("Pedidos despachados (${estado.pedidos.size})") }
        items(estado.pedidos, key = { it.id }) { pedido ->
            Text("#${pedido.id} · ${pedido.cliente.nombre} · ${pedido.fecha} · ${formatearPesos(pedido.calcularTotal().total)}")
        }
    }
}

@Composable
private fun Titulo(texto: String) {
    Column {
        Spacer(Modifier.height(8.dp))
        Text(texto, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
        HorizontalDivider()
    }
}

@Composable
private fun FilaProducto(producto: Producto, stock: Int, cantidad: Int, alCambiar: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.padding(end = 8.dp)) {
            Text(producto.nombre)
            Text("${formatearPesos(producto.precio)} · stock $stock", style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
        }
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            TextButton(onClick = { alCambiar(cantidad - 1) }, enabled = cantidad > 0) { Text("−") }
            Text("$cantidad")
            TextButton(onClick = { alCambiar(cantidad + 1) }, enabled = cantidad < stock) { Text("+") }
        }
    }
}
