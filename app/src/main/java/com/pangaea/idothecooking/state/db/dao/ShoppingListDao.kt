package com.pangaea.idothecooking.state.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.pangaea.idothecooking.state.db.entities.ShoppingList
import com.pangaea.idothecooking.state.db.entities.ShoppingListDetails

@Dao
interface ShoppingListDao {
    @Query("SELECT * FROM shopping_lists ORDER BY name COLLATE NOCASE ASC")
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

    @Delete
    suspend fun delete(shoppingList: ShoppingList)
}