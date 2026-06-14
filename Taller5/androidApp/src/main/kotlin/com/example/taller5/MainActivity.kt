package com.example.taller5

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import java.io.InputStream

// MainActivity es la actividad principal de la app.
// Procesa tanto Intents salientes (cámara, marcador) como Intents entrantes (texto, imágenes compartidas).
class MainActivity : ComponentActivity() {

    // Estado para Intents salientes (captura de foto)
    private var capturedThumbnail by mutableStateOf<Bitmap?>(null)
    private lateinit var takePictureLauncher: ActivityResultLauncher<Intent>

    // Estado para Intents entrantes
    private var selectedTab by mutableStateOf(0)
    private var incomingText by mutableStateOf<String?>(null)
    private var incomingImageUri by mutableStateOf<Uri?>(null)
    private var incomingImageBitmap by mutableStateOf<Bitmap?>(null)
    private var incomingStatus by mutableStateOf("Esperando datos externos...")
    private var savedGossips by mutableStateOf<List<GossipItem>>(emptyList())
    private lateinit var gossipRepository: GossipRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Inicializar el repositorio de chismes (SQLite)
        try {
            gossipRepository = GossipRepository(this)
            loadGossips()
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error initializing GossipRepository: ${e.message}")
            Toast.makeText(this, "Error al cargar BD local", Toast.LENGTH_SHORT).show()
            savedGossips = emptyList()
        }

        // Aquí se configura el Activity Result para la cámara con StartActivityForResult.
        takePictureLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                // La miniatura viene en extras bajo la clave "data" como Bitmap.
                // IMPORTANTE: Esta es una miniatura de baja resolución (típicamente 160x120).
                // Para capturar la imagen en resolución alta, se debe especificar un Uri
                // en el Intent con la clave MediaStore.EXTRA_OUTPUT y guardar en archivo.
                @Suppress("DEPRECATION")
                val bitmap = result.data?.extras?.get("data") as? Bitmap
                if (bitmap != null) {
                    // Actualizamos el estado de Compose para mostrar la foto capturada.
                    capturedThumbnail = bitmap
                } else {
                    Toast.makeText(
                        this,
                        "No se pudo obtener la miniatura",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            } else {
                Toast.makeText(this, "Captura cancelada", Toast.LENGTH_SHORT).show()
            }
        }

        // Procesar el Intent inicial si la app se abre desde otra app compartiendo datos.
        // Esto ocurre cuando el usuario abre la app por primera vez desde "Compartir".
        handleIncomingIntent(intent)

        setContent {
            IntentsApp(
                // Intents salientes
                thumbnail = capturedThumbnail?.asImageBitmap(),
                onDialClick = { phoneNumber ->
                    handleDialIntent(phoneNumber)
                },
                onCapturePhoto = {
                    handleCameraIntent()
                },

                // Intents entrantes
                selectedTab = selectedTab,
                onTabChange = { newTab ->
                    selectedTab = newTab
                },
                incomingText = incomingText,
                incomingImage = incomingImageBitmap?.asImageBitmap(),
                incomingStatus = incomingStatus,
                savedGossips = savedGossips,
                onSaveGossip = {
                    handleSaveGossip()
                },
                onClearReceived = {
                    incomingText = null
                    incomingImageUri = null
                    incomingImageBitmap = null
                    incomingStatus = "Esperando datos externos..."
                },
                onDeleteGossip = { id ->
                    handleDeleteGossip(id)
                },
            )
        }
    }

    // Cuando la app ya está abierta y se comparte datos desde otra app,
    // se llama a onNewIntent en lugar de onCreate.
    // Esto es configurado con android:launchMode="singleTop" en AndroidManifest.xml.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Procesar el nuevo Intent recibido
        handleIncomingIntent(intent)
    }

    // Función auxiliar que procesa los Intents entrantes.
    // Detecta si es texto, imagen u otro tipo de contenido.
    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return

        // Verificar que el Intent sea de tipo ACTION_SEND (compartir).
        // ACTION_SEND es la acción que dispara el menú de "Compartir" en Android.
        if (intent.action == Intent.ACTION_SEND) {
            val mimeType = intent.type
            when {
                mimeType == "text/plain" -> {
                    val text = intent.getStringExtra(Intent.EXTRA_TEXT)
                    if (!text.isNullOrEmpty()) {
                        incomingText = text
                        incomingImageUri = null
                        incomingImageBitmap = null
                        incomingStatus = "Texto recibido"
                        // Mostrar automáticamente la pestaña "Entrantes"
                        selectedTab = 1
                    }
                }
                mimeType?.startsWith("image/") == true -> {
                    @Suppress("DEPRECATION")
                    val imageUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                    if (imageUri != null) {
                        incomingText = null
                        incomingImageUri = imageUri
                        incomingImageBitmap = loadBitmapFromUri(imageUri)
                        incomingStatus = "Imagen recibida"
                        // Mostrar automáticamente la pestaña "Entrantes"
                        selectedTab = 1
                    }
                }

                // Si no es texto ni imagen, mostrar error
                else -> {
                    incomingText = null
                    incomingImageUri = null
                    incomingImageBitmap = null
                    incomingStatus = "No se pudo procesar el contenido"
                    selectedTab = 1
                }
            }
        }
    }

    // Procesar Intent de marcador (ACTION_DIAL)
    private fun handleDialIntent(phoneNumber: String) {
        val phone = phoneNumber.trim()

        if (phone.isNotEmpty()) {
            // Aquí se crea el Intent con ACTION_DIAL para abrir el marcador nativo.
            // Usamos Uri.parse("tel:$phone") para enviar el número con el esquema tel:
            // requerido por Android para que el marcador reconozca el dato como teléfono.
            val dialIntent = Intent(
                Intent.ACTION_DIAL,
                Uri.parse("tel:$phone"),
            )

            // Antes de lanzar el Intent, verificamos con resolveActivity(packageManager)
            // que exista una app compatible que pueda manejar el marcador.
            if (dialIntent.resolveActivity(packageManager) != null) {
                startActivity(dialIntent)
            } else {
                Toast.makeText(
                    this,
                    "No hay aplicación disponible para realizar esta acción",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        } else {
            Toast.makeText(
                this,
                "Debe ingresar un número telefónico",
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    // Procesar Intent de cámara (MediaStore.ACTION_IMAGE_CAPTURE)
    private fun handleCameraIntent() {
        // Lanzamos el Intent nativo de cámara con MediaStore.ACTION_IMAGE_CAPTURE.
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // Verificamos que exista una app de cámara antes de intentar abrirla.
        if (cameraIntent.resolveActivity(packageManager) != null) {
            takePictureLauncher.launch(cameraIntent)
        } else {
            Toast.makeText(
                this,
                "No hay aplicación de cámara disponible",
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    // Cargar la imagen desde un Uri recibido por Intent.
    // Usa try/catch para evitar crasheos si el Uri es inválido.
    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            if (inputStream != null) {
                BitmapFactory.decodeStream(inputStream).also {
                    inputStream.close()
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al cargar imagen: ${e.message}", Toast.LENGTH_SHORT)
                .show()
            null
        }
    }

    // Guardar el texto recibido como un "chisme" en SQLite.
    private fun handleSaveGossip() {
        if (incomingText.isNullOrEmpty()) {
            Toast.makeText(this, "No hay texto para guardar", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // Verificar si el chisme ya existe en la base de datos.
            if (gossipRepository.textExists(incomingText!!)) {
                Toast.makeText(this, "Este chisme ya fue guardado", Toast.LENGTH_SHORT).show()
                return
            }

            // Insertar el chisme en SQLite
            val id = gossipRepository.insertGossip(incomingText!!)
            if (id > 0) {
                Toast.makeText(this, "Chisme guardado correctamente", Toast.LENGTH_SHORT).show()
                loadGossips()
                // Limpiar el texto recibido después de guardar
                incomingText = null
                incomingStatus = "Esperando datos externos..."
            } else {
                Toast.makeText(this, "Error al guardar chisme", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error saving gossip: ${e.message}")
            Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Eliminar un chisme de SQLite por su ID.
    private fun handleDeleteGossip(id: Int) {
        if (gossipRepository.deleteGossip(id)) {
            Toast.makeText(this, "Chisme eliminado", Toast.LENGTH_SHORT).show()
            loadGossips()
        } else {
            Toast.makeText(this, "Error al eliminar chisme", Toast.LENGTH_SHORT).show()
        }
    }

    // Cargar todos los chismes guardados desde SQLite.
    private fun loadGossips() {
        try {
            savedGossips = gossipRepository.getAllGossips()
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error loading gossips: ${e.message}")
            savedGossips = emptyList()
        }
    }
}

@Preview
@Composable
fun IntentsAppPreview() {
    IntentsApp(
        thumbnail = null,
        onDialClick = {},
        onCapturePhoto = {},
        selectedTab = 0,
        onTabChange = {},
        incomingText = null,
        incomingImage = null,
        incomingStatus = "Esperando datos externos...",
        savedGossips = emptyList(),
        onSaveGossip = {},
        onClearReceived = {},
        onDeleteGossip = {},
    )
}

