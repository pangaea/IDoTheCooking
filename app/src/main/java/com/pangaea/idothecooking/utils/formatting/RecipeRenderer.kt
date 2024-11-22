package com.pangaea.idothecooking.utils.formatting

import android.content.Context
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.state.db.entities.Direction
import com.pangaea.idothecooking.state.db.entities.Ingredient
import com.pangaea.idothecooking.state.db.entities.RecipeDetails
import com.pangaea.idothecooking.ui.shared.ImageTool
import com.pangaea.idothecooking.utils.extensions.vulgarFraction

class RecipeRenderer(private val context: Context, private val recipeDetails: RecipeDetails,
                     private val servingSize: Int, private val categoryMap: Map<Int, String>) {

    fun drawRecipeHtml(): String {
        return renderRecipe(R.string.html_recipe, R.string.html_recipe_ingredient,
                                      R.string.html_recipe_direction)
    }

    fun drawRecipeText(): String {
        return renderRecipe(R.string.text_recipe, R.string.text_recipe_ingredient,
                                      R.string.text_recipe_direction)
    }

    private fun renderRecipe(template: Int, ingredientTemplate: Int, directionTemplate: Int): String {
        val ingredientBuilder = StringBuilder()
        val htmlRecipeIngredient = context.resources.getString(ingredientTemplate)
        val ingredients = recipeDetails.ingredients.toMutableList()

        var adjRatio: Double = 1.0
        if (recipeDetails.recipe.servings > 0 && servingSize > 0) {
            // Avoid division by zero
            adjRatio = (servingSize.toDouble() / recipeDetails.recipe.servings)
        }
        ingredients.sortWith { obj1, obj2 ->
            Integer.valueOf(obj1.order).compareTo(Integer.valueOf(obj2.order))
        }
        ingredients.forEach { ingredient: Ingredient ->
            do {
                if (ingredient.amount != null && ingredient.amount!! > 0f) {
                    var adjAmount = ingredient.amount
                    if (adjRatio != 1.0) {
                        adjAmount = ingredient.amount!! * adjRatio
                    }
                    val frac: Pair<String, Double>? = adjAmount?.vulgarFraction
                    if (frac != null) {
                        val amount = frac.first + " " + ingredient.unit
                        ingredientBuilder.append(htmlRecipeIngredient.replace("{{amount}}", amount)
                                                     .replace("{{name}}", ingredient.name))
                        break;
                    }
                }

                ingredientBuilder.append(htmlRecipeIngredient.replace("{{amount}}", "")
                                             .replace("{{name}}", ingredient.name))
            } while (false)
        }

        val directionBuilder = StringBuilder()
        val htmlRecipeDirection = context.resources.getString(directionTemplate)
        val directions = recipeDetails.directions.toMutableList()
        directions.sortWith { obj1, obj2 ->
            Integer.valueOf(obj1.order).compareTo(Integer.valueOf(obj2.order))
        }
        directions.forEachIndexed { index, direction: Direction ->
            directionBuilder.append(htmlRecipeDirection.replace("{{content}}", direction.content)
                                        .replace("{{step}}", (index+1).toString()))
        }

        var imageElem = ""
        if (!recipeDetails.recipe.imageUri.isNullOrEmpty()) {
            if (recipeDetails.recipe.imageUri!!.startsWith(ImageTool.assetProtocol)) {
                val assetName = recipeDetails.recipe.imageUri!!.substring(ImageTool.assetProtocol.length)
                imageElem = "<img length=\"100px\" width=\"100px\" src=\""+  "file:///android_asset/${assetName}" + "\">";
            } else {
                imageElem =
                    "<img length=\"100px\" width=\"100px\" src=\"" + recipeDetails.recipe.imageUri.toString() + "\">";
            }
        }

        val htmlRecipeCategories =
            recipeDetails.categories.joinToString(", ") { o -> categoryMap[o.category_id]!! }

        var htmlRecipe = context.resources.getString(template)
        htmlRecipe = htmlRecipe.replace("{{title}}", recipeDetails.recipe.name).replace("{{description}}", recipeDetails.recipe.description)
            .replace("{{ingredients}}", ingredientBuilder.toString())
            .replace("{{directions}}", directionBuilder.toString())
            .replace("{{image}}", imageElem)
            .replace("{{categories}}", htmlRecipeCategories)

        if (recipeDetails.recipe.servings > 0) {
            htmlRecipe = htmlRecipe.replace("{{servings}}", servingSize.toString())
        } else {
            htmlRecipe = htmlRecipe.replace("{{servings}}", "?")
        }
        return htmlRecipe
    }
}