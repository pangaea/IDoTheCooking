package com.pangaea.idothecooking.ui.recipe.adapters

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.ui.shared.adapters.draggable.DraggableItemTouchHelperViewHolder
import com.pangaea.idothecooking.utils.extensions.addBackgroundRipple


class RecipeIngredientViewHolder(itemView: View) : RecipeItemStyledHolder(itemView) {
    val display: TextView
    val handleView: ImageView

    init {
        display = itemView.findViewById<TextView>(R.id.display)
        handleView = itemView.findViewById<ImageView>(R.id.handle)
    }
}