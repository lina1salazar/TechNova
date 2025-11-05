package com.example.technova.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.technova.R
import com.example.technova.database.ProductoDAO
import com.example.technova.databinding.ActivityProductsBinding
import com.example.technova.models.Producto
import com.example.technova.ui.adapters.ProductAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.concurrent.thread

class ProductsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductsBinding
    private lateinit var adapter: ProductAdapter
    private lateinit var productoDAO: ProductoDAO
    private var esAdmin: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        productoDAO = ProductoDAO(this)
        var prefs = getSharedPreferences("technova_prefs", MODE_PRIVATE)
        esAdmin = prefs.getBoolean("esAdmin", false)

        adapter = ProductAdapter(
            onClick = {producto ->onProductClick(producto)},
            onLongClick = {producto -> onProductLongClick(producto)}
        )

        binding.recyclerProducts.adapter = adapter


        binding.fabAddProduct.visibility = if (esAdmin) FloatingActionButton.VISIBLE else FloatingActionButton.GONE
        binding.fabAddProduct.setOnClickListener {
            // Solo el administrador puede agregar productos
            if (!esAdmin) {
                Toast.makeText(this, "No tienes permiso para agregar productos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Toast.makeText(this, "Agregar producto", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, AddEditProductActivity::class.java)
            startActivity(intent)
        }

        binding.swipeRefresh.setOnRefreshListener {cargarProductos()}
        cargarProductos()
    }

    override fun onResume(){
        super.onResume()
        cargarProductos()
    }

    private fun cargarProductos(){
        binding.swipeRefresh.isRefreshing = true
        thread {
            val productos = productoDAO.obtenerTodos()
            runOnUiThread {
                adapter.submitList(productos)
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun onProductClick(producto: Producto){
        // Solo el administrador puede editar productos
        if (esAdmin) {
            val intent = Intent(this, AddEditProductActivity::class.java)
            intent.putExtra("productoId", producto.id)
            startActivity(intent)
        } else {
            // Mostrar detalles del producto
            Toast.makeText(this, "${producto.nombre}\n${producto.precio}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onProductLongClick(producto: Producto) {
        // Solo el administrador puede eliminar productos
        if (!esAdmin) {
            Toast.makeText(this, "No tienes permiso para eliminar productos", Toast.LENGTH_SHORT)
                .show()
            return
        }
        AlertDialog.Builder(this)
            .setTitle("Eliminar producto")
            .setMessage("¿Deseas eliminar el producto ${producto.nombre}?")
            .setPositiveButton("Eliminar") { _, _ ->
                thread {
                    val ok = productoDAO.eliminar(producto.id)
                    runOnUiThread {
                        if (ok) {
                            cargarProductos()
                            Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(
                                this,
                                "Error al eliminar el producto",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}