package com.pangaea.idothecooking.utils.formatting

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.TextAppearanceSpan
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.ui.recipe.adapters.HelperSuggestion

class SuggestionFormatter {
    companion object {
        private fun appendFormattedText(context: Context, builder: SpannableStringBuilder, text: String, style: Int) {
            val span = SpannableString(text)
            span.setSpan(
                TextAppearanceSpan(context, style),
                0,
                text.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            builder.append(span)
        }

        fun formatDisplay(context: Context, suggestion: HelperSuggestion): SpannableStringBuilder {
            val builder = SpannableStringBuilder()
            if (suggestion.ingredient != null) {
                appendFormattedText(context, builder,
                                    suggestion.ingredient!!,
                                    R.style.SuggestionIngredientStyleChanged)
                appendFormattedText(context, builder, " ", R.style.SuggestionIngredientStyleChanged)
            } else if (suggestion.cookingTechnique != null) {
                appendFormattedText(context, builder,
                                    suggestion.cookingTechnique!!,
                                    R.style.SuggestionIngredientStyleChanged)
                appendFormattedText(context, builder, " ", R.style.SuggestionIngredientStyleChanged)
            }
            appendFormattedText(context, builder, suggestion.description, R.style.SuggestionDescStyleChanged)
            return builder
        }
    }
}