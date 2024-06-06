package com.pangaea.idothecooking.ui.recipe.adapters

import android.content.Context
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
//        holder.textView.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
//            override fun afterTextChanged(s: Editable) {
//                mItems!![holder.adapterPosition].content = s.toString()
//                mDragStartListener.onItemChanged()
//            }
//        })
        holder.textView.setOnClickListener() {
            mDragStartListener.onItemClicked(position)
        }

        // Attach drag event to handle image
        handleDragEvent(holder, holder.handleView)
    }
}