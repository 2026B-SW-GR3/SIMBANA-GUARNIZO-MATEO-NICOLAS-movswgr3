package com.example.taller5

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

// Pantalla principal de la app con TabRow para navegar entre Intents salientes y entrantes.
@Composable
fun IntentsApp(
    thumbnail: ImageBitmap?,
    onDialClick: (phoneNumber: String) -> Unit,
    onCapturePhoto: () -> Unit,
    selectedTab: Int,
    onTabChange: (Int) -> Unit,
    incomingText: String?,
    incomingImage: ImageBitmap?,
    incomingStatus: String,
    savedGossips: List<GossipItem>,
    onSaveGossip: () -> Unit,
    onClearReceived: () -> Unit,
    onDeleteGossip: (id: Int) -> Unit,
) {
    var phoneNumber by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comunicación Inter-App") },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // TabRow para navegar entre Salientes y Entrantes
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { onTabChange(0) },
                    text = { Text("Salientes") },
                )

                Tab(
                    selected = selectedTab == 1,
                    onClick = { onTabChange(1) },
                    text = { Text("Entrantes") },
                )
            }

            // Contenido de la pestaña seleccionada
            when (selectedTab) {
                0 -> OutgoingScreen(
                    thumbnail = thumbnail,
                    phoneNumber = phoneNumber,
                    onPhoneNumberChange = { phoneNumber = it },
                    onDialClick = onDialClick,
                    onCapturePhoto = onCapturePhoto,
                )

                1 -> IncomingScreen(
                    incomingText = incomingText,
                    incomingImage = incomingImage,
                    incomingStatus = incomingStatus,
                    savedGossips = savedGossips,
                    onSaveGossip = onSaveGossip,
                    onClearReceived = onClearReceived,
                    onDeleteGossip = onDeleteGossip,
                )
            }
        }
    }
}

// Pantalla de Intents salientes (Llamador Misterioso y Foto Express).
// Este es el contenido de la pestaña "Salientes".
@Composable
fun OutgoingScreen(
    thumbnail: ImageBitmap?,
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    onDialClick: (phoneNumber: String) -> Unit,
    onCapturePhoto: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Llamador Misterioso",
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = "Abre el marcador nativo con el número digitado, sin realizar la llamada directamente.",
                    style = MaterialTheme.typography.bodyMedium,
                )

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = onPhoneNumberChange,
                    label = { Text("Teléfono") },
                    placeholder = { Text("Escribir número") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                Button(
                    onClick = { onDialClick(phoneNumber) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Iniciar Dial")
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Foto Express",
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = "Toma una foto con la cámara nativa y muestra la miniatura capturada.",
                    style = MaterialTheme.typography.bodyMedium,
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(16.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (thumbnail != null) {
                        Image(
                            bitmap = thumbnail,
                            contentDescription = "Miniatura capturada",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Text("Sin foto capturada")
                    }
                }

                Button(
                    onClick = onCapturePhoto,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Tomar Foto")
                }
            }
        }
    }
}


