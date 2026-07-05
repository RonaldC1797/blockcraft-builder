package com.cacuango.blockcraft.builder.ui.home

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.cacuango.blockcraft.builder.R
import com.cacuango.blockcraft.builder.ui.editor.IsometricView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class InventarioBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottom_sheet_inventario, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val contenedor = view.findViewById<LinearLayout>(R.id.layoutInventario)

        val categorias = mapOf(
            "🧱 Estructura" to IsometricView.BLOQUES_ESTRUCTURA,
            "🎨 Decoración" to IsometricView.BLOQUES_DECORACION,
            "🌿 Naturaleza" to IsometricView.BLOQUES_NATURALEZA,
            "⭐ Especiales" to IsometricView.BLOQUES_ESPECIALES
        )

        categorias.forEach { (nombreCategoria, bloques) ->
            // Título de categoría
            val tvCategoria = TextView(requireContext()).apply {
                text = nombreCategoria
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(resources.getColor(R.color.purple_500, null))
                setPadding(0, 24, 0, 12)
            }
            contenedor.addView(tvCategoria)

            // Filas de bloques (4 por fila)
            var fila = crearFila()
            contenedor.addView(fila)

            bloques.forEachIndexed { index, nombreBloque ->
                if (index > 0 && index % 4 == 0) {
                    fila = crearFila()
                    contenedor.addView(fila)
                }

                val resId = resources.getIdentifier(
                    nombreBloque, "drawable", requireContext().packageName
                )

                val item = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                    val lp = LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    lp.setMargins(4, 4, 4, 8)
                    layoutParams = lp
                }

                val img = ImageView(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(72, 72)
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    if (resId != 0) setImageResource(resId)
                }

                val nombre = TextView(requireContext()).apply {
                    text = formatearNombre(nombreBloque)
                    textSize = 9f
                    gravity = Gravity.CENTER
                    setTextColor(resources.getColor(android.R.color.darker_gray, null))
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }

                item.addView(img)
                item.addView(nombre)
                fila.addView(item)
            }

            // Rellenar espacios vacíos en la última fila
            val resto = bloques.size % 4
            if (resto != 0) {
                repeat(4 - resto) {
                    val espacio = View(requireContext()).apply {
                        layoutParams = LinearLayout.LayoutParams(0,
                            LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    }
                    fila.addView(espacio)
                }
            }
        }
    }

    private fun crearFila(): LinearLayout {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
    }

    private fun formatearNombre(nombreBloque: String): String {
        val nombres = mapOf(
            // ESTRUCTURA
            "blk_estructura_piedra"         to "Piedra",
            "blk_estructura_madera"         to "Madera",
            "blk_estructura_madera_oscura"  to "Madera de abeto",
            "blk_estructura_madera_clara"   to "Arbusto",
            "blk_estructura_ladrillo"       to "Ladrillo",
            "blk_estructura_metal"          to "Madera roble oscuro",
            "blk_estructura_metal_oscuro"   to "Metal oscuro",
            "blk_estructura_cemento"        to "Cemento",
            "blk_estructura_cemento_oscuro" to "Cemento oscuro",
            "blk_estructura_roca"           to "Roca",
            "blk_estructura_roca_oscura"    to "Roca oscura",

            // DECORACIÓN
            "blk_deco_amarillo"             to "Horno",
            "blk_deco_naranja"              to "Mesa de trabajo",
            "blk_deco_morado"               to "Cristal",
            "blk_deco_especial"             to "Lana",

            // NATURALEZA
            "blk_nat_cesped"                to "Tierra húmeda",
            "blk_nat_tierra"                to "Nieve",
            "blk_nat_tierra_oscura"         to "Hojas",
            "blk_nat_arcilla"               to "Arcilla",
            "blk_nat_arena"                 to "Hielo",
            "blk_nat_nieve"                 to "Hielo duro",
            "blk_nat_hielo"                 to "Arcilla roja",
            "blk_nat_agua"                  to "Musgo",
            "blk_nat_lava"                  to "Lodo",
            "blk_nat_roca_verde"            to "Ceniza",
            "blk_nat_musgo"                 to "Nieve sobre piedra",
            "blk_nat_madera_arbol"          to "Arena",
            "blk_nat_flor"                  to "Tierra",
            "blk_nat_hoja"                  to "Pasto",

            // ESPECIALES
            "blk_esp_cristal_rojo"          to "Hierro incrustado",
            "blk_esp_cristal_azul"          to "Oro",
            "blk_esp_cristal_verde"         to "Diamantes",
            "blk_esp_oro"                   to "Mineral raro",
            "blk_esp_diamante"              to "Infección",
            "blk_esp_esmeralda"             to "Carbón",
            "blk_esp_rubi"                  to "Especial",
            "blk_esp_zafiro"                to "Cobre",
            "blk_esp_neon_azul"             to "Cofre verde",
            "blk_esp_neon_verde"            to "Cofre azul",
            "blk_esp_gris_especial"         to "Esmeralda"
        )
        return nombres[nombreBloque] ?: nombreBloque
            .replace("blk_", "")
            .replace("_", " ")
            .replaceFirstChar { it.uppercase() }
    }}