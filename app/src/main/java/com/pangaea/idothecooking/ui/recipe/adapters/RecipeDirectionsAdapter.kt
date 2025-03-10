package com.pangaea.idothecooking.ui.recipe.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import androidx.core.view.MotionEventCompat
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.state.db.entities.Direction
import com.pangaea.idothecooking.ui.shared.adapters.draggable.DraggableItemTouchHelperAdapter
import com.pangaea.idothecooking.ui.shared.adapters.draggable.OnStartDragListener
import com.pangaea.idothecooking.ui.shared.adapters.draggable.DraggableItemsAdapter
import com.pangaea.idothecooking.utils.extensions.addBackgroundRipple
import java.util.Timer
import kotlin.concurrent.timerTask

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