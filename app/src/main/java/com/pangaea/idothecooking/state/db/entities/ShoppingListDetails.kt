package com.pangaea.idothecooking.state.db.entities

import androidx.room.Embedded
import androidx.room.Relation

data class ShoppingListDetails (
    @Embedded
    val shoppingList: ShoppingList,
    @Relation(parentColumn = "id", entityColumn = "shopping_list_id")
    var shoppingListItems: List<ShoppingListItem>,
)