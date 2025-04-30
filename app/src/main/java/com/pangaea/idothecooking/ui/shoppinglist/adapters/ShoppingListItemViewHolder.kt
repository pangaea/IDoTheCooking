package com.pangaea.idothecooking.ui.shoppinglist.adapters

import android.graphics.Color
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.ui.shared.adapters.draggable.DraggableItemTouchHelperViewHolder

class ShoppingListItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
                                                   DraggableItemTouchHelperViewHolder {
    val display: TextView
    val handleView: ImageView
    val isChecked: CheckBox

    init {
        isChecked = itemView.findViewById<CheckBox>(R.id.item_checked)
        display = itemView.findViewById<TextView>(R.id.display)
        handleView = itemView.findViewById<ImageView>(R.id.handle)
    }

    override fun onItemSelected() {
        itemView.setBackgroundColor(Color.LTGRAY)
    }

    override fun onItemClear() {
        itemView.setBackgroundColor(0)
    }
}