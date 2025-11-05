package com.example.technova.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "usuariosdb"
        private const val DATABASE_VERSION = 2

        const val TABLE_USUARIOS = "usuarios"
        const val COLUMN_ID = "id"
        const val COLUMN_NOMBRE = "nombre"
        const val COLUMN_CORREO = "correo"
        const val COLUMN_CONTRASENA = "contrasena"
        const val COLUMN_ES_ADMIN = "es_admin"

        // Products
        const val TABLE_PRODUCTOS = "productos"
        const val COLUMN_PRODUCTO_ID = "id"
        const val COLUMN_PRODUCTO_NOMBRE = "nombre"
        const val COLUMN_PRODUCTO_DESCRIPCION = "descripcion"
        const val COLUMN_PRODUCTO_PRECIO = "precio"
        const val COLUMN_PRODUCTO_STOCK = "stock"
        const val COLUMN_PRODUCTO_IMAGEN_URL = "imagen_url"


        private const val CREATE_TABLE_USUARIOS = """
            CREATE TABLE $TABLE_USUARIOS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NOMBRE TEXT NOT NULL,
                $COLUMN_CORREO TEXT NOT NULL UNIQUE,
                $COLUMN_CONTRASENA TEXT NOT NULL,
                $COLUMN_ES_ADMIN INTEGER DEFAULT 0
            )
        """

        private const val CREATE_TABLE_PRODUCTOS = """
            CREATE TABLE $TABLE_PRODUCTOS (
                $COLUMN_PRODUCTO_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_PRODUCTO_NOMBRE TEXT NOT NULL,
                $COLUMN_PRODUCTO_DESCRIPCION TEXT NOT NULL,
                $COLUMN_PRODUCTO_PRECIO REAL NOT NULL,
                $COLUMN_PRODUCTO_STOCK INTEGER NOT NULL,
                $COLUMN_PRODUCTO_IMAGEN_URL TEXT NOT NULL
                )
        """
    }
    //Se ejecuta solo la primera vez que se crea la base de datos
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_USUARIOS)
        db.execSQL(CREATE_TABLE_PRODUCTOS)

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            try {
                db.execSQL("ALTER TABLE $TABLE_USUARIOS ADD COLUMN $COLUMN_ES_ADMIN INTEGER DEFAULT 0")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                db.execSQL(CREATE_TABLE_PRODUCTOS)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}