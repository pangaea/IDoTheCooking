package com.lifeoneuropa.idothecooking.ui.recipe.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lifeoneuropa.idothecooking.state.db.entities.Category
import com.lifeoneuropa.idothecooking.state.db.entities.Direction
import com.lifeoneuropa.idothecooking.state.db.entities.Ingredient
import com.lifeoneuropa.idothecooking.state.db.entities.Recipe
import com.lifeoneuropa.idothecooking.state.db.entities.RecipeCategoryLink
import com.lifeoneuropa.idothecooking.state.db.entities.RecipeDetails

class SelectedRecipeModel : ViewModel() {
    private val mutableSelectedRecipe = MutableLiveData<RecipeDetails>()
    val selectedRecipe: LiveData<RecipeDetails> get() = mutableSelectedRecipe

    fun setRecipeDetails(item: RecipeDetails) {
        mutableSelectedRecipe.value = item
    }

    fun updateRecipe(item: Recipe) {
        val curRecipe = mutableSelectedRecipe.value
        curRecipe?.recipe = item
        mutableSelectedRecipe.value = curRecipe!!
    }

    fun updateRecipeIngredients(items: List<Ingredient>) {
        val curRecipe = mutableSelectedRecipe.value
        curRecipe?.ingredients = items
        mutableSelectedRecipe.value = curRecipe!!
    }

    fun updateRecipeDirections(items: List<Direction>) {
        val curRecipe = mutableSelectedRecipe.value
        curRecipe?.directions = items
        mutableSelectedRecipe.value = curRecipe!!
    }

    fun updateRecipeCategories(items: List<Category>) {
        val curRecipe = mutableSelectedRecipe.value
        curRecipe?.categories = items.map { o ->
            RecipeCategoryLink(0, curRecipe?.recipe?.id!!, o.id)
        }
        mutableSelectedRecipe.value = curRecipe!!
    }
}