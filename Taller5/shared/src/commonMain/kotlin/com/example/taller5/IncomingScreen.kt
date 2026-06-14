package com.example.taller5

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

// Pantalla para mostrar los Intents entrantes.
// Aquí se muestran textos/imágenes recibidas desde otras apps.
// También se permite guardar textos recibidos como "chismes" en SQLite.
@Composable
fun IncomingScreen(
    incomingText: String?,
    incomingImage: ImageBitmap?,
    incomingStatus: String,
    savedGossips: List<GossipItem>,
    onSaveGossip: () -> Unit,
    onClearReceived: () -> Unit,
    onDeleteGossip: (id: Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Intents entrantes",
            style = MaterialTheme.typography.titleLarge,
        )

        Text(
            text = incomingStatus,
            style = MaterialTheme.typography.bodyMedium,
        )

        // Si se recibió texto desde otra app, se muestra en esta Card.
        if (incomingText != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "Texto recibido",
                        style = MaterialTheme.typography.titleMedium,
                    )

                    Text(
                        text = incomingText,
                        style = MaterialTheme.typography.bodyMedium,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            onClick = onSaveGossip,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Guardar")
                        }

                        OutlinedButton(
                            onClick = onClearReceived,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Limpiar")
                        }
                    }
                }
            }
        }

        // Si se recibió una imagen desde otra app, se muestra en esta Card.
        if (incomingImage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "Imagen recibida",
                        style = MaterialTheme.typography.titleMedium,
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(16.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            bitmap = incomingImage,
                            contentDescription = "Imagen recibida",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    }

                    OutlinedButton(
                        onClick = onClearReceived,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Limpiar imagen")
                    }
                }
            }
        }

        // Sección del historial de chismes guardados en SQLite.
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Chismes guardados",
                    style = MaterialTheme.typography.titleMedium,
                )

                if (savedGossips.isEmpty()) {
                    Text(
                        text = "No hay chismes guardados",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    /*
                     * Se usa Column y no LazyColumn porque esta pantalla ya tiene
                     * verticalScroll. Evitamos scroll anidado, que puede causar
                     * cierres inesperados en Compose.
                     */
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        savedGossips.forEach { gossip ->
                            GossipCard(
                                gossip = gossip,
                                onDeleteGossip = onDeleteGossip,
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// Card individual para cada chisme guardado.
@Composable
fun GossipCard(
    gossip: GossipItem,
    onDeleteGossip: (id: Int) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = gossip.text,
                style = MaterialTheme.typography.bodyMedium,
            )

            Text(
                text = gossip.createdAt,
                style = MaterialTheme.typography.labelSmall,
            )

            OutlinedButton(
                onClick = { onDeleteGossip(gossip.id) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Eliminar")
            }
        }
    }
}