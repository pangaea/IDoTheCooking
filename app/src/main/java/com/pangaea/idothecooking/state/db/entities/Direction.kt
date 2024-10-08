package com.pangaea.idothecooking.state.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonIgnore

@Entity(foreignKeys = [ForeignKey(entity = Recipe::class,
                                  parentColumns = arrayOf("id"),
                                  childColumns = arrayOf("recipe_id"),
                                  onDelete = ForeignKey.CASCADE)],
        tableName = "directions")
data class Direction (
    @JsonIgnore
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    @JsonIgnore
    @ColumnInfo(index = true)
    var recipe_id: Int = 0,

    @ColumnInfo(name = "order")
    var order: Int = 0,

    @ColumnInfo(name = "content")
    var content: String = ""
)