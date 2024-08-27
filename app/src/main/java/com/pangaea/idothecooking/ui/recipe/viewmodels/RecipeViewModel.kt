package com.pangaea.idothecooking.ui.recipe.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pangaea.idothecooking.IDoTheCookingApp
import com.pangaea.idothecooking.state.RecipeRepository
import com.pangaea.idothecooking.state.ShoppingListRepository
import com.pangaea.idothecooking.state.db.entities.Recipe
import com.pangaea.idothecooking.state.db.entities.RecipeDetails
import kotlinx.coroutines.launch
import java.util.Optional
import java.util.function.Consumer

class RecipeViewModel(app: Application, private val recipeId: Long?) : ViewModel() {

    private val recipeRepository = RecipeRepository(app)

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
        val id = recipeRepository.insert(recipe)
        Optional.ofNullable(callback).ifPresent { o: Consumer<Long> ->
            o.accept(id)
        }
    }
    fun update(recipe: RecipeDetails, callback: Consumer<Long>?) = viewModelScope.launch {
        val id = recipeRepository.update(recipe)
        Optional.ofNullable(callback).ifPresent { o: Consumer<Long> ->
            o.accept(id)
        }
    }
    fun delete(recipe: Recipe) = viewModelScope.launch {
        recipeRepository.delete(recipe)
    }
}

class RecipeViewModelFactory(val app: Application, private val recipeId: Long?) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecipeViewModel(app, recipeId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}