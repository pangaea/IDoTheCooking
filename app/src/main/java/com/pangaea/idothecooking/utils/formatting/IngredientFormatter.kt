package com.pangaea.idothecooking.utils.formatting

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.TextAppearanceSpan
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.state.db.entities.Ingredient
import com.pangaea.idothecooking.state.db.entities.MeasuredItem
import com.pangaea.idothecooking.utils.extensions.vulgarFraction

class IngredientFormatter {
    companion object {
        fun formatDisplay(context: Context, ingredient: MeasuredItem, id: Int): SpannableStringBuilder {
            val builder = SpannableStringBuilder()
            if (ingredient.amount != null && ingredient.amount!! > 0f) {
                val frac: Pair<String, Double>? = ingredient.amount?.vulgarFraction
                if (frac != null) {
                    val s = SpannableString(frac.first)
                    s.setSpan(
                        TextAppearanceSpan(context, R.style.IngredientAmountStyle),
                        0, frac.first.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    builder.append(s)
                }
            }

            if (!builder.isEmpty() && ingredient.unit.isNotEmpty()) {
                val s = SpannableString(" " + ingredient.unit)
                s.setSpan(
                    TextAppearanceSpan(context, R.style.IngredientUnitStyle),
                    0, ingredient.unit.length + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                builder.append(s)
            }

            var text = ingredient.name
            if (!builder.isEmpty()) {
                text = " $text"
            }

//            val appearance = if(id == 0)
//                                TextAppearanceSpan(context, R.style.IngredientNameStyleNew)
//                             else if(id < 0)
//                                TextAppearanceSpan(context, R.style.IngredientNameStyleChanged)
//                             else
//                                TextAppearanceSpan(context, R.style.IngredientNameStyle)


            val s = SpannableString(text)
            s.setSpan(
                TextAppearanceSpan(context, R.style.IngredientNameStyle),
                0, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            builder.append(s)
            return builder
        }
    }
}