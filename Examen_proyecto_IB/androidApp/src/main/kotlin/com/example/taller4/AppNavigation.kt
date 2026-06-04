package com.example.taller4

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Navegación simple por estado, sin Navigation Compose.
 * - Crud: pantalla principal del CRUD.
 * - ApiRest: módulo de JSONPlaceholder.
 * - Secrets: módulo de almacenamiento seguro/configuración.
 */
sealed class AppScreen {
    data object Crud : AppScreen()
    data object ApiRest : AppScreen()
    data object Secrets : AppScreen()
}

@Composable
fun AppRoot() {
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Crud) }

    when (currentScreen) {
        AppScreen.Crud -> CrudApp(
            onOpenApiRest = { currentScreen = AppScreen.ApiRest },
            onOpenSecrets = { currentScreen = AppScreen.Secrets },
        )

        AppScreen.ApiRest -> ApiRestScreen(onBack = { currentScreen = AppScreen.Crud })
        AppScreen.Secrets -> SecretsScreen(onBack = { currentScreen = AppScreen.Crud })
    }
}

