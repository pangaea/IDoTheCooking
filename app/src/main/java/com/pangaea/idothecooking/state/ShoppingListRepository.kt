package com.pangaea.idothecooking.state

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.room.Transaction
import com.pangaea.idothecooking.IDoTheCookingApp
import com.pangaea.idothecooking.state.db.AppDatabase
import com.pangaea.idothecooking.state.db.dao.RecipeCategoryLinkDao
import com.pangaea.idothecooking.state.db.dao.RecipeDao
import com.pangaea.idothecooking.state.db.dao.RecipeDirectionDao
import com.pangaea.idothecooking.state.db.dao.RecipeIngredientDao
import com.pangaea.idothecooking.state.db.dao.ShoppingListDao
import com.pangaea.idothecooking.state.db.dao.ShoppingListItemDao
import com.pangaea.idothecooking.state.db.entities.Direction
import com.pangaea.idothecooking.state.db.entities.Recipe
import com.pangaea.idothecooking.state.db.entities.RecipeDetails
import com.pangaea.idothecooking.state.db.entities.ShoppingList
import com.pangaea.idothecooking.state.db.entities.ShoppingListDetails
import com.pangaea.idothecooking.state.db.entities.ShoppingListItem
import com.pangaea.idothecooking.state.db.entities.converters.TimestampConverter
import java.util.Optional
import java.util.function.Consumer

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

    @Transaction
    suspend fun insert(shoppingList: ShoppingListDetails): Long {
        val id: Long = shoppingListDao.insert(insertWithTimestamp(shoppingList.shoppingList))
        insertShoppingListItems(shoppingList.shoppingListItems, id.toInt())
        return id
    }

    @Transaction
    suspend fun update(shoppingList: ShoppingListDetails): Long {
        val tc = TimestampConverter()
        updateWithTimestamp(shoppingList.shoppingList)
        val id: Int = shoppingListDao.updateData(shoppingList.shoppingList.id.toLong(),
                                                 shoppingList.shoppingList.name, shoppingList.shoppingList.description,
                                                 tc.dateToTimestamp(shoppingList.shoppingList.modifiedAt))
        shoppingListItemDao.deleteAllByShoppingList(shoppingList.shoppingList.id)
        insertShoppingListItems(shoppingList.shoppingListItems, shoppingList.shoppingList.id)
        return id.toLong()
    }

    suspend fun delete(shoppingList: ShoppingList) {
        shoppingListDao.delete(shoppingList)
    }

    private suspend fun insertShoppingListItems(items: List<ShoppingListItem>, idNew: Int) {
        var i = 0
        val size = items.size
        while (i < size) {
            val item: ShoppingListItem = items[i]
            item.shopping_list_id = idNew
            item.order = i
            shoppingListItemDao.insert(item)
            i++
        }
    }
}