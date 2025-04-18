package com.lifeoneuropa.idothecooking.state.db.entities

import androidx.room.ColumnInfo
import androidx.room.TypeConverters
import com.fasterxml.jackson.annotation.JsonIgnore
import com.lifeoneuropa.idothecooking.state.db.entities.converters.TimestampConverter
import java.util.Date

open class BaseEntity {
    @JsonIgnore
    @ColumnInfo(name = "created_at")
    @TypeConverters(TimestampConverter::class)
    lateinit var createdAt: Date

    @JsonIgnore
    @ColumnInfo(name = "modified_at")
    @TypeConverters(TimestampConverter::class)
    lateinit var modifiedAt: Date
}