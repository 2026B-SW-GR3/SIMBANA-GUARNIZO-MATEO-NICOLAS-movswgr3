package com.example.taller4.storage

import com.example.taller4.MockItem

/**
 * Interfaz común para los dos modos de persistencia (SQL y NoSQL).
 * Define operaciones CRUD básicas usadas por la UI.
 */
interface Repository {
    // Devuelve todos los elementos del origen.
    fun getAll(): List<MockItem>

    // Inserta un elemento. Retorna el id asignado (si aplica).
    fun insert(item: MockItem): Int

    // Actualiza un elemento existente.
    fun update(item: MockItem)

    // Elimina por id.
    fun delete(id: Int)
}

