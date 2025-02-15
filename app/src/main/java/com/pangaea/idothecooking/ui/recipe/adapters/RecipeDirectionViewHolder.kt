package com.pangaea.idothecooking.ui.recipe.adapters

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.ui.shared.adapters.draggable.DraggableItemTouchHelperViewHolder
import com.pangaea.idothecooking.utils.extensions.addBackgroundRipple

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