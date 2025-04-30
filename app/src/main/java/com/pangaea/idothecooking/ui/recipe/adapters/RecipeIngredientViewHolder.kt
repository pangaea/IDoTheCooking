package com.pangaea.idothecooking.ui.recipe.adapters

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.pangaea.idothecooking.R


class RecipeIngredientViewHolder(itemView: View) : RecipeItemStyledHolder(itemView) {
    val display: TextView
    val handleView: ImageView

    init {
        display = itemView.findViewById<TextView>(R.id.display)
        handleView = itemView.findViewById<ImageView>(R.id.handle)
    }
}