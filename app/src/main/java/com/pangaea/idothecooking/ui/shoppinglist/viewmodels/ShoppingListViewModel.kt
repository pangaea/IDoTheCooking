package com.pangaea.idothecooking.ui.shoppinglist.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pangaea.idothecooking.IDoTheCookingApp
import com.pangaea.idothecooking.state.CategoryRepository
import com.pangaea.idothecooking.state.ShoppingListRepository
import com.pangaea.idothecooking.state.db.AppDatabase
import com.pangaea.idothecooking.state.db.entities.Recipe
import com.pangaea.idothecooking.state.db.entities.RecipeDetails
import com.pangaea.idothecooking.state.db.entities.ShoppingList
import com.pangaea.idothecooking.state.db.entities.ShoppingListDetails
import kotlinx.coroutines.launch
import java.util.function.Consumer

class ShoppingListViewModel(app: IDoTheCookingApp,
                            private val shoppingListId: Long?) : ViewModel() {

    private val shoppingListRepository = ShoppingListRepository(app)

    fun getAllShoppingLists(): LiveData<List<ShoppingList>> {
        return shoppingListRepository.getAllShoppingLists()
    }

    fun getAllShoppingListsWithDetails(): LiveData<List<ShoppingListDetails>> {
        return shoppingListRepository.getAllShoppingListsWithDetails()
    }

    fun getShoppingList(): LiveData<List<ShoppingList>>? {
        return shoppingListId?.let { shoppingListRepository.getShoppingList(it) }
    }
    fun getDetails(): LiveData<List<ShoppingListDetails>>? {
        return shoppingListId?.let { shoppingListRepository.getShoppingListWithDetails(it) }
    }

    fun insert(shoppingList: ShoppingListDetails, callback: Consumer<Long>) = viewModelScope.launch {
        shoppingListRepository.insert(shoppingList, callback)
    }
    fun update(shoppingList: ShoppingListDetails) = viewModelScope.launch {
        shoppingListRepository.update(shoppingList){}
    }
    fun delete(shoppingList: ShoppingList) = viewModelScope.launch {
        shoppingListRepository.delete(shoppingList)
    }
}

class ShoppingListViewModelFactory(private val app: IDoTheCookingApp,
                                   private val shoppingListId: Long?) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShoppingListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ShoppingListViewModel(app, shoppingListId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}