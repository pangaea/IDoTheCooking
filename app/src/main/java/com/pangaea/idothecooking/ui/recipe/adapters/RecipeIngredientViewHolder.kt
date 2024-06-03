package com.pangaea.idothecooking.ui.recipe.adapters

import android.graphics.Color
import android.text.Html
import android.view.View
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.ui.shared.adapters.draggable.DraggableItemTouchHelperViewHolder

class RecipeIngredientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
    DraggableItemTouchHelperViewHolder {
    val displayAmountView: TextView
    val displayNameView: TextView
    //val editView: ImageView
    val handleView: ImageView

    init {
        displayAmountView = itemView.findViewById<TextView>(R.id.display_amount)
        displayNameView = itemView.findViewById<TextView>(R.id.display_name)
        //editView = itemView.findViewById<ImageView>(R.id.edit)
        handleView = itemView.findViewById<ImageView>(R.id.handle)
    }

    override fun onItemSelected() {
        itemView.setBackgroundColor(Color.LTGRAY)
    }

    override fun onItemClear() {
        itemView.setBackgroundColor(0)
    }
}