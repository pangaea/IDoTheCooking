package com.pangaea.idothecooking.ui.recipe.adapters

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

    override fun onBindViewHolder(holder: RecipeDirectionViewHolder, position: Int) {
        holder.textView.setText(mItems!![position].content)
        holder.textView.setOnClickListener() {
            highlightItem(holder.itemView)
            mDragStartListener.onItemClicked(position)
        }

        // Attach drag event to handle image
        handleDragEvent(holder, holder.handleView)
    }

    private fun highlightItem(view: View) {
        view.setBackgroundResource(com.google.android.material.R.color.abc_color_highlight_material)
        Timer().schedule(timerTask {
            view.setBackgroundColor(Color.TRANSPARENT)
        }, 200)
    }
}