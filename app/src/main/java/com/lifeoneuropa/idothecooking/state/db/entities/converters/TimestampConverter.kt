package com.lifeoneuropa.idothecooking.state.db.entities.converters

import androidx.room.TypeConverter
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

class TimestampConverter {
    private val df: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    @TypeConverter
    fun fromTimestamp(value: String?): Date? {
        return if (value != null) {
            try {
                val timeZone = TimeZone.getTimeZone("UTC")
                df.timeZone = timeZone
                return df.parse(value)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            Date(0)
        } else {
            Date(0)
        }
    }


    @TypeConverter
    fun dateToTimestamp(value: Date?): String? {
        val timeZone = TimeZone.getTimeZone("UTC")
        df.timeZone = timeZone
        return if (value == null) null else df.format(value)
    }
}