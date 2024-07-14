package com.pangaea.idothecooking.state.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.pangaea.idothecooking.state.db.entities.ShoppingListItem

@Dao
interface ShoppingListItemDao {
    @Insert
    suspend fun insert(shoppingListItem: ShoppingListItem)

    @Query("DELETE FROM shopping_list_items WHERE shopping_list_id=:id")
    suspend fun deleteAllByShoppingList(id: Int)
}