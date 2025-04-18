package com.lifeoneuropa.idothecooking.ui.shoppinglist.adapters

import android.content.Context
import android.view.View
import com.lifeoneuropa.idothecooking.R
import com.lifeoneuropa.idothecooking.state.db.entities.ShoppingListItem
import com.lifeoneuropa.idothecooking.ui.shared.adapters.draggable.DraggableItemTouchHelperAdapter
import com.lifeoneuropa.idothecooking.ui.shared.adapters.draggable.DraggableItemsAdapter
import com.lifeoneuropa.idothecooking.ui.shared.adapters.draggable.OnStartDragListener
import com.lifeoneuropa.idothecooking.utils.formatting.IngredientFormatter

class ShoppingListItemsAdapter(
    val context: Context?,
    val items: MutableList<ShoppingListItem>?,
    private val mDragStartListener: OnStartDragListener
) :
    DraggableItemsAdapter<ShoppingListItem, ShoppingListItemViewHolder>(items,
                                                                    R.layout.shopping_list_item, mDragStartListener),
    DraggableItemTouchHelperAdapter {
    override fun createHolder(view: View): ShoppingListItemViewHolder {
        return ShoppingListItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShoppingListItemViewHolder, position: Int) {
        val selectedItem = mItems!![position]
        holder.isChecked.isChecked = selectedItem.checked
        holder.display.text = context?.let { IngredientFormatter.formatDisplay(it, selectedItem, selectedItem.id) }
        holder.itemView.setOnClickListener { mDragStartListener.onItemClicked(position) }
        holder.isChecked.setOnClickListener {
            selectedItem.checked = holder.isChecked.isChecked
            mDragStartListener.onItemChanged()
        }

        // Attach drag event to handle image
        handleDragEvent(holder, holder.handleView)
    }
}