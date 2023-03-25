package com.github.ljts42.hw5_1ch_with_mem

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class MessagesDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "messages.db"

        private const val TABLE_NAME = "messages"
        private const val KEY_ID = "id"
        private const val KEY_FROM = "user"
        private const val KEY_TO = "channel"
        private const val KEY_TYPE = "type"
        private const val KEY_DATA = "data"
        private const val KEY_TIME = "time"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val SQL_CREATE_ENTRIES =
            ("CREATE TABLE $TABLE_NAME("
                    + "$KEY_ID INTEGER PRIMARY KEY, "
                    + "$KEY_FROM TEXT, "
                    + "$KEY_TO TEXT, "
                    + "$KEY_TYPE INTEGER, "
                    + "$KEY_DATA TEXT, "
                    + "$KEY_TIME TEXT) ")
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun addMessage(message: Message) {
        val db = this.writableDatabase

        val values = ContentValues().apply {
            put(KEY_ID, message.id.toInt())
            put(KEY_FROM, message.from)
            put(KEY_TO, message.to)
            put(KEY_TYPE, message.type.ordinal)
            put(KEY_DATA, message.data)
            put(KEY_TIME, message.time)
        }
        try {
            db.insert(TABLE_NAME, null, values)
        } catch (e: SQLiteException) {
            Log.e("DatabaseHelper", "Error adding message to database", e)
        } finally {
            db.close()
        }
    }

    fun getMessages(to: String): List<Message> {
        val messages = mutableListOf<Message>()
        val selectQuery = ("SELECT * FROM $TABLE_NAME WHERE $KEY_TO = ? ORDER BY $KEY_ID")
        val args = arrayOf(to)
        val db = this.readableDatabase
        val cursor: Cursor
        try {
            cursor = db.rawQuery(selectQuery, args)
        } catch (e: SQLiteException) {
            Log.e("DatabaseHelper", "Error querying messages from database", e)
            return messages
        }
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ID))
                val from = cursor.getString(cursor.getColumnIndexOrThrow(KEY_FROM))
                val type =
                    MessageType.values()[cursor.getInt(cursor.getColumnIndexOrThrow(KEY_TYPE))]
                val data = cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATA))
                val time = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TIME))

                val message = Message(id, from, to, type, data, time)
                messages.add(message)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return messages
    }
}