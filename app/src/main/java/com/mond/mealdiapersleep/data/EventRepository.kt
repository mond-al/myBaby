package com.mond.mealdiapersleep.data

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

class EventRepository(private val eventDao: EventDao) {
    val allEvent: Flow<List<Event>> = eventDao.getAll()
    val in48hours: Flow<List<Event>> = eventDao.getFlowFromToNow(LocalDateTime.now().minusDays(2))
    fun getIn48Hours(): List<Event> = eventDao.getFromToNow(LocalDateTime.now().minusDays(2))

    @WorkerThread
    suspend fun insert(event: Event) = eventDao.insert(event)
    @WorkerThread
    suspend fun getPrevEvent(eventType: EventType, start:LocalDateTime): Event? = eventDao.getPrevEvent(eventType,start)
    @WorkerThread
    suspend fun delete(mId: Int) = eventDao.delete(mId)
}
