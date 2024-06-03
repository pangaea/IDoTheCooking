package com.pangaea.idothecooking.state.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "recipes",
    indices = [Index(value = ["name"], unique = true)])
data class Recipe (
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    @ColumnInfo(name = "name")
    var name: String = "",

    @ColumnInfo(name = "description")
    var description: String = "",

    @ColumnInfo(name = "imageUri")
    var imageUri: String? = "",

    @ColumnInfo(name = "servings")
    var servings: Int = 0,

) : BaseEntity()