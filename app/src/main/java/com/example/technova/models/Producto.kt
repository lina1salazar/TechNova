package com.example.technova.models

data class Producto(
    val id: Long = 0,
    val nombre: String,
    val descripcion: String? = null,
    val precio: Double,
    val stock: Int,
    val imagenUrl: String
)
