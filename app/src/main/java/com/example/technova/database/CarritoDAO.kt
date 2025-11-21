package com.example.technova.database

import android.content.ContentValues
import android.content.Context
import com.example.technova.models.CartItem

class CarritoDAO(private val context: Context) {
    private val dbHelper = DatabaseHelper(context)
    private val productoDAO = ProductoDAO(context)

    /** Lee el usuario actual como Long (desde SharedPreferences) */
    private fun currentUserId(): Long {
        val prefs = context.getSharedPreferences("technova_prefs", Context.MODE_PRIVATE)
        return prefs.getLong("usuarioId", -1L)
    }

    /** Obtiene o crea el carrito OPEN del usuario actual y retorna su id (Long) */
    private fun getOrCreateOpenCartId(): Long {
        val userId = currentUserId()
        if (userId <= 0L) return -1L

        val db = dbHelper.writableDatabase

        db.rawQuery(
            "SELECT id FROM ${DatabaseHelper.TABLE_CARRITO} WHERE user_id=? AND status='OPEN' LIMIT 1",
            arrayOf(userId.toString())
        ).use { c ->
            if (c.moveToFirst()) return c.getLong(0)
        }

        val cv = ContentValues().apply {
            put("user_id", userId)
            put("created_at", System.currentTimeMillis())
            put("status", "OPEN")
        }
        db.insert(DatabaseHelper.TABLE_CARRITO, null, cv)

        db.rawQuery(
            "SELECT id FROM ${DatabaseHelper.TABLE_CARRITO} WHERE user_id=? AND status='OPEN' ORDER BY id DESC LIMIT 1",
            arrayOf(userId.toString())
        ).use { c ->
            if (c.moveToFirst()) return c.getLong(0)
        }
        return -1L
    }

    /** Crea la línea si no existe o incrementa cantidad; limita por stock del producto */
    fun agregarOIncrementar(item: CartItem): Boolean {
        val cartId = getOrCreateOpenCartId()
        if (cartId <= 0L) return false

        val db = dbHelper.writableDatabase
        // Soporta CartItem.productoId como Int o Long (ambos tienen .toLong())
        val prod = productoDAO.obtenerPorId(item.productoId.toLong()) ?: return false
        val stock = prod.stock

        db.beginTransaction()
        return try {
            db.rawQuery(
                "SELECT id, cantidad FROM ${DatabaseHelper.TABLE_CARRITO_DET} WHERE carrito_id=? AND producto_id=?",
                arrayOf(cartId.toString(), item.productoId.toString())
            ).use { c ->
                if (c.moveToFirst()) {
                    val detId = c.getInt(0)
                    val actual = c.getInt(1)
                    val nueva = (actual + item.cantidad).coerceAtMost(stock)
                    if (nueva <= 0) {
                        db.delete(DatabaseHelper.TABLE_CARRITO_DET, "id=?", arrayOf(detId.toString()))
                    } else {
                        val cv = ContentValues().apply { put("cantidad", nueva) }
                        db.update(DatabaseHelper.TABLE_CARRITO_DET, cv, "id=?", arrayOf(detId.toString()))
                    }
                } else {
                    val cantidad = item.cantidad.coerceAtMost(stock)
                    if (cantidad > 0) {
                        val cv = ContentValues().apply {
                            put("carrito_id", cartId)                        // Long ok (INTEGER en SQLite)
                            put("producto_id", item.productoId.toInt())      // guardamos como INTEGER
                            put("nombre_snapshot", item.nombreSnapshot)
                            put("precio_unitario", item.precioUnitario)
                            put("cantidad", cantidad)
                            put("imagen_url", item.imagenUrl)
                            put("created_at", item.createdAt)
                        }
                        db.insert(DatabaseHelper.TABLE_CARRITO_DET, null, cv)
                    }
                }
            }
            db.setTransactionSuccessful()
            true
        } catch (_: Exception) {
            false
        } finally {
            db.endTransaction()
        }
    }

