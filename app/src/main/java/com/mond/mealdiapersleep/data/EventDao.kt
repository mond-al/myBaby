package com.mond.mealdiapersleep.data

import android.util.Log
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface EventDao {
    @Query("SELECT * FROM event order by mId ASC")
    fun getAll(): Flow<List<Event>>

    @Query("SELECT * FROM event WHERE type is (:type) AND start < (:start) order by start DESC Limit 1")
    suspend fun getPrevEvent(type: EventType, start: LocalDateTime): Event?

    @Query("SELECT * FROM event WHERE start >= :start AND start<= (:finish) order by start DESC")
    fun getFromTo(start: LocalDateTime, finish: LocalDateTime): Flow<List<Event>>

    @Query("SELECT * FROM event WHERE start >= :start order by start DESC")
    fun getFlowFromToNow(start: LocalDateTime): Flow<List<Event>>

    @Query("SELECT * FROM event WHERE start >= :start order by start DESC")
    fun getFromToNow(start: LocalDateTime): List<Event>

    @Query("SELECT * FROM event WHERE mId is (:eventId)")
    fun loadAllById(eventId: Int): List<Event>

    @Insert(onConflict = OnConflictStrategy.REPLACE)  // or OnConflictStrategy.IGNORE
    suspend fun insert(event: Event)

    @Query("DELETE FROM event")
    suspend fun deleteAll()

    @Query("DELETE FROM event WHERE mId is (:eventId)")
    suspend fun delete(eventId: Int)
}
    