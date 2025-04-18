package com.lifeoneuropa.idothecooking.state.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(foreignKeys = [ForeignKey(entity = Recipe::class,
                                  parentColumns = arrayOf("id"),
                                  childColumns = arrayOf("recipe_id"),
                                  onDelete = ForeignKey.CASCADE), ForeignKey(entity = Category::class,
                                                                             parentColumns = arrayOf("id"),
                                                                             childColumns = arrayOf("category_id"),
                                                                             onDelete = ForeignKey.CASCADE)],
        indices = [Index(value = ["recipe_id", "category_id"], unique = true)],
        tableName = "recipe_category_links")
data class RecipeCategoryLink(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    @ColumnInfo(index = true)
    var recipe_id: Int = 0,

    @ColumnInfo(index = true)
    var category_id: Int = 0,
)
