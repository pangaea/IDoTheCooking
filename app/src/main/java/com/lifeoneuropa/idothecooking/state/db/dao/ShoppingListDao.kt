package com.lifeoneuropa.idothecooking.state.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.lifeoneuropa.idothecooking.state.db.entities.ShoppingList
import com.lifeoneuropa.idothecooking.state.db.entities.ShoppingListDetails
import com.lifeoneuropa.idothecooking.state.db.entities.ShoppingListItem

@Dao
interface ShoppingListDao {
    @Query("SELECT * FROM shopping_lists ORDER BY modified_at COLLATE NOCASE DESC")
    fun loadAllShoppingLists(): LiveData<List<ShoppingList>>

    @Transaction
    @Query("SELECT * FROM shopping_lists ORDER BY modified_at COLLATE NOCASE DESC")
    fun loadAllShoppingListsWithDetails(): LiveData<List<ShoppingListDetails>>

    @Query("SELECT * FROM shopping_lists WHERE id IN (:shoppingListIds)")
    fun loadShoppingListsByIds(shoppingListIds: IntArray): LiveData<List<ShoppingList>>

    @Transaction
    @Query("SELECT * FROM shopping_lists WHERE id IN (:shoppingListIds) order by modified_at COLLATE NOCASE desc")
    fun loadShoppingListWithDetailsByIds(shoppingListIds: IntArray): LiveData<List<ShoppingListDetails>>

    @Insert
    suspend fun insert(shoppingList: ShoppingList): Long

    @Update
    suspend fun update(shoppingList: ShoppingList)

    @Query("UPDATE shopping_lists SET name = :name, description = :description, modified_at = :modifiedAt WHERE id = :id")
    suspend fun updateData(id: Long, name: String?, description: String?, modifiedAt: String?): Int

    @Transaction
    suspend fun createAll(shoppingListItemDao: ShoppingListItemDao, shoppingList: ShoppingListDetails): Int {
        val id: Long = insert(shoppingList.shoppingList)
        insertShoppingListItems(shoppingListItemDao, shoppingList.shoppingListItems, id.toInt())
        return id.toInt()
    }
    @Transaction
    suspend fun updateAll(shoppingListItemDao: ShoppingListItemDao, shoppingList: ShoppingListDetails, modifiedAt: String?): Int {
        val id: Int = updateData(shoppingList.shoppingList.id.toLong(), shoppingList.shoppingList.name,
                                 shoppingList.shoppingList.description, modifiedAt)
        shoppingListItemDao.deleteAllByShoppingList(shoppingList.shoppingList.id)
        insertShoppingListItems(shoppingListItemDao, shoppingList.shoppingListItems, shoppingList.shoppingList.id)
        return id
    }

    @Delete
    suspend fun delete(shoppingList: ShoppingList)

    private suspend fun insertShoppingListItems(shoppingListItemDao: ShoppingListItemDao,
                                                items: List<ShoppingListItem>, idNew: Int) {
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