package com.example.technova.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "usuariosdb"
        private const val DATABASE_VERSION = 1   // ← Volvemos a la versión 1

        // Usuarios
        const val TABLE_USUARIOS = "usuarios"
        const val COLUMN_ID = "id"
        const val COLUMN_NOMBRE = "nombre"
        const val COLUMN_CORREO = "correo"
        const val COLUMN_CONTRASENA = "contrasena"
        const val COLUMN_ES_ADMIN = "es_admin"

        // Productos
        const val TABLE_PRODUCTOS = "productos"
        const val COLUMN_PRODUCTO_ID = "id"
        const val COLUMN_PRODUCTO_NOMBRE = "nombre"
        const val COLUMN_PRODUCTO_DESCRIPCION = "descripcion"
        const val COLUMN_PRODUCTO_PRECIO = "precio"
        const val COLUMN_PRODUCTO_STOCK = "stock"
        const val COLUMN_PRODUCTO_IMAGEN_URL = "imagen_url"

        // Carrito
        const val TABLE_CARRITO = "carrito"
        const val TABLE_CARRITO_DET = "carrito_detalle"

        // =============================
        //          CREATE TABLES
        // =============================

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

        private const val CREATE_TABLE_CARRITO = """
            CREATE TABLE IF NOT EXISTS $TABLE_CARRITO (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                created_at INTEGER NOT NULL,
                status TEXT NOT NULL CHECK(status IN ('OPEN','PAID','CANCELLED')) DEFAULT 'OPEN',
                FOREIGN KEY(user_id) REFERENCES $TABLE_USUARIOS($COLUMN_ID) ON DELETE CASCADE
            )
        """

        private const val CREATE_TABLE_CARRITO_DETALLE = """
            CREATE TABLE IF NOT EXISTS $TABLE_CARRITO_DET (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                carrito_id INTEGER NOT NULL,
                producto_id INTEGER NOT NULL,
                nombre_snapshot TEXT NOT NULL,
                precio_unitario REAL NOT NULL,
                cantidad INTEGER NOT NULL DEFAULT 1,
                imagen_url TEXT,
                created_at INTEGER NOT NULL,
                FOREIGN KEY(carrito_id) REFERENCES $TABLE_CARRITO(id) ON DELETE CASCADE,
                FOREIGN KEY(producto_id) REFERENCES $TABLE_PRODUCTOS($COLUMN_PRODUCTO_ID) ON DELETE CASCADE
            )
        """

        // Indexes
        private const val CREATE_INDEX_CARRITO_USER = """
            CREATE INDEX IF NOT EXISTS idx_carrito_user ON $TABLE_CARRITO(user_id, status)
        """

        private const val CREATE_INDEX_DET_CARRITO = """
            CREATE INDEX IF NOT EXISTS idx_det_carrito ON $TABLE_CARRITO_DET(carrito_id)
        """

        private const val CREATE_INDEX_DET_PRODUCTO = """
            CREATE INDEX IF NOT EXISTS idx_det_producto ON $TABLE_CARRITO_DET(producto_id)
        """
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_USUARIOS)
        db.execSQL(CREATE_TABLE_PRODUCTOS)
        db.execSQL(CREATE_TABLE_CARRITO)
        db.execSQL(CREATE_TABLE_CARRITO_DETALLE)

        db.execSQL(CREATE_INDEX_CARRITO_USER)
        db.execSQL(CREATE_INDEX_DET_CARRITO)
        db.execSQL(CREATE_INDEX_DET_PRODUCTO)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Durante desarrollo: resetear todo
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CARRITO_DET")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CARRITO")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PRODUCTOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USUARIOS")

        onCreate(db)
    }
}
