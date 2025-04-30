package com.pangaea.idothecooking.state.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonIgnore

@Entity(tableName = "categories",
    indices = [Index(value = ["name"], unique = true)])
data class Category(
    @JsonIgnore
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    @ColumnInfo(name = "name")
    var name: String = ""
) : BaseEntity()
