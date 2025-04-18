package com.lifeoneuropa.idothecooking.state.db.entities

import androidx.room.ColumnInfo

open class MeasuredItem {
    @ColumnInfo(name = "name")
    var name: String = ""

    @ColumnInfo(name = "amount")
    var amount: Double? = 0.0

    @ColumnInfo(name = "unit")
    var unit: String = ""
}