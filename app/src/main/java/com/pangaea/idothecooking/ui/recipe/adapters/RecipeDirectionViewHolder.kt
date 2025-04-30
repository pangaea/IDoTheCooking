package com.pangaea.idothecooking.ui.recipe.adapters

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.pangaea.idothecooking.R

class RecipeDirectionViewHolder(itemView: View) : RecipeItemStyledHolder(itemView) {
    val bulletView: ImageView
    val textView: TextView
    val handleView: ImageView

    init {
        bulletView = itemView.findViewById<ImageView>(R.id.bullet)
        textView = itemView.findViewById<TextView>(R.id.text)
        handleView = itemView.findViewById<ImageView>(R.id.handle)
    }
}