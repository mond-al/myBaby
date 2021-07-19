package com.mond.mealdiapersleep.data

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

class EventRepository(private val eventDao: EventDao) {
    val allEvent: Flow<List<Event>> = eventDao.getAll()
    val today: Flow<List<Event>> = eventDao.getFromToNow(getTodayStart())

    @WorkerThread
    suspend fun insert(event: Event) = eventDao.insert(event)
    @WorkerThread
    suspend fun getPrevEvent(eventType: EventType, start:LocalDateTime): Event? = eventDao.getPrevEvent(eventType,start)
    @WorkerThread
    suspend fun delete(mId: Int) = eventDao.delete(mId)

    private fun getTodayStart(): LocalDateTime = LocalDateTime.now().minusDays(1)
}
