package com.example.technova.models

data class CartItem(
    val id: Int = 0,
    val productoId: Int,
    val nombreSnapshot: String,
    val precioUnitario: Double,
    val cantidad: Int,
    val imagenUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    val subtotal: Double get() = precioUnitario * cantidad
}
