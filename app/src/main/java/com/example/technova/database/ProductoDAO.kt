package com.example.technova.database

import android.content.ContentValues
import android.content.Context
import com.example.technova.models.Producto

class ProductoDAO(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    // Crear un producto en la base de datos
    fun insertar(producto: Producto): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_PRODUCTO_NOMBRE, producto.nombre)
            put(DatabaseHelper.COLUMN_PRODUCTO_DESCRIPCION, producto.descripcion)
            put(DatabaseHelper.COLUMN_PRODUCTO_PRECIO, producto.precio)
            put(DatabaseHelper.COLUMN_PRODUCTO_STOCK, producto.stock)
            put(DatabaseHelper.COLUMN_PRODUCTO_IMAGEN_URL, producto.imagenUrl)
        }
        val newRowId = db.insert(DatabaseHelper.TABLE_PRODUCTOS, null, values)
        return newRowId
    }

    // Actualizar un producto en la base de datos
    fun actualizar(producto: Producto): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_PRODUCTO_NOMBRE, producto.nombre)
            put(DatabaseHelper.COLUMN_PRODUCTO_DESCRIPCION, producto.descripcion)
            put(DatabaseHelper.COLUMN_PRODUCTO_PRECIO, producto.precio)
            put(DatabaseHelper.COLUMN_PRODUCTO_STOCK, producto.stock)
            put(DatabaseHelper.COLUMN_PRODUCTO_IMAGEN_URL, producto.imagenUrl)
        }

        val resultado = db.update(
            DatabaseHelper.TABLE_PRODUCTOS,
            values,
            "${DatabaseHelper.COLUMN_PRODUCTO_ID} = ?",
            arrayOf(producto.id.toString())
        )
        return resultado > 0
    }

    // Eliminar un producto de la base de datos
    fun eliminar(productoId: Long): Boolean {
        val db = dbHelper.writableDatabase
        val resultado = db.delete(
            DatabaseHelper.TABLE_PRODUCTOS,
            "${DatabaseHelper.COLUMN_PRODUCTO_ID} = ?",
            arrayOf(productoId.toString())
        )
        return resultado > 0
    }

    // Obtener un producto por su ID
    fun obtenerPorId(productoId: Long): Producto? {
        val db = dbHelper.readableDatabase
        val query = "SELECT * FROM ${DatabaseHelper.TABLE_PRODUCTOS} WHERE ${DatabaseHelper.COLUMN_PRODUCTO_ID} = ?"
        val cursor = db.rawQuery(query, arrayOf(productoId.toString()))
        var p: Producto? = null
        if (cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCTO_ID))
            val nombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCTO_NOMBRE))
            val descripcion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCTO_DESCRIPCION))
            val precio = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCTO_PRECIO))
            val stock = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCTO_STOCK))
            val imagenUrl = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCTO_IMAGEN_URL))
            p = Producto(id, nombre, descripcion, precio, stock, imagenUrl)
        }
        cursor.close()
        return p
    }

    // Obtener todos los productos de la base de datos
    fun obtenerTodos(): List<Producto> {
        val productos = mutableListOf<Producto>()
        val db = dbHelper.readableDatabase
        val query = "SELECT * FROM ${DatabaseHelper.TABLE_PRODUCTOS}"
        val cursor = db.rawQuery(query, null)
        while (cursor.moveToNext()) {
            val p = Producto(
                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCTO_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCTO_NOMBRE)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCTO_DESCRIPCION)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCTO_PRECIO)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCTO_STOCK)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCTO_IMAGEN_URL))
            )
            productos.add(p)
        }
        cursor.close()
        return productos
    }
}