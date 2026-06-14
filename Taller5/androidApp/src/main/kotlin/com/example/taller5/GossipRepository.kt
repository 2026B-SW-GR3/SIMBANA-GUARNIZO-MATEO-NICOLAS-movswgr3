package com.example.taller5

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// GossipRepository gestiona la base de datos SQLite local para guardar los textos compartidos.
// Usa SQLiteOpenHelper para crear y gestionar la tabla de chismes.
class GossipRepository(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION,
) {

    companion object {
        private const val DATABASE_NAME = "gossips.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "gossips"
        private const val COLUMN_ID = "id"
        private const val COLUMN_TEXT = "text"
        private const val COLUMN_CREATED_AT = "created_at"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Crear la tabla gossips con los campos necesarios.
        val createTableSQL = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TEXT TEXT NOT NULL,
                $COLUMN_CREATED_AT TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTableSQL)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Por ahora, simplemente eliminar y recrear la tabla.
        // En producción, se haría migración de datos.
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // Obtener todos los chismes guardados de la base de datos.
    // Retorna una lista ordenada por fecha descendente (más recientes primero).
    fun getAllGossips(): List<GossipItem> {
        return try {
            val db = readableDatabase
            val gossips = mutableListOf<GossipItem>()

            val cursor = db.query(
                TABLE_NAME,
                arrayOf(COLUMN_ID, COLUMN_TEXT, COLUMN_CREATED_AT),
                null,
                null,
                null,
                null,
                "$COLUMN_CREATED_AT DESC",
            )

            with(cursor) {
                while (moveToNext()) {
                    try {
                        val id = getInt(getColumnIndexOrThrow(COLUMN_ID))
                        val text = getString(getColumnIndexOrThrow(COLUMN_TEXT))
                        val createdAt = getString(getColumnIndexOrThrow(COLUMN_CREATED_AT))
                        gossips.add(GossipItem(id, text, createdAt))
                    } catch (e: Exception) {
                        android.util.Log.e("GossipRepository", "Error reading gossip: ${e.message}")
                    }
                }
                close()
            }

            gossips
        } catch (e: Exception) {
            android.util.Log.e("GossipRepository", "Error getting gossips: ${e.message}")
            emptyList()
        }
    }

    // Verificar si un texto ya existe en la base de datos.
    // Usado para evitar duplicados al guardar chismes.
    fun textExists(text: String): Boolean {
        return try {
            val db = readableDatabase
            val cursor = db.query(
                TABLE_NAME,
                arrayOf(COLUMN_ID),
                "$COLUMN_TEXT = ?",
                arrayOf(text),
                null,
                null,
                null,
            )

            val exists = cursor.count > 0
            cursor.close()
            exists
        } catch (e: Exception) {
            android.util.Log.e("GossipRepository", "Error checking text existence: ${e.message}")
            false
        }
    }

    // Insertar un nuevo chisme en la base de datos.
    // Retorna el ID del chisme insertado, o -1 si hay error.
    fun insertGossip(text: String): Int {
        return try {
            val db = writableDatabase
            val timestamp = android.text.format.DateFormat.format(
                "yyyy-MM-dd HH:mm:ss",
                java.util.Date(),
            ).toString()

            val values = android.content.ContentValues().apply {
                put(COLUMN_TEXT, text)
                put(COLUMN_CREATED_AT, timestamp)
            }

            val id = db.insert(TABLE_NAME, null, values)
            id.toInt()
        } catch (e: Exception) {
            android.util.Log.e("GossipRepository", "Error inserting gossip: ${e.message}")
            -1
        }
    }

    // Eliminar un chisme de la base de datos por su ID.
    // Retorna true si se eliminó correctamente, false en caso contrario.
    fun deleteGossip(id: Int): Boolean {
        return try {
            val db = writableDatabase
            val rowsDeleted = db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id.toString()))
            rowsDeleted > 0
        } catch (e: Exception) {
            android.util.Log.e("GossipRepository", "Error deleting gossip: ${e.message}")
            false
        }
    }
}

