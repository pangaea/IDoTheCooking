package com.pangaea.idothecooking.state.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonIgnore

@Entity(foreignKeys = [ForeignKey(entity = ShoppingList::class,
                                  parentColumns = arrayOf("id"),
                                  childColumns = arrayOf("shopping_list_id"),
                                  onDelete = ForeignKey.CASCADE)],
        tableName = "shopping_list_items")
data class ShoppingListItem (
    @JsonIgnore
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    @JsonIgnore
    @ColumnInfo(index = true)
    var shopping_list_id: Int = 0,

    @ColumnInfo(name = "order")
    var order: Int = 0,

    @ColumnInfo(name = "checked")
    var checked: Boolean = false
) : MeasuredItem()