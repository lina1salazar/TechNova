package com.example.technova.models

data class CartItem(
    val id: Long = 0,
    val productoId: Long,
    val nombreSnapshot: String,
    val precioUnitario: Double,
    var cantidad: Int,
    val imagenUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    val subtotal: Double get() = precioUnitario * cantidad
}
