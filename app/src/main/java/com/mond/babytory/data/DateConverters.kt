package com.mond.babytory.data

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.ZoneOffset

class DateConverters {
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? {
        return value?.let { LocalDateTime.ofEpochSecond(it,0,ZoneOffset.UTC) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): Long? {
        return date?.toEpochSecond(ZoneOffset.UTC)
    }

    @TypeConverter
    fun getTypeBy(value: String?): EventType {
        return EventType.valueOf(value)
    }

    @TypeConverter
    fun getStringBy(type: EventType?): String {
        return type.toString()
    }
}