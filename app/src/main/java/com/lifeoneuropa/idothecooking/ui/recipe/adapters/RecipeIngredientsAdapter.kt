package com.lifeoneuropa.idothecooking.ui.recipe.adapters

import android.content.Context
import android.view.View
import com.lifeoneuropa.idothecooking.R
import com.lifeoneuropa.idothecooking.state.db.entities.Ingredient
import com.lifeoneuropa.idothecooking.ui.shared.adapters.draggable.DraggableItemTouchHelperAdapter
import com.lifeoneuropa.idothecooking.ui.shared.adapters.draggable.DraggableItemsAdapter
import com.lifeoneuropa.idothecooking.ui.shared.adapters.draggable.OnStartDragListener
import com.lifeoneuropa.idothecooking.utils.formatting.IngredientFormatter

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
