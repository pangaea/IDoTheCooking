package com.pangaea.idothecooking.ui.category.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pangaea.idothecooking.state.CategoryRepository
import com.pangaea.idothecooking.state.db.entities.Category
import com.pangaea.idothecooking.state.db.entities.RecipeCategoryLink
import kotlinx.coroutines.launch
import java.util.Optional
import java.util.function.Consumer

class CategoryViewModel(app: Application, private val categoryId: Long?) : ViewModel() {
    private val categoryRepository = CategoryRepository(app)

    fun getAllCategories(): LiveData<List<Category>> {
        return categoryRepository.getAllCategories()
    }

    fun getAllLinkedRecipe(id: Int): LiveData<List<RecipeCategoryLink>> {
        return categoryRepository.getAllRecipeLinks(id)
    }

    fun insert(category: Category, callback: Consumer<Long>) = viewModelScope.launch {
        val id: Long = categoryRepository.insert(category)
        Optional.ofNullable(callback).ifPresent { o: Consumer<Long> ->
            o.accept(id)
        }
    }
    fun bulkInsert(categories: List<Category>, callback: Consumer<List<Long>>) = viewModelScope.launch {
        val ids: List<Long> = categoryRepository.bulkInsert(categories)
        Optional.ofNullable(callback).ifPresent { o: Consumer<List<Long>> ->
            o.accept(ids)
        }
    }
    fun update(category: Category) = viewModelScope.launch {
        categoryRepository.update(category)
    }
    fun delete(category: Category) = viewModelScope.launch {
        categoryRepository.delete(category)
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