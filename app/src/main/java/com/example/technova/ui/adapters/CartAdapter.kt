package com.example.technova.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.technova.R
import com.example.technova.database.CarritoDAO
import com.example.technova.models.CartItem

class CartAdapter(
    private var items: MutableList<CartItem>,
    private val carritoDAO: CarritoDAO,
    private val onCartUpdated: () -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgProduct: ImageView = view.findViewById(R.id.imgProduct)
        val tvName: TextView = view.findViewById(R.id.tvProductName)
        val tvPrice: TextView = view.findViewById(R.id.tvProductPrice)
        val tvQuantity: TextView = view.findViewById(R.id.tvQuantity)
        val btnIncrement: ImageButton = view.findViewById(R.id.btnIncrement)
        val btnDecrement: ImageButton = view.findViewById(R.id.btnDecrement)
        val btnRemove: ImageButton = view.findViewById(R.id.btnRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = items[position]

        holder.tvName.text = item.nombreSnapshot
        holder.tvQuantity.text = item.cantidad.toString()
        holder.tvPrice.text = "$${item.precioUnitario * item.cantidad}"

        Glide.with(holder.imgProduct.context)
            .load(item.imagenUrl)
            .placeholder(R.drawable.image_placeholder)
            .into(holder.imgProduct)

        // ➕ Aumentar cantidad
        holder.btnIncrement.setOnClickListener {
            carritoDAO.actualizarCantidad(item.id, item.cantidad + 1)
            item.cantidad++
            notifyItemChanged(position)
            onCartUpdated()
        }

        // ➖ Disminuir cantidad
        holder.btnDecrement.setOnClickListener {
            if (item.cantidad > 1) {
                carritoDAO.actualizarCantidad(item.id, item.cantidad - 1)
                item.cantidad--
                notifyItemChanged(position)
                onCartUpdated()
            }
        }

        // 🗑️ Eliminar ítem
        holder.btnRemove.setOnClickListener {
            carritoDAO.eliminar(item.id)
            items.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, items.size)
            onCartUpdated()
        }
    }

    fun refreshData(newItems: List<CartItem>) {
        items = newItems.toMutableList()
        notifyDataSetChanged()
    }
}
