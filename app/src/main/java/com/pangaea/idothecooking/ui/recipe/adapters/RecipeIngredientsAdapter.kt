package com.pangaea.idothecooking.ui.recipe.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.TextAppearanceSpan
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.state.db.entities.Ingredient
import com.pangaea.idothecooking.ui.shared.adapters.draggable.DraggableItemTouchHelperAdapter
import com.pangaea.idothecooking.ui.shared.adapters.draggable.DraggableItemsAdapter
import com.pangaea.idothecooking.ui.shared.adapters.draggable.OnStartDragListener
import com.pangaea.idothecooking.utils.extensions.vulgarFraction
import java.util.Timer
import kotlin.concurrent.timerTask


class RecipeIngredientsAdapter(
    val context: Context?,
    checklistItems: MutableList<Ingredient>?,
    private val mDragStartListener: OnStartDragListener
) :
    DraggableItemsAdapter<Ingredient, RecipeIngredientViewHolder>(checklistItems,
        R.layout.recipe_ingredient_item, mDragStartListener),
    DraggableItemTouchHelperAdapter {
    override fun createHolder(view: View): RecipeIngredientViewHolder {
        return RecipeIngredientViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeIngredientViewHolder, position: Int) {
        val selectedItem = mItems!![position]

        val builderAmount = SpannableStringBuilder()
        val frac: Pair<String, Double>? = selectedItem.amount?.vulgarFraction
        if (frac != null) {
            val s = SpannableString(frac.first)
            s.setSpan(
                TextAppearanceSpan(context, R.style.IngredientAmountStyle),
                0, frac.first.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            builderAmount.append(s)
        }
        if (selectedItem.unit.isNotEmpty()) {
            val s = SpannableString(" " + selectedItem.unit)
            s.setSpan(
                TextAppearanceSpan(context, R.style.IngredientUnitStyle),
                0, selectedItem.unit.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            builderAmount.append(s)
        }

        holder.displayAmountView.text = builderAmount
        val builder = SpannableStringBuilder()
        val s = SpannableString(selectedItem.name)
        s.setSpan(
            TextAppearanceSpan(context, R.style.IngredientNameStyle),
            0, selectedItem.name.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        builder.append(s)
        holder.displayNameView.text = builder
        holder.displayAmountView.setOnClickListener() {
            highlightItem(holder.itemView)
            mDragStartListener.onItemClicked(position)
        }
        holder.displayNameView.setOnClickListener() {
            highlightItem(holder.itemView)
            mDragStartListener.onItemClicked(position)
        }

        // Attach drag event to handle image
        handleDragEvent(holder, holder.handleView)
    }

    @SuppressLint("ResourceType")
    private fun highlightItem(view: View) {
//        if (context != null) {
//            val tv = TypedValue()
//            context.theme?.resolveAttribute(android.R.attr.selectableItemBackground, tv, true)
//            if (tv.resourceId != 0) {
//                view.setBackgroundResource(tv.resourceId);
//            } else {
//                view.setBackgroundColor(tv.data);
//            }
//        }
        view.setBackgroundResource(com.google.android.material.R.color.abc_color_highlight_material)
        Timer().schedule(timerTask {
            view.setBackgroundColor(Color.TRANSPARENT)
        }, 200)
    }
}