// ui/load/MundoAdapter.kt
package com.cacuango.blockcraft.builder.ui.load

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cacuango.blockcraft.builder.R
import com.cacuango.blockcraft.builder.data.local.entity.Proyecto  // ✅ Importación correcta

class MundoAdapter(
    private val onItemClick: (Proyecto) -> Unit,
    private val onDeleteClick: (Proyecto) -> Unit
) : RecyclerView.Adapter<MundoAdapter.MundoViewHolder>() {

    private var mundos: List<Proyecto> = emptyList()

    // Datos de ejemplo para mostrar en la UI
    private val tiposEjemplo = listOf("SUPERVIVENCIA", "CREATIVO", "AVENTURA", "ESPECTADOR")
    private val biomasEjemplo = listOf("PRADERA", "DESIERTO", "BOSQUE", "NIEVE", "ESPACIO")

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
        val mundo = mundos[position]
        val tipo = tiposEjemplo[position % tiposEjemplo.size]
        val bioma = biomasEjemplo[position % biomasEjemplo.size]
        holder.bind(mundo, tipo, bioma, onItemClick, onDeleteClick)
    }

    override fun getItemCount(): Int = mundos.size

    class MundoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre: TextView = itemView.findViewById(R.id.tvNombreMundo)
        private val tvFecha: TextView = itemView.findViewById(R.id.tvFechaMundo)
        private val tvTipo: TextView = itemView.findViewById(R.id.tvTipoMundo)
        private val tvBioma: TextView = itemView.findViewById(R.id.tvBiomaMundo)
        private val btnCargar: Button = itemView.findViewById(R.id.btnCargarMundo)
        private val btnEliminar: Button = itemView.findViewById(R.id.btnEliminarMundo)

        fun bind(
            mundo: Proyecto,
            tipo: String,
            bioma: String,
            onItemClick: (Proyecto) -> Unit,
            onDeleteClick: (Proyecto) -> Unit
        ) {
            tvNombre.text = mundo.nombre
            tvFecha.text = mundo.fechaCreacion
            tvTipo.text = tipo
            tvBioma.text = bioma

            btnCargar.setOnClickListener { onItemClick(mundo) }
            btnEliminar.setOnClickListener { onDeleteClick(mundo) }
        }
    }
}