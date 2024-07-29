package com.pangaea.idothecooking.ui.category.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pangaea.idothecooking.state.CategoryRepository
import com.pangaea.idothecooking.state.RecipeRepository
import com.pangaea.idothecooking.state.ShoppingListRepository
import com.pangaea.idothecooking.state.db.entities.Category
import com.pangaea.idothecooking.state.db.entities.Recipe
import com.pangaea.idothecooking.state.db.entities.RecipeDetails
import com.pangaea.idothecooking.ui.recipe.viewmodels.RecipeViewModel
import kotlinx.coroutines.launch
import java.util.function.Consumer

class CategoryViewModel(app: Application, private val recipeId: Long?) : ViewModel() {
    private val categoryRepository = CategoryRepository(app)

    fun getAllCategories(): LiveData<List<Category>> {
        return categoryRepository.getAllCategories()
    }

    fun insert(category: Category, callback: Consumer<Long>) = viewModelScope.launch {
        categoryRepository.insert(category, callback)
    }
    fun update(category: Category) = viewModelScope.launch {
        categoryRepository.update(category)
    }
    fun delete(category: Category) = viewModelScope.launch {
        categoryRepository.delete(category)
    }
}

class CategoryViewModelFactory(val app: Application, private val recipeId: Long?) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoryViewModel(app, recipeId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}