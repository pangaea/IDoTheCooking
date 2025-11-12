package com.pangaea.idothecooking.utils.formatting

import android.content.Context
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.state.db.entities.ShoppingListDetails
import com.pangaea.idothecooking.state.db.entities.ShoppingListItem
import com.pangaea.idothecooking.utils.extensions.readContentFromAssets
import com.pangaea.idothecooking.utils.extensions.vulgarFraction

class ShoppingListRenderer(private val context: Context, private val shoppingListDetails: ShoppingListDetails) {

    fun drawShoppingListHtml(): String {
        val shoppingListHtml = context.readContentFromAssets("templates/shopping_list.html")
        val shoppingListItemHtml = context.readContentFromAssets("templates/shopping_list_item.html")
        return renderShoppingList(shoppingListHtml, shoppingListItemHtml)
    }

    fun drawShoppingListText(): String {
        val shoppingListTxt = context.readContentFromAssets("templates/shopping_list.txt")
        val shoppingListItemTxt = context.readContentFromAssets("templates/shopping_list_item.txt")
        return renderShoppingList(shoppingListTxt, shoppingListItemTxt)
    }

    private fun renderShoppingList(templateContent: String, ingredientTemplateContent: String): String {
        val itemBuilder = StringBuilder()
        //val htmlRecipeIngredient = context.resources.getString(ingredientTemplate)
        val items = shoppingListDetails.shoppingListItems.toMutableList()
        items.forEach { ingredient: ShoppingListItem ->
            do {
                if (ingredient.amount != null && ingredient.amount!! > 0f) {
                    val frac: Pair<String, Double>? = ingredient.amount?.vulgarFraction
                    if (frac != null) {
                        val amount = frac.first + " " + ingredient.unit
                        itemBuilder.append(ingredientTemplateContent.replace("{{amount}}", amount)
                                                     .replace("{{name}}", ingredient.name))
                        break;
                    }
                }

                itemBuilder.append(ingredientTemplateContent.replace("{{amount}}", "")
                                             .replace("{{name}}", ingredient.name))
            } while (false)
        }

        //var htmlRecipe = context.resources.getString(template)
        val htmlRecipe = templateContent.replace("{{title}}", shoppingListDetails.shoppingList.name)
            .replace("{{items}}", itemBuilder.toString())
        return htmlRecipe
    }
}