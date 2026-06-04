package com.example.taller4.storage

import android.content.Context
import com.example.taller4.MockItem
import org.json.JSONArray
import org.json.JSONObject

/**
 * Implementación NoSQL simulada usando un archivo JSON en almacenamiento interno.
 * Archivo: NoSqlRepository.kt
 * - Guarda una colección JSON en un archivo privado de la app.
 * - Proporciona getAll/insert/update/delete sobre objetos JSON.
 */
class NoSqlRepository(private val context: Context) : Repository {
    private val fileName = "nosql_store.json"

    private fun readJsonArray(): JSONArray {
        return try {
            val text = context.openFileInput(fileName).bufferedReader().use { it.readText() }
            if (text.isBlank()) JSONArray() else JSONArray(text)
        } catch (_: Exception) {
            JSONArray()
        }
    }

    private fun writeJsonArray(array: JSONArray) {
        context.openFileOutput(fileName, Context.MODE_PRIVATE).use { stream ->
            stream.write(array.toString().toByteArray())
        }
    }

    override fun getAll(): List<MockItem> {
        val arr = readJsonArray()
        val list = mutableListOf<MockItem>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val id = obj.optInt("id")
            val title = obj.optString("title")
            val subtitle = obj.optString("subtitle")
            val active = obj.optBoolean("active")
            list.add(MockItem(id, title, subtitle, active))
        }
        // order by id asc
        return list.sortedBy { it.id }
    }

    override fun insert(item: MockItem): Int {
        val arr = readJsonArray()
        // compute new id: max+1
        var maxId = 0
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            maxId = maxOf(maxId, obj.optInt("id", 0))
        }
        val newId = maxId + 1
        val obj = JSONObject().apply {
            put("id", newId)
            put("title", item.title)
            put("subtitle", item.subtitle)
            put("active", item.active)
        }
        arr.put(obj)
        writeJsonArray(arr)
        return newId
    }

    override fun update(item: MockItem) {
        val arr = readJsonArray()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            if (obj.optInt("id") == item.id) {
                obj.put("title", item.title)
                obj.put("subtitle", item.subtitle)
                obj.put("active", item.active)
                writeJsonArray(arr)
                return
            }
        }
    }

    override fun delete(id: Int) {
        val arr = readJsonArray()
        val newArr = JSONArray()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            if (obj.optInt("id") != id) newArr.put(obj)
        }
        writeJsonArray(newArr)
    }
}

