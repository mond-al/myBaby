package com.mond.babytory.ui.main

import androidx.lifecycle.*
import com.mond.babytory.data.Event
import com.mond.babytory.data.EventRepository
import com.mond.babytory.data.EventType
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime

class MainViewModel(private val repository: EventRepository) : ViewModel() {

    val in48Hours: LiveData<List<Event>> = repository.in48hours.asLiveData()

    fun add(type: EventType, time: LocalDateTime, callback: Runnable? = null, volume: Int = 0) {
        viewModelScope.launch {
            repository.insert(Event(type, time, volume, getPid(type, time)))
            callback?.run()
        }
    }

    suspend fun getPrevEvent(type: EventType, time: LocalDateTime): Event? {
        return repository.getPrevEvent(type, time)
    }

    private suspend fun getPid(type: EventType, time: LocalDateTime): Int? {
        return repository.getPrevEvent(type, time)?.mId
    }

    fun delete(mId: Int) {
        viewModelScope.launch {
            repository.delete(mId)
        }
    }

    fun getLastEvent(eventType: EventType): Event? =
        repository.getIn48Hours().filter { it.type == eventType }.maxByOrNull { it.start }

    fun getNextGap(eventType: EventType, term: Long): Duration {
        val lastEvent = getLastEvent(eventType) ?: return Duration.ZERO
        return Duration.between(LocalDateTime.now(), lastEvent.start.plusMinutes(term))
    }

}