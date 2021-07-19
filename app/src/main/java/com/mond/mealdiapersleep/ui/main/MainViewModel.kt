package com.mond.mealdiapersleep.ui.main

import androidx.lifecycle.*
import com.mond.mealdiapersleep.data.Event
import com.mond.mealdiapersleep.data.EventRepository
import com.mond.mealdiapersleep.data.EventType
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicBoolean

class MainViewModel(private val repository: EventRepository) : ViewModel() {

    val allData: LiveData<List<Event>> = repository.allEvent.asLiveData()
    val hour24: LiveData<List<Event>> = repository.today.asLiveData()
    var dirty = AtomicBoolean(true)

    fun add(type: EventType, time: LocalDateTime, volume: Int = 0) {
        viewModelScope.launch {
            repository.insert(Event(type, time, volume, getPid(type, time)))
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
}