package com.cacuango.blockcraft.builder.ui.load

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cacuango.blockcraft.builder.R
import com.cacuango.blockcraft.builder.data.local.entity.Proyecto
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MundoAdapter(
    private val onItemClick: (Proyecto) -> Unit,
    private val onDeleteClick: (Proyecto) -> Unit,
    private val onCargarClick: (Proyecto) -> Unit  // ← flecha directo al editor
) : RecyclerView.Adapter<MundoAdapter.MundoViewHolder>() {

    private var mundos: List<Proyecto> = emptyList()

    fun actualizarLista(nuevosMundos: List<Proyecto>) {
        this.mundos = nuevosMundos
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MundoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mundo, parent, false)
        return MundoViewHolder(view)
    }

    override fun onBindViewHolder(holder: MundoViewHolder, position: Int) {
        holder.bind(mundos[position], onItemClick, onDeleteClick, onCargarClick)
    }

    override fun getItemCount(): Int = mundos.size

    class MundoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre: TextView = itemView.findViewById(R.id.tvNombreMundo)
        private val tvFecha: TextView = itemView.findViewById(R.id.tvFechaMundo)
        private val tvTipo: TextView = itemView.findViewById(R.id.tvTipoMundo)
        private val tvBioma: TextView = itemView.findViewById(R.id.tvBiomaMundo)
        private val btnCargar: Button = itemView.findViewById(R.id.btnCargarMundo)
        private val btnEliminar: Button = itemView.findViewById(R.id.btnEliminarMundo)
        private val ivArrow: ImageView = itemView.findViewById(R.id.ivArrow)

        fun bind(
            mundo: Proyecto,
            onItemClick: (Proyecto) -> Unit,
            onDeleteClick: (Proyecto) -> Unit,
            onCargarClick: (Proyecto) -> Unit
        ) {
            tvNombre.text = mundo.nombre
            tvFecha.text = formatearFecha(mundo.fechaModificacion)
            tvTipo.text = mundo.categoria
            tvBioma.visibility = View.GONE  // ← quitar chip duplicado

            // Botón "Cargar mundo" → editar nombre/categoría
            btnCargar.setOnClickListener { onItemClick(mundo) }

            // Botón "Eliminar"
            btnEliminar.setOnClickListener { onDeleteClick(mundo) }

            // Flecha azul → directo al editor
            ivArrow.setOnClickListener { onCargarClick(mundo) }
        }

        private fun formatearFecha(timestamp: String): String {
            return try {
                val ms = timestamp.toLong()
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                sdf.format(Date(ms))
            } catch (e: Exception) {
                timestamp
            }
        }
    }
}