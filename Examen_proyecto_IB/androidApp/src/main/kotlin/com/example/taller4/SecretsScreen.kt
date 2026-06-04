package com.example.taller4

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.taller4.secrets.SecretStore
import kotlinx.coroutines.launch

/**
 * Módulo 3: Almacenamiento seguro y configuración.
 *
 * Se usan tres mecanismos nativos:
 * - SharedPreferences: simple y síncrono.
 * - DataStore: moderno y asíncrono.
 * - EncryptedSharedPreferences: para información sensible, ya cifrada automáticamente.
 *
 * No implementamos Android Keystore directamente porque EncryptedSharedPreferences
 * lo usa internamente para administrar la clave maestra de forma transparente.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecretsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val store = remember { SecretStore(context) }
    val scrollState = rememberScrollState()

    // Separamos los campos para que el flujo académico quede claro:
    // - Crear/Actualizar: llave + valor.
    // - Obtener: solo llave.
    var saveKeyText by remember { mutableStateOf("") }
    var valueText by remember { mutableStateOf("") }
    var retrieveKeyText by remember { mutableStateOf("") }
    var recoveredValue by remember { mutableStateOf<String?>(null) }
    var statusText by remember { mutableStateOf("Sin valor recuperado") }
    var selectedBackend by remember { mutableStateOf(SecretStore.Backend.SharedPreferences) }

    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    AppTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Secretos") },
                    actions = {
                        TextButton(onClick = onBack) { Text("Volver") }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Gestión de secretos y configuración",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = "Mecanismo de almacenamiento",
                            fontWeight = FontWeight.SemiBold,
                        )
                        SecretStore.Backend.entries.forEach { backend ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = selectedBackend == backend,
                                    onClick = { selectedBackend = backend },
                                )
                                Text(text = backend.label)
                            }
                        }

                        Text(
                            text = "Crear / Actualizar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        OutlinedTextField(
                            value = saveKeyText,
                            onValueChange = { saveKeyText = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Llave") },
                            singleLine = true,
                        )
                        OutlinedTextField(
                            value = valueText,
                            onValueChange = { valueText = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Valor") },
                            minLines = 2,
                        )

                        Button(onClick = {
                            val key = saveKeyText.trim()
                            val value = valueText.trim()
                            if (key.isEmpty()) {
                                statusText = "La llave es obligatoria"
                                showToast("La llave es obligatoria")
                                return@Button
                            }
                            if (value.isEmpty()) {
                                statusText = "El valor es obligatorio"
                                showToast("El valor es obligatorio")
                                return@Button
                            }

                            scope.launch {
                                // Se selecciona el mecanismo activo mediante el RadioButton.
                                // La UI no mezcla datos entre SharedPreferences, DataStore y EncryptedSharedPreferences.
                                when (selectedBackend) {
                                    SecretStore.Backend.SharedPreferences -> {
                                        store.saveSync(key, value, selectedBackend)
                                    }
                                    SecretStore.Backend.EncryptedSharedPreferences -> {
                                        store.saveSync(key, value, selectedBackend)
                                    }
                                    SecretStore.Backend.DataStore -> {
                                        store.saveDataStore(key, value)
                                    }
                                }
                                recoveredValue = null
                                statusText = "Dato guardado correctamente"
                                showToast("Dato guardado correctamente")
                            }
                        }, modifier = Modifier.fillMaxWidth()) { Text("Guardar") }

                        Text(
                            text = "Obtener",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        OutlinedTextField(
                            value = retrieveKeyText,
                            onValueChange = { retrieveKeyText = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Llave") },
                            singleLine = true,
                        )

                        Button(onClick = {
                            val key = retrieveKeyText.trim()
                            if (key.isEmpty()) {
                                statusText = "La llave es obligatoria"
                                showToast("La llave es obligatoria")
                                return@Button
                            }

                            scope.launch {
                                // Lectura según el mecanismo seleccionado.
                                val value = store.getValue(key, selectedBackend)
                                if (value == null) {
                                    recoveredValue = null
                                    statusText = "Secreto no encontrado"
                                    showToast("Secreto no encontrado")
                                } else {
                                    recoveredValue = value
                                    statusText = "Secreto recuperado correctamente"
                                    showToast("Secreto recuperado correctamente")
                                }
                            }
                        }, modifier = Modifier.fillMaxWidth()) { Text("Recuperar") }

                        // Botón único para limpiar; el botón "Volver" está en la TopAppBar.
                        OutlinedButton(
                            onClick = {
                                saveKeyText = ""
                                valueText = ""
                                retrieveKeyText = ""
                                recoveredValue = null
                                statusText = "Sin valor recuperado"
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Limpiar") }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Resultado", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = statusText,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Valor recuperado", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(recoveredValue ?: "Sin valor recuperado")
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun SecretsScreenPreview() {
    SecretsScreen(onBack = {})
}

