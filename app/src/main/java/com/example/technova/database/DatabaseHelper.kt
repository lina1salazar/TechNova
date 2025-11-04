package com.example.technova.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "usuariosdb"
        private const val DATABASE_VERSION = 1

        const val TABLE_NAME = "usuarios"
        const val COLUMN_ID = "id"
        const val COLUMN_NOMBRE = "nombre"
        const val COLUMN_CORREO = "correo"
        const val COLUMN_CONTRASENA = "contrasena"

        private const val CREATE_TABLE = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NOMBRE TEXT NOT NULL,
                $COLUMN_CORREO TEXT NOT NULL UNIQUE,
                $COLUMN_CONTRASENA TEXT NOT NULL
            )
        """
    }
    //Se ejecuta solo la primera vez que se crea la base de datos
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }
}