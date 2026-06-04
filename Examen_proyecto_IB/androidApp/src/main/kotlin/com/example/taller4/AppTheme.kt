package com.example.taller4

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Tema compartido para toda la app.
 *
 * Se usa la misma paleta minimalista del Mock-CRUD para que:
 * - CRUD
 * - API REST
 * - Secretos
 * tengan una apariencia consistente.
 */
private val AppColors: ColorScheme = lightColorScheme(
    primary = Color(0xFF121212),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFF5F7FA),
    onPrimaryContainer = Color(0xFF111111),
    secondary = Color(0xFF5661E8),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFFF8F9FA),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111827),
    error = Color(0xFFB00020),
    onError = Color(0xFFFFFFFF),
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = AppColors, content = content)
}

