package com.pangaea.idothecooking.ui.category.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pangaea.idothecooking.state.CategoryRepository
import com.pangaea.idothecooking.state.db.entities.Category
import com.pangaea.idothecooking.state.db.entities.RecipeCategoryLink
import com.pangaea.idothecooking.ui.shared.DisplayException
import kotlinx.coroutines.launch
import java.util.Optional
import java.util.function.Consumer

class CategoryViewModel(val app: Application, private val categoryId: Long?) : ViewModel() {
    private val categoryRepository = CategoryRepository(app)

    fun getAllCategories(): LiveData<List<Category>> {
        return categoryRepository.getAllCategories()
    }

    fun getAllLinkedRecipe(id: Int): LiveData<List<RecipeCategoryLink>> {
        return categoryRepository.getAllRecipeLinks(id)
    }

    fun insert(category: Category, callback: Consumer<Long>) = viewModelScope.launch {
        try {
            val id: Long = categoryRepository.insert(category)
            Optional.ofNullable(callback).ifPresent { o: Consumer<Long> ->
                o.accept(id)
            }
        } catch(e: Exception) {
            DisplayException.show(app.baseContext, e)
        }
    }
    fun bulkInsert(categories: List<Category>, callback: Consumer<List<Long>>) = viewModelScope.launch {
        try {
            val ids: List<Long> = categoryRepository.bulkInsert(categories)
            Optional.ofNullable(callback).ifPresent { o: Consumer<List<Long>> ->
                o.accept(ids)
            }
        } catch(e: Exception) {
            DisplayException.show(app.baseContext, e)
        }
    }
    fun update(category: Category) = viewModelScope.launch {
        try {
            categoryRepository.update(category)
        } catch(e: Exception) {
            DisplayException.show(app.baseContext, e)
        }
    }
    fun delete(category: Category) = viewModelScope.launch {
        try {
            categoryRepository.delete(category)
        } catch(e: Exception) {
            DisplayException.show(app.baseContext, e)
        }
    }
}

class CategoryViewModelFactory(val app: Application, private val categoryId: Long?) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoryViewModel(app, categoryId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}