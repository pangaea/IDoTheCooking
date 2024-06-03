package com.pangaea.idothecooking.ui.recipe

import com.pangaea.idothecooking.state.db.entities.Category
import com.pangaea.idothecooking.state.db.entities.Direction
import com.pangaea.idothecooking.state.db.entities.Ingredient
import com.pangaea.idothecooking.state.db.entities.Recipe
import com.pangaea.idothecooking.state.db.entities.RecipeDetails

interface RecipeCallBackListener {
    fun getRecipeDetails(): RecipeDetails
    fun onRecipeInfoUpdate(recipe: Recipe)
    fun onRecipeCategories(categories: List<Category>)
    fun onRecipeDirectionUpdate(directions: List<Direction>)
    fun onRecipeIngredientUpdate(ingredients: List<Ingredient>)
}