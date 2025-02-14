package com.pangaea.idothecooking.ui.recipe.adapters

import android.graphics.Color
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.ui.shared.adapters.draggable.DraggableItemTouchHelperViewHolder
import com.pangaea.idothecooking.utils.extensions.addBackgroundRipple

open class RecipeItemStyledHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
                                                    DraggableItemTouchHelperViewHolder {
    var id: Int = 0
        get() = field
        set(value) {
            field = value
            initItem()
        }

    private fun initItem() {
        if (id == 0) {
            itemView.setBackgroundResource(R.drawable.recipe_new_item_ripple)
        } else if (id < 0) {
            itemView.setBackgroundResource(R.drawable.recipe_changed_item_ripple)
        } else {
            itemView.addBackgroundRipple()
        }
    }

    override fun onItemSelected() {
        itemView.setBackgroundResource(R.color.recipe_selected_item_background)
    }

    override fun onItemClear() {
        initItem()
    }
}