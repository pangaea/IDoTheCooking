package com.pangaea.idothecooking.ui.recipe.adapters

import android.content.Context
import android.view.View
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.state.db.entities.Ingredient
import com.pangaea.idothecooking.ui.shared.adapters.draggable.DraggableItemTouchHelperAdapter
import com.pangaea.idothecooking.ui.shared.adapters.draggable.DraggableItemsAdapter
import com.pangaea.idothecooking.ui.shared.adapters.draggable.OnStartDragListener
import com.pangaea.idothecooking.utils.formatting.IngredientFormatter

class RecipeIngredientsAdapter(
    val context: Context?,
    ingredients: MutableList<Ingredient>?,
    private val mDragStartListener: OnStartDragListener
) :
    DraggableItemsAdapter<Ingredient, RecipeIngredientViewHolder>(ingredients,
        R.layout.recipe_ingredient_item, mDragStartListener),
    DraggableItemTouchHelperAdapter {
    override fun createHolder(view: View): RecipeIngredientViewHolder {
        return RecipeIngredientViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeIngredientViewHolder, position: Int) {
        val selectedItem = mItems!![position]
        holder.id = selectedItem.id
        holder.display.text = context?.let { IngredientFormatter.formatDisplay(it, selectedItem, selectedItem.id) }
        holder.itemView.setOnClickListener { mDragStartListener.onItemClicked(position) }

        // Attach drag event to handle image
        handleDragEvent(holder, holder.handleView)
    }
}
