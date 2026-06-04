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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.taller4.net.ApiPost
import com.example.taller4.net.JsonPlaceholderApi
import kotlinx.coroutines.launch

/**
 * Módulo 1: Comunicación de red con JSONPlaceholder.
 *
 * - GET: obtiene un post por id.
 * - PUT: actualiza el post de forma simulada en la API de pruebas.
 * - JSON: el parsing se hace dentro de `JsonPlaceholderApi`.
 * - Compose: el estado visual se actualiza con `remember` y recomposición.
 * - Loading: mientras `loading == true`, se bloquean botones/campos y se muestra progreso.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiRestScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var postIdText by remember { mutableStateOf("") }
    var titleText by remember { mutableStateOf("") }
    var bodyText by remember { mutableStateOf("") }
    var statusText by remember { mutableStateOf("Esperando consulta") }
    var errorText by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var postLoaded by remember { mutableStateOf(false) }

    fun setError(message: String) {
        errorText = message
        statusText = message
    }

    AppTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("API REST") },
                    actions = {
                        TextButton(onClick = onBack, enabled = !loading) { Text("Volver") }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "JSONPlaceholder /posts/{id}",
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
                        OutlinedTextField(
                            value = postIdText,
                            onValueChange = { if (!loading) postIdText = it.filter { ch -> ch.isDigit() } },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("ID del post") },
                            singleLine = true,
                            enabled = !loading,
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = {
                                    val id = postIdText.trim().toIntOrNull()
                                    if (id == null || id <= 0) {
                                        setError("ID vacío o inválido")
                                        return@Button
                                    }

                                    scope.launch {
                                        loading = true
                                        errorText = null
                                        statusText = "Consultando..."
                                        postLoaded = false

                                        val result = JsonPlaceholderApi.getPost(id)
                                        result.fold(
                                            onSuccess = { post ->
                                                postIdText = post.id.toString()
                                                titleText = post.title
                                                bodyText = post.body
                                                postLoaded = true
                                                statusText = "Post obtenido correctamente"
                                            },
                                            onFailure = {
                                                setError("Error de conexión")
                                            }
                                        )
                                        loading = false
                                    }
                                },
                                enabled = !loading,
                            ) { Text("Obtener") }

                            Button(
                                onClick = {
                                    val id = postIdText.trim().toIntOrNull()
                                    if (id == null || id <= 0) {
                                        setError("ID vacío o inválido")
                                        return@Button
                                    }
                                    if (titleText.isBlank() || bodyText.isBlank()) {
                                        setError("Title y body no pueden estar vacíos")
                                        return@Button
                                    }

                                    scope.launch {
                                        loading = true
                                        errorText = null
                                        statusText = "Actualizando..."

                                        val result = JsonPlaceholderApi.updatePost(
                                            ApiPost(id = id, title = titleText, body = bodyText)
                                        )
                                        result.fold(
                                            onSuccess = {
                                                statusText = "Actualización exitosa"
                                                Toast.makeText(context, "Actualización exitosa", Toast.LENGTH_SHORT).show()
                                            },
                                            onFailure = {
                                                setError("Error de conexión")
                                            }
                                        )
                                        loading = false
                                    }
                                },
                                enabled = !loading && postLoaded,
                            ) { Text("Actualizar") }
                        }

                        if (loading) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                CircularProgressIndicator()
                                Text("Cargando...")
                            }
                        }

                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )

                        if (errorText != null) {
                            Text(
                                text = errorText.orEmpty(),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }

                        OutlinedTextField(
                            value = titleText,
                            onValueChange = { if (!loading) titleText = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Title") },
                            enabled = !loading && postLoaded,
                        )

                        OutlinedTextField(
                            value = bodyText,
                            onValueChange = { if (!loading) bodyText = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Body") },
                            minLines = 4,
                            enabled = !loading && postLoaded,
                        )

                        // Eliminado el botón interno "Volver al CRUD"; el TopAppBar contiene el botón "Volver".
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun ApiRestScreenPreview() {
    ApiRestScreen(onBack = {})
}

