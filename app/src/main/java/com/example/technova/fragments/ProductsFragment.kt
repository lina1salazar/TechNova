package com.example.technova.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.technova.R
import com.example.technova.activities.AddEditProductActivity
import com.example.technova.database.ProductoDAO
import com.example.technova.databinding.FragmentProductsBinding
import com.example.technova.models.Producto
import com.example.technova.ui.adapters.ProductAdapter
import kotlin.concurrent.thread

class ProductsFragment : Fragment() {
    private var _binding: FragmentProductsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ProductAdapter
    private lateinit var productoDAO: ProductoDAO
    private var esAdmin: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProductsBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializaciones dependientes del contexto/vista
        productoDAO = ProductoDAO(requireContext())

        val prefs = requireActivity().getSharedPreferences("technova_prefs", 0)
        esAdmin = prefs.getBoolean("esAdmin", false)

        adapter = ProductAdapter(
            onClick = { producto -> onProductClick(producto) },
            onLongClick = { producto -> onProductLongClick(producto) }
        )

        // Recycler
        binding.recyclerProducts.adapter = adapter

        // FAB — solo visible si es admin
        binding.fabAddProduct.visibility = if (esAdmin) View.VISIBLE else View.GONE
        binding.fabAddProduct.setOnClickListener {
            if (!esAdmin) {
                Toast.makeText(requireContext(), "No tienes permiso para agregar productos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Toast.makeText(requireContext(), "Agregar producto", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), AddEditProductActivity::class.java)
            startActivity(intent)
        }

        // SwipeRefresh
        binding.swipeRefresh.setOnRefreshListener { cargarProductos() }

        // Cargar por primera vez
        cargarProductos()
    }

    override fun onResume() {
        super.onResume()
        cargarProductos()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun cargarProductos() {
        binding.swipeRefresh.isRefreshing = true
        thread {
            val productos = productoDAO.obtenerTodos()
            requireActivity().runOnUiThread {
                adapter.submitList(productos)
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun onProductClick(producto: Producto) {
        if (esAdmin) {
            val intent = Intent(requireContext(), AddEditProductActivity::class.java)
            intent.putExtra("productoId", producto.id)
            startActivity(intent)
        } else {
            Toast.makeText(requireContext(), "${producto.nombre}\n${producto.precio}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onProductLongClick(producto: Producto) {
        if (!esAdmin) {
            Toast.makeText(requireContext(), "No tienes permiso para eliminar productos", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar producto")
            .setMessage("¿Deseas eliminar el producto ${producto.nombre}?")
            .setPositiveButton("Eliminar") { _, _ ->
                thread {
                    val ok = productoDAO.eliminar(producto.id)
                    requireActivity().runOnUiThread {
                        if (ok) {
                            cargarProductos()
                            Toast.makeText(requireContext(), "Producto eliminado", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Error al eliminar el producto", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}