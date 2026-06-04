package com.example.taller4.storage

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.taller4.MockItem

/**
 * Implementación simple usando SQLiteOpenHelper.
 * Archivo: SqlRepository.kt
 * - Implementa create/read/update/delete usando SQLite local en Android.
 * - Mantener simple y didáctico para el taller.
 */
class SqlRepository(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION), Repository {
    companion object {
        private const val DB_NAME = "taller_local.db"
        private const val DB_VERSION = 1
        private const val TABLE = "items"
        private const val COL_ID = "id"
        private const val COL_TITLE = "title"
        private const val COL_SUBTITLE = "subtitle"
        private const val COL_ACTIVE = "active"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val sql = """
            CREATE TABLE $TABLE (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TITLE TEXT NOT NULL,
                $COL_SUBTITLE TEXT NOT NULL,
                $COL_ACTIVE INTEGER NOT NULL
            )
        """.trimIndent()
        db.execSQL(sql)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Para el taller no implementamos migraciones complejas.
        db.execSQL("DROP TABLE IF EXISTS $TABLE")
        onCreate(db)
    }

    override fun getAll(): List<MockItem> {
        val list = mutableListOf<MockItem>()
        val db = readableDatabase
        val cursor: Cursor = db.query(TABLE, null, null, null, null, null, "$COL_ID ASC")
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndexOrThrow(COL_ID))
                val title = it.getString(it.getColumnIndexOrThrow(COL_TITLE))
                val subtitle = it.getString(it.getColumnIndexOrThrow(COL_SUBTITLE))
                val active = it.getInt(it.getColumnIndexOrThrow(COL_ACTIVE)) == 1
                list.add(MockItem(id, title, subtitle, active))
            }
        }
        return list
    }

    override fun insert(item: MockItem): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_TITLE, item.title)
            put(COL_SUBTITLE, item.subtitle)
            put(COL_ACTIVE, if (item.active) 1 else 0)
        }
        val id = db.insert(TABLE, null, values)
        return id.toInt()
    }

    override fun update(item: MockItem) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_TITLE, item.title)
            put(COL_SUBTITLE, item.subtitle)
            put(COL_ACTIVE, if (item.active) 1 else 0)
        }
        db.update(TABLE, values, "$COL_ID = ?", arrayOf(item.id.toString()))
    }

    override fun delete(id: Int) {
        val db = writableDatabase
        db.delete(TABLE, "$COL_ID = ?", arrayOf(id.toString()))
    }
}

