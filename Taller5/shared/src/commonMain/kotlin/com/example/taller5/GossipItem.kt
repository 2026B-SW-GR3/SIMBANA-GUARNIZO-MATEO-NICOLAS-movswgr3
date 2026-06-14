package com.example.taller5

// Modelo de datos para representar un "chisme" guardado.
// Cada chisme contiene un texto y la fecha/hora en que fue guardado.
data class GossipItem(
    val id: Int,
    val text: String,
    val createdAt: String,
)

