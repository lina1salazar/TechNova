package com.example.technova.database

import android.content.ContentValues
import android.content.Context
import com.example.technova.models.CartItem

class CarritoDAO(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    /** Crea el ítem si no existe, o incrementa la cantidad si ya estaba en el carrito */
    fun agregarOIncrementar(item: CartItem): Boolean {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        return try {
            val c = db.rawQuery(
                "SELECT id, cantidad FROM carrito WHERE producto_id = ?",
                arrayOf(item.productoId.toString())
            )
            if (c.moveToFirst()) {
                val id = c.getInt(0)
                val qty = c.getInt(1)
                c.close()
                val cv = ContentValues().apply { put("cantidad", qty + item.cantidad) }
                db.update("carrito", cv, "id=?", arrayOf(id.toString()))
            } else {
                c.close()
                val cv = ContentValues().apply {
                    put("producto_id", item.productoId)
                    put("nombre_snapshot", item.nombreSnapshot)
                    put("precio_unitario", item.precioUnitario)
                    put("cantidad", item.cantidad)
                    put("imagen_url", item.imagenUrl)
                    put("created_at", item.createdAt)
                }
                db.insert("carrito", null, cv)
            }
            db.setTransactionSuccessful()
            true
        } catch (_: Exception) {
            false
        } finally {
            db.endTransaction()
        }
    }

    /** Lista todos los ítems del carrito (más recientes primero) */
    fun listar(): List<CartItem> {
        val db = dbHelper.readableDatabase
        val out = mutableListOf<CartItem>()
        val c = db.rawQuery(
            "SELECT id, producto_id, nombre_snapshot, precio_unitario, cantidad, imagen_url, created_at " +
                    "FROM carrito ORDER BY created_at DESC",
            null
        )
        c.use {
            while (it.moveToNext()) {
                out.add(
                    CartItem(
                        id = it.getInt(0),
                        productoId = it.getInt(1),
                        nombreSnapshot = it.getString(2),
                        precioUnitario = it.getDouble(3),
                        cantidad = it.getInt(4),
                        imagenUrl = it.getString(5),
                        createdAt = it.getLong(6)
                    )
                )
            }
        }
        return out
    }

    /** Actualiza cantidad; si la nueva cantidad <= 0, elimina el ítem */
    fun actualizarCantidad(idCarrito: Int, nuevaCantidad: Int): Boolean {
        val db = dbHelper.writableDatabase
        return if (nuevaCantidad <= 0) {
            eliminar(idCarrito)
        } else {
            val cv = ContentValues().apply { put("cantidad", nuevaCantidad) }
            db.update("carrito", cv, "id=?", arrayOf(idCarrito.toString())) > 0
        }
    }

    /** Elimina un ítem por id */
    fun eliminar(idCarrito: Int): Boolean {
        val db = dbHelper.writableDatabase
        return db.delete("carrito", "id=?", arrayOf(idCarrito.toString())) > 0
    }

    /** Vacía todo el carrito */
    fun vaciar(): Boolean {
        val db = dbHelper.writableDatabase
        return db.delete("carrito", null, null) >= 0
    }

    /** Total acumulado del carrito */
    fun total(): Double {
        val db = dbHelper.readableDatabase
        val c = db.rawQuery("SELECT IFNULL(SUM(precio_unitario * cantidad), 0) FROM carrito", null)
        c.use { return if (it.moveToFirst()) it.getDouble(0) else 0.0 }
    }

    /** Número total de unidades (suma de cantidades) */
    fun contarItems(): Int {
        val db = dbHelper.readableDatabase
        val c = db.rawQuery("SELECT IFNULL(SUM(cantidad), 0) FROM carrito", null)
        c.use { return if (it.moveToFirst()) it.getInt(0) else 0 }
    }
}
