package com.lifeoneuropa.idothecooking.ui.shoppinglist.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lifeoneuropa.idothecooking.state.ShoppingListRepository
import com.lifeoneuropa.idothecooking.state.db.entities.ShoppingList
import com.lifeoneuropa.idothecooking.state.db.entities.ShoppingListDetails
import com.lifeoneuropa.idothecooking.ui.shared.DisplayException
import kotlinx.coroutines.launch
import java.util.Optional
import java.util.function.Consumer

class ShoppingListViewModel(val app: Application,
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
        try {
            val id = shoppingListRepository.insert(shoppingList)
            Optional.ofNullable(callback).ifPresent { o: Consumer<Long> ->
                o.accept(id)
            }
        } catch(e: Exception) {
            DisplayException.show(app.baseContext, e)
        }
    }
    fun update(shoppingList: ShoppingListDetails, callback: Consumer<Long>) = viewModelScope.launch {
        try {
            val id = shoppingListRepository.update(shoppingList)
            Optional.ofNullable(callback).ifPresent { o: Consumer<Long> ->
                o.accept(id)
            }
        } catch(e: Exception) {
            DisplayException.show(app.baseContext, e)
        }
    }
    fun delete(shoppingList: ShoppingList) = viewModelScope.launch {
        try {
            shoppingListRepository.delete(shoppingList)
        } catch(e: Exception) {
            DisplayException.show(app.baseContext, e)
        }
    }
}

class ShoppingListViewModelFactory(private val app: Application,
                                   private val shoppingListId: Long?) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShoppingListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ShoppingListViewModel(app, shoppingListId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}