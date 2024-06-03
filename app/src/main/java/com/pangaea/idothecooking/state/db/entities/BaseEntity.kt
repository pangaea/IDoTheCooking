package com.pangaea.idothecooking.state.db.entities

import androidx.room.ColumnInfo
import androidx.room.TypeConverters
import com.pangaea.idothecooking.state.db.entities.converters.TimestampConverter
import java.util.Date

open class BaseEntity {
    @ColumnInfo(name = "created_at")
    @TypeConverters(TimestampConverter::class)
    lateinit var createdAt: Date

    @ColumnInfo(name = "modified_at")
    @TypeConverters(TimestampConverter::class)
    lateinit var modifiedAt: Date
}