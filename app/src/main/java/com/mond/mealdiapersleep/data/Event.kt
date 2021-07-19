package com.mond.mealdiapersleep.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity
data class Event constructor(
    @PrimaryKey(autoGenerate = true) val mId: Int=0,
    @ColumnInfo(name = "pId") val pId: Int ,
    @ColumnInfo(name = "type") val type: EventType,
    @ColumnInfo(name = "start") val start: LocalDateTime,
    @ColumnInfo(name = "volume") val volume: Int?
) {
    constructor(type: EventType, start: LocalDateTime, volume: Int?,pId: Int?) : this(
        0,
        pId?:-1,
        type,
        start,
        volume
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Event

        if (mId != other.mId) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = mId
        result = 31 * result + start.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }

    override fun toString(): String {
        return "Event(mId=$mId, pId=$pId, type=$type, start=$start, volume=$volume)"
    }

}