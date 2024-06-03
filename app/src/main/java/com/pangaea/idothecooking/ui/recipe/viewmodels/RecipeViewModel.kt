package com.pangaea.idothecooking.ui.recipe.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pangaea.idothecooking.state.RecipeRepository
import com.pangaea.idothecooking.state.db.entities.Recipe
import com.pangaea.idothecooking.state.db.entities.RecipeDetails
import kotlinx.coroutines.launch
import java.util.function.Consumer

class RecipeViewModel(private val recipeRepository: RecipeRepository, private val recipeId: Long?) : ViewModel() {
    //val allRecipes: LiveData<List<Recipe>> = recipeRepository.allRecipes
    fun getAllRecipes(): LiveData<List<Recipe>> {
        return recipeRepository.getAllRecipes()
    }
    fun getAllRecipesWithDetails(): LiveData<List<RecipeDetails>> {
        return recipeRepository.getAllRecipesWithDetails()
    }
    fun getRecipe(): LiveData<List<Recipe>>? {
        return recipeId?.let { recipeRepository.getRecipe(it) }
    }
    fun getDetails(): LiveData<List<RecipeDetails>>? {
        return recipeId?.let { recipeRepository.getRecipeWithDetails(it) }
    }
    fun insert(recipe: RecipeDetails, callback: Consumer<Long>) = viewModelScope.launch {
        recipeRepository.insert(recipe, callback)
    }
    fun update(recipe: RecipeDetails) = viewModelScope.launch {
        recipeRepository.update(recipe)
    }
    fun delete(recipe: Recipe) = viewModelScope.launch {
        recipeRepository.delete(recipe)
    }
}

class RecipeViewModelFactory(private val recipeRepository: RecipeRepository, private val recipeId: Long?) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecipeViewModel(recipeRepository, recipeId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}