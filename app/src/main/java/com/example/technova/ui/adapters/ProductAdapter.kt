package com.example.technova.ui.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.technova.R
import com.example.technova.database.CarritoDAO
import com.example.technova.models.CartItem
import com.example.technova.models.Producto

class ProductAdapter(
    private val onClick: (Producto) -> Unit,
    private val onLongClick: (Producto) -> Unit
) : ListAdapter<Producto, ProductAdapter.VH>(DIFF) {

    private lateinit var carritoDAO: CarritoDAO

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Producto>() {
            override fun areItemsTheSame(old: Producto, new: Producto) = old.id == new.id
            override fun areContentsTheSame(old: Producto, new: Producto) = old == new
        }
    }

    fun attachContext(context: Context) {
        carritoDAO = CarritoDAO(context)
    }


    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val img: ImageView = view.findViewById(R.id.imgProduct)
        private val tvName: TextView = view.findViewById(R.id.tvProductName)
        private val tvDesc: TextView = view.findViewById(R.id.tvProductDesc)
        private val tvPrice: TextView = view.findViewById(R.id.tvProductPrice)
        private val btnAddCart: ImageButton = view.findViewById(R.id.btnAddCart)

        fun bind(p: Producto) {
            tvName.text = p.nombre
            tvDesc.text = p.descripcion
            tvPrice.text = String.format("$%.2f", p.precio)

            // Cargar imagen
            if (!p.imagenUrl.isNullOrEmpty()) {
                Glide.with(img.context)
                    .load(Uri.parse(p.imagenUrl))
                    .placeholder(R.drawable.logo_technova_claro)
                    .error(R.drawable.image_placeholder)
                    .into(img)
            } else {
                img.setImageResource(R.drawable.logo_technova_claro)
            }

            // Clicks generales
            itemView.setOnClickListener { onClick(p) }
            itemView.setOnLongClickListener {
                onLongClick(p)
                true
            }

            // 👉 BOTÓN AGREGAR AL CARRITO
            btnAddCart.setOnClickListener {
                val cartItem = CartItem(
                    id=0,
                    productoId = p.id.toLong(),
                    nombreSnapshot = p.nombre,
                    precioUnitario = p.precio,
                    cantidad = 1,
                    imagenUrl = p.imagenUrl,
                    createdAt = System.currentTimeMillis()
                )

                if (carritoDAO.agregarOIncrementar(cartItem)) {
                    Toast.makeText(
                        itemView.context,
                        "${p.nombre} agregado al carrito",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        itemView.context,
                        "Error al agregar",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        attachContext(parent.context)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(getItem(position))
}
