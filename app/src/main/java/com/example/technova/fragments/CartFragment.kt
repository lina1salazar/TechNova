package com.example.technova.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.technova.R
import com.example.technova.adapters.CartAdapter
import com.example.technova.database.CarritoDAO
import com.example.technova.database.DatabaseHelper

class CartFragment : Fragment() {

    private lateinit var recyclerCart: RecyclerView
    private lateinit var tvTotalAmount: TextView
    private lateinit var btnCheckout: Button

    private lateinit var carritoDAO: CarritoDAO
    private lateinit var adapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_cart, container, false)

        // Referencias UI
        recyclerCart = view.findViewById(R.id.recyclerCart)
        tvTotalAmount = view.findViewById(R.id.tvTotalAmount)
        btnCheckout = view.findViewById(R.id.btnCheckout)

        // DAO
        carritoDAO = CarritoDAO(requireContext())

        setupRecyclerView()
        loadCartItems()

        btnCheckout.setOnClickListener {
            // Lógica de finalizar compra
            // Puedes dejarlo vacío por ahora
        }

        return view
    }

    private fun setupRecyclerView() {
        recyclerCart.layoutManager = LinearLayoutManager(requireContext())
        adapter = CartAdapter(
            items = mutableListOf(),
            carritoDAO = carritoDAO,
            onCartUpdated = {
                updateTotal()
                loadCartItems()
            }
        )
        recyclerCart.adapter = adapter
    }

    private fun loadCartItems() {
        val items = carritoDAO.listar()
        adapter.refreshData(items)
        updateTotal()
    }

    private fun updateTotal() {
        val total = carritoDAO.total()
        tvTotalAmount.text = "$$total"
    }
}
