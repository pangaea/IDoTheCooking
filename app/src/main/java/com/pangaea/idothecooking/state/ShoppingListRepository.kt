package com.pangaea.idothecooking.state

import android.app.Application
import androidx.lifecycle.LiveData
import com.pangaea.idothecooking.IDoTheCookingApp
import com.pangaea.idothecooking.state.db.entities.ShoppingList
import com.pangaea.idothecooking.state.db.entities.ShoppingListDetails
import com.pangaea.idothecooking.state.db.entities.converters.TimestampConverter

class ShoppingListRepository(application: Application) : RepositoryBase<ShoppingList>() {
    val db = (application as IDoTheCookingApp).getDatabase()
    private val shoppingListDao = db.shoppingListDao()!!
    private val shoppingListItemDao = db.shoppingListItemDao()!!

    fun getAllShoppingLists(): LiveData<List<ShoppingList>> {
        return shoppingListDao.loadAllShoppingLists()
    }

    fun getAllShoppingListsWithDetails(): LiveData<List<ShoppingListDetails>> {
        return shoppingListDao.loadAllShoppingListsWithDetails()
    }

    fun getShoppingList(id: Long): LiveData<List<ShoppingList>> {
        return shoppingListDao.loadShoppingListsByIds(intArrayOf(id.toInt()))
    }

    fun getShoppingListWithDetails(id: Long): LiveData<List<ShoppingListDetails>> {
        return shoppingListDao.loadShoppingListWithDetailsByIds(intArrayOf(id.toInt()))
    }

    suspend fun insert(shoppingList: ShoppingListDetails): Long {
        insertWithTimestamp(shoppingList.shoppingList)
        return shoppingListDao.createAll(shoppingListItemDao, shoppingList).toLong()
    }

    suspend fun update(shoppingList: ShoppingListDetails): Long {
        val tc = TimestampConverter()
        updateWithTimestamp(shoppingList.shoppingList)
        return shoppingListDao.updateAll(shoppingListItemDao, shoppingList,
                                         tc.dateToTimestamp(shoppingList.shoppingList.modifiedAt)).toLong()
    }

    suspend fun delete(shoppingList: ShoppingList) {
        shoppingListDao.delete(shoppingList)
    }
}