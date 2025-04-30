package com.pangaea.idothecooking.ui.recipe.viewmodels

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pangaea.idothecooking.state.RecipeRepository
import com.pangaea.idothecooking.state.db.entities.Recipe
import com.pangaea.idothecooking.state.db.entities.RecipeDetails
import com.pangaea.idothecooking.ui.shared.DisplayException
import kotlinx.coroutines.launch
import java.util.Optional
import java.util.function.Consumer

class RecipeViewModel(val app: Application, private val recipeId: Long?) : ViewModel() {

    private val recipeRepository = RecipeRepository(app)

    fun getAllRecipes(): LiveData<List<Recipe>> {
        return recipeRepository.getAllRecipes()
    }
    fun getAllRecipesWithDetails(): LiveData<List<RecipeDetails>> {
        return recipeRepository.getAllRecipesWithDetails()
    }

    fun getAllFavoriteRecipesWithDetails(): LiveData<List<RecipeDetails>> {
        return recipeRepository.getAllFavoriteRecipesWithDetails()
    }

    fun getRecipe(): LiveData<List<Recipe>>? {
        return recipeId?.let { recipeRepository.getRecipe(it) }
    }
    fun getDetails(): LiveData<List<RecipeDetails>>? {
        return recipeId?.let { recipeRepository.getRecipeWithDetails(it) }
    }
    fun getRecipeByName(name: String): LiveData<List<Recipe>> {
        return recipeRepository.getRecipeByName(name)
    }
//    fun handleErrors(callback: () -> Long): Long {
//        try {
//            return callback()
//        } catch(e: Exception) {
//            Toast.makeText(app.baseContext, e.message, Toast.LENGTH_LONG).show()
//        }
//        return -1
//    }
    @RequiresApi(Build.VERSION_CODES.N)
    fun insert(recipe: RecipeDetails, callback: Consumer<Long>) = viewModelScope.launch {
        try {
            val id = recipeRepository.insert(recipe)
            Optional.ofNullable(callback).ifPresent { o: Consumer<Long> ->
                o.accept(id)
            }
        } catch(e: Exception) {
            DisplayException.show(app.baseContext, e)
        }
    }
    @RequiresApi(Build.VERSION_CODES.N)
    fun update(recipe: RecipeDetails, callback: Consumer<Long>?) = viewModelScope.launch {
        try {
            val id = recipeRepository.update(recipe)
            Optional.ofNullable(callback).ifPresent { o: Consumer<Long> ->
                o.accept(id)
            }
        } catch(e: Exception) {
            DisplayException.show(app.baseContext, e)
        }
    }
    @RequiresApi(Build.VERSION_CODES.N)
    fun update(recipe: Recipe, callback: Consumer<Long>?) = viewModelScope.launch {
        try {
            val id = recipeRepository.update(recipe)
            Optional.ofNullable(callback).ifPresent { o: Consumer<Long> ->
                o.accept(id)
            }
        } catch(e: Exception) {
            DisplayException.show(app.baseContext, e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun updateFavorite(recipe: Recipe, callback: Consumer<Long>?) = viewModelScope.launch {
        try {
            val id = recipeRepository.updateFavorite(recipe)
            Optional.ofNullable(callback).ifPresent { o: Consumer<Long> ->
                o.accept(id)
            }
        } catch(e: Exception) {
            DisplayException.show(app.baseContext, e)
        }
    }

    fun delete(recipe: Recipe) = viewModelScope.launch {
        try {
            recipeRepository.delete(recipe)
        } catch(e: Exception) {
            DisplayException.show(app.baseContext, e)
        }
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