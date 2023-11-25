package com.yveskalume.alertapp.database.converters

import androidx.room.TypeConverter
import java.util.Date

class Converters {

    @TypeConverter
    fun toDate(timeStamp: Long): Date {
        return Date(timeStamp)
    }

    @TypeConverter
    fun fromDate(date: Date): Long {
        return date.time
    }
}