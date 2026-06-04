package com.example.taller4

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            CrudApp()
        }
    }
}

private data class MockItem(
    val id: Int,
    val title: String,
    val subtitle: String,
    val active: Boolean,
)

private sealed class ScreenState {
    data object List : ScreenState()
    data object Create : ScreenState()
    data class Edit(val itemId: Int) : ScreenState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrudApp() {
    // Paleta minimalista: tonos neutros y un acento suave.
    // Minimal design: neutral tones with a subtle blue accent.
    val customColors: ColorScheme = lightColorScheme(
        primary = Color(0xFF121212), // very dark neutral
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFF5F7FA), // light neutral container
        onPrimaryContainer = Color(0xFF111111),
        secondary = Color(0xFF5661E8), // muted blue accent
        onSecondary = Color(0xFFFFFFFF),
        background = Color(0xFFF8F9FA),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF111827),
        error = Color(0xFFB00020),
        onError = Color(0xFFFFFFFF),
    )

    // Aplicamos el tema con la paleta minimalista.
    MaterialTheme(colorScheme = customColors) {
        // `LocalContext.current` se usa para acceder al Context nativo de Android
        // y mostrar `Toast.makeText(...)` (feedback nativo solicitado en el taller).
        val context = LocalContext.current
        val items = remember {
            mutableStateListOf(
                MockItem(1, "Producto Alpha", "Elemento base de demostración", true),
                MockItem(2, "Producto Beta", "Segundo elemento con estado inactivo", false),
                MockItem(3, "Producto Gamma", "Registro de ejemplo para el taller", true),
            )
        }
        var screenState by remember { mutableStateOf<ScreenState>(ScreenState.List) }
        var nextId by remember { mutableIntStateOf(4) }
        var itemToDelete by remember { mutableStateOf<MockItem?>(null) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        // Barra superior con título según la pantalla.
                        Text(
                            text = when (screenState) {
                                ScreenState.List -> "Mock CRUD"
                                ScreenState.Create -> "Crear elemento"
                                is ScreenState.Edit -> "Editar elemento"
                            },
                            fontWeight = FontWeight.SemiBold,
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
            },
            floatingActionButton = {
                if (screenState == ScreenState.List) {
                    FloatingActionButton(onClick = { screenState = ScreenState.Create }) {
                        Text(text = "+", style = MaterialTheme.typography.headlineMedium)
                    }
                }
            },
        ) { innerPadding ->
            when (val currentScreen = screenState) {
                ScreenState.List -> {
                    ListScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        items = items,
                        onItemClick = { item ->
                            screenState = ScreenState.Edit(item.id)
                        },
                        onDeleteClick = { item ->
                            itemToDelete = item
                        },
                    )
                }

                ScreenState.Create -> {
                    ItemFormScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        title = "",
                        subtitle = "",
                        active = true,
                        onSave = { formTitle, formSubtitle, formActive ->
                            items.add(
                                MockItem(
                                    id = nextId++,
                                    title = formTitle,
                                    subtitle = formSubtitle,
                                    active = formActive,
                                ),
                            )
                            // Toast nativo de Android para feedback (Create)
                            Toast.makeText(context, "Elemento creado correctamente", Toast.LENGTH_SHORT).show()
                            screenState = ScreenState.List
                        },
                        onCancel = {
                            screenState = ScreenState.List
                        },
                    )
                }

                is ScreenState.Edit -> {
                    val item = items.firstOrNull { it.id == currentScreen.itemId }

                    if (item == null) {
                        LaunchedEffect(currentScreen.itemId) {
                            Toast.makeText(context, "El elemento ya no existe", Toast.LENGTH_SHORT).show()
                            screenState = ScreenState.List
                        }
                    } else {
                        ItemFormScreen(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            title = item.title,
                            subtitle = item.subtitle,
                            active = item.active,
                            onSave = { formTitle, formSubtitle, formActive ->
                                val index = items.indexOfFirst { it.id == item.id }
                                if (index >= 0) {
                                    items[index] = item.copy(
                                        title = formTitle,
                                        subtitle = formSubtitle,
                                        active = formActive,
                                    )
                                    // Toast nativo de Android para feedback (Update)
                                    Toast.makeText(context, "Elemento actualizado correctamente", Toast.LENGTH_SHORT).show()
                                }
                                screenState = ScreenState.List
                            },
                            onCancel = {
                                screenState = ScreenState.List
                            },
                        )
                    }
                }
            }

            if (itemToDelete != null) {
                // AlertDialog para confirmación de eliminación (Delete).
                // Aquí usamos Compose AlertDialog para pedir confirmación antes de eliminar.
                AlertDialog(
                    onDismissRequest = { itemToDelete = null },
                    title = { Text("Eliminar elemento") },
                    text = {
                        Text("¿Deseas eliminar \"${itemToDelete?.title.orEmpty()}\"?")
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val target = itemToDelete
                                if (target != null) {
                                    items.removeAll { it.id == target.id }
                                    // LocalContext.current y Toast.makeText para el Toast nativo de Android.
                                    // Uso de Toast nativo para feedback (Delete)
                                    Toast.makeText(context, "Elemento eliminado correctamente", Toast.LENGTH_SHORT).show()
                                }
                                itemToDelete = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError,
                            ),
                        ) {
                            Text("Eliminar")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { itemToDelete = null }) {
                            Text("Cancelar")
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun ListScreen(
    modifier: Modifier = Modifier,
    items: List<MockItem>,
    onItemClick: (MockItem) -> Unit,
    onDeleteClick: (MockItem) -> Unit,
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "Listado de elementos",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Toca un elemento para editarlo o elimina uno desde la tarjeta.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(16.dp))

        // LazyColumn para el listado.
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(items, key = { it.id }) { item ->
                ItemCard(
                    item = item,
                    onClick = { onItemClick(item) },
                    onDeleteClick = { onDeleteClick(item) },
                )
            }
        }
    }
}

@Composable
private fun ItemCard(
    item: MockItem,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = item.title.firstOrNull()?.uppercaseChar()?.toString().orEmpty().ifBlank { "A" },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (item.active) "Activo" else "Inactivo",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (item.active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Button(
                onClick = onDeleteClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text("Eliminar")
            }
        }
    }
}

@Composable
private fun ItemFormScreen(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    active: Boolean,
    onSave: (String, String, Boolean) -> Unit,
    onCancel: () -> Unit,
) {
    var localTitle by remember(title) { mutableStateOf(title) }
    var localSubtitle by remember(subtitle) { mutableStateOf(subtitle) }
    var localActive by remember(active) { mutableStateOf(active) }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // OutlinedTextField y Switch para el formulario.
                OutlinedTextField(
                    value = localTitle,
                    onValueChange = { localTitle = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Título") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = localSubtitle,
                    onValueChange = { localSubtitle = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Subtítulo") },
                    minLines = 2,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Estado activo",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = if (localActive) "El elemento está habilitado" else "El elemento está deshabilitado",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = localActive,
                        onCheckedChange = { localActive = it },
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = {
                            val sanitizedTitle = localTitle.trim()
                            val sanitizedSubtitle = localSubtitle.trim()
                            if (sanitizedTitle.isNotEmpty() && sanitizedSubtitle.isNotEmpty()) {
                                onSave(sanitizedTitle, sanitizedSubtitle, localActive)
                            }
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    CrudApp()
}