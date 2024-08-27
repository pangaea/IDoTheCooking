package com.pangaea.idothecooking.state.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonIgnore

@Entity(tableName = "shopping_lists",
        indices = [Index(value = ["name"], unique = true)])
data class ShoppingList (
    @JsonIgnore
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    @ColumnInfo(name = "name")
    var name: String = "",

    @ColumnInfo(name = "description")
    var description: String = ""
) : BaseEntity()
