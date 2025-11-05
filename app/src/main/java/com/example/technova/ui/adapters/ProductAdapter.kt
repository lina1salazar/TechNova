package com.example.technova.ui.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.technova.R
import com.example.technova.models.Producto

class ProductAdapter(
    private val onClick: (Producto) -> Unit,
    private val onLongClick: (Producto) -> Unit
) : ListAdapter<Producto, ProductAdapter.VH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Producto>() {
            override fun areItemsTheSame(old: Producto, new: Producto) = old.id == new.id
            override fun areContentsTheSame(old: Producto, new: Producto) = old == new
        }
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val img: ImageView = view.findViewById(R.id.imgProduct)
        private val tvName: TextView = view.findViewById(R.id.tvProductName)
        private val tvDesc: TextView = view.findViewById(R.id.tvProductDesc)
        private val tvPrice: TextView = view.findViewById(R.id.tvProductPrice)

        fun bind(p: Producto) {
            tvName.text = p.nombre
            tvDesc.text = p.descripcion
            tvPrice.text = String.format("$%.2f", p.precio)
            if (!p.imagenUrl.isNullOrEmpty()) {
                Glide.with(img.context)
                    .load(Uri.parse(p.imagenUrl))
                    .placeholder(R.drawable.logo_technova_claro)
                    .error(R.drawable.image_placeholder)
                    .into(img)
            } else {
                img.setImageResource(R.drawable.logo_technova_claro)
            }

            itemView.setOnClickListener { onClick(p) }
            itemView.setOnLongClickListener {
                onLongClick(p)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))
}