    /** Lista las líneas del carrito del usuario actual (más recientes primero) */
    fun listar(): List<CartItem> {
        val cartId = getOrCreateOpenCartId()
        if (cartId <= 0L) return emptyList()

        val db = dbHelper.readableDatabase
        val out = mutableListOf<CartItem>()
        db.rawQuery(
            "SELECT id, producto_id, nombre_snapshot, precio_unitario, cantidad, imagen_url, created_at " +
                    "FROM ${DatabaseHelper.TABLE_CARRITO_DET} WHERE carrito_id=? ORDER BY created_at DESC",
            arrayOf(cartId.toString())
        ).use { c ->
            while (c.moveToNext()) {
                out.add(
                    CartItem(
                        id = c.getLong(0),
                        // Si tu CartItem.productoId es Long, cambia a getLong(1)
                        productoId = c.getInt(1),
                        nombreSnapshot = c.getString(2),
                        precioUnitario = c.getDouble(3),
                        cantidad = c.getInt(4),
                        imagenUrl = c.getString(5),
                        createdAt = c.getLong(6)
                    )
                )
            }
        }
        return out
    }

    /** Actualiza cantidad; si <=0 elimina. Respeta stock actual del producto. */
    fun actualizarCantidad(idDetalle: Int, nuevaCantidad: Int): Boolean {
        val db = dbHelper.writableDatabase

        var productoId = -1
        db.rawQuery(
            "SELECT producto_id FROM ${DatabaseHelper.TABLE_CARRITO_DET} WHERE id=?",
            arrayOf(idDetalle.toString())
        ).use { c -> if (c.moveToFirst()) productoId = c.getInt(0) }
        if (productoId <= 0) return false

        val stock = productoDAO.obtenerPorId(productoId.toLong())?.stock ?: Int.MAX_VALUE
        val nueva = nuevaCantidad.coerceAtMost(stock)

        return if (nueva <= 0) {
            eliminar(idDetalle)
        } else {
            val cv = ContentValues().apply { put("cantidad", nueva) }
            db.update(DatabaseHelper.TABLE_CARRITO_DET, cv, "id=?", arrayOf(idDetalle.toString())) > 0
        }
    }

    /** Elimina una línea del carrito (por id detalle) */
    fun eliminar(idDetalle: Int): Boolean {
        val db = dbHelper.writableDatabase
        return db.delete(DatabaseHelper.TABLE_CARRITO_DET, "id=?", arrayOf(idDetalle.toString())) > 0
    }

    /** Vacía el carrito del usuario actual */
    fun vaciar(): Boolean {
        val cartId = getOrCreateOpenCartId()
        if (cartId <= 0L) return false
        val db = dbHelper.writableDatabase
        return db.delete(DatabaseHelper.TABLE_CARRITO_DET, "carrito_id=?", arrayOf(cartId.toString())) >= 0
    }

    /** Total $ del carrito del usuario actual */
    fun total(): Double {
        val cartId = getOrCreateOpenCartId()
        if (cartId <= 0L) return 0.0
        val db = dbHelper.readableDatabase
        db.rawQuery(
            "SELECT IFNULL(SUM(precio_unitario * cantidad),0) FROM ${DatabaseHelper.TABLE_CARRITO_DET} WHERE carrito_id=?",
            arrayOf(cartId.toString())
        ).use { c -> return if (c.moveToFirst()) c.getDouble(0) else 0.0 }
    }

    /** Número total de unidades en el carrito del usuario actual */
    fun contarItems(): Int {
        val cartId = getOrCreateOpenCartId()
        if (cartId <= 0L) return 0
        val db = dbHelper.readableDatabase
        db.rawQuery(
            "SELECT IFNULL(SUM(cantidad),0) FROM ${DatabaseHelper.TABLE_CARRITO_DET} WHERE carrito_id=?",
            arrayOf(cartId.toString())
        ).use { c -> return if (c.moveToFirst()) c.getInt(0) else 0 }
    }
}

