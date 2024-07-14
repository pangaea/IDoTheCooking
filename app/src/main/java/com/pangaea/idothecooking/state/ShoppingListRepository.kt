package com.pangaea.idothecooking.state

import android.app.Application
import androidx.lifecycle.LiveData
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

class ShoppingListRepository() : RepositoryBase<ShoppingList>() {
    lateinit var db: AppDatabase
    private lateinit var shoppingListDao: ShoppingListDao
    private lateinit var shoppingListItemDao: ShoppingListItemDao

    constructor(application: Application) : this() {
        db = (application as IDoTheCookingApp).getDatabase()
        shoppingListDao = db.shoppingListDao()!!
        shoppingListItemDao = db.shoppingListItemDao()!!
    }

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

    suspend fun insert(shoppingList: ShoppingListDetails, callback: Consumer<Long>?) {
        val id: Long = shoppingListDao.insert(insertWithTimestamp(shoppingList.shoppingList))
        insertShoppingListItems(shoppingList.shoppingListItems, id.toInt())
        Optional.ofNullable(callback).ifPresent { o: Consumer<Long> ->
            o.accept(id)
        }
    }

    suspend fun update(shoppingList: ShoppingListDetails, callback: Consumer<Long>?) {
        val tc = TimestampConverter()
        updateWithTimestamp(shoppingList.shoppingList)
        val id: Int = shoppingListDao.updateData(shoppingList.shoppingList.id.toLong(),
                                                 shoppingList.shoppingList.name, shoppingList.shoppingList.description,
                                                 tc.dateToTimestamp(shoppingList.shoppingList.modifiedAt))
        shoppingListItemDao.deleteAllByShoppingList(shoppingList.shoppingList.id)
        insertShoppingListItems(shoppingList.shoppingListItems, shoppingList.shoppingList.id)
        Optional.ofNullable(callback).ifPresent { o: Consumer<Long> ->
            o.accept(id.toLong())
        }
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
            shoppingListItemDao.insert(item)
            i++
        }
    }
}