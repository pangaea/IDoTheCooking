package com.pangaea.idothecooking.utils.formatting

import android.content.Context
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.state.db.entities.ShoppingListDetails
import com.pangaea.idothecooking.state.db.entities.ShoppingListItem
import com.pangaea.idothecooking.utils.extensions.vulgarFraction

class ShoppingListRenderer(private val context: Context, private val shoppingListDetails: ShoppingListDetails) {

    fun drawShoppingListHtml(): String {
        return renderShoppingList(R.string.html_shopping_list, R.string.html_shopping_list_item)
    }

    fun drawShoppingListText(): String {
        return renderShoppingList(R.string.text_shopping_list, R.string.text_shopping_list_item)
    }

    private fun renderShoppingList(template: Int, ingredientTemplate: Int): String {
        val itemBuilder = StringBuilder()
        val htmlRecipeIngredient = context.resources.getString(ingredientTemplate)
        val items = shoppingListDetails.shoppingListItems.toMutableList()
        items.forEach { ingredient: ShoppingListItem ->
            do {
                if (ingredient.amount != null && ingredient.amount!! > 0f) {
                    val frac: Pair<String, Double>? = ingredient.amount?.vulgarFraction
                    if (frac != null) {
                        val amount = frac.first + " " + ingredient.unit
                        itemBuilder.append(htmlRecipeIngredient.replace("{{amount}}", amount)
                                                     .replace("{{name}}", ingredient.name))
                        break;
                    }
                }

                itemBuilder.append(htmlRecipeIngredient.replace("{{amount}}", "")
                                             .replace("{{name}}", ingredient.name))
            } while (false)
        }

        var htmlRecipe = context.resources.getString(template)
        htmlRecipe = htmlRecipe.replace("{{title}}", shoppingListDetails.shoppingList.name)
            .replace("{{items}}", itemBuilder.toString())
        return htmlRecipe
    }
}