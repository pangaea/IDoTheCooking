package com.lifeoneuropa.idothecooking.ui.recipe.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import com.lifeoneuropa.idothecooking.R
import com.lifeoneuropa.idothecooking.state.db.entities.Direction
import com.lifeoneuropa.idothecooking.ui.shared.adapters.draggable.DraggableItemTouchHelperAdapter
import com.lifeoneuropa.idothecooking.ui.shared.adapters.draggable.OnStartDragListener
import com.lifeoneuropa.idothecooking.ui.shared.adapters.draggable.DraggableItemsAdapter

class RecipeDirectionsAdapter(
    context: Context?,
    checklistItems: MutableList<Direction>?,
    private val mDragStartListener: OnStartDragListener
) :
    DraggableItemsAdapter<Direction, RecipeDirectionViewHolder>(checklistItems,
        R.layout.recipe_direction_item, mDragStartListener),
    DraggableItemTouchHelperAdapter {
    override fun createHolder(view: View): RecipeDirectionViewHolder {
        return RecipeDirectionViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecipeDirectionViewHolder, position: Int) {
        val selectedItem = mItems!![position]
        holder.id = selectedItem.id
        holder.textView.setText(selectedItem.content)
        holder.itemView.setOnClickListener { mDragStartListener.onItemClicked(position) }

        // Attach drag event to handle image
        handleDragEvent(holder, holder.handleView)
    }
}