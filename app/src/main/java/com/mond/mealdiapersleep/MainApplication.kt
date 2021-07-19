package com.mond.mealdiapersleep

import android.app.Application
import com.mond.mealdiapersleep.data.EventDatabase
import com.mond.mealdiapersleep.data.EventRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class MainApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { EventDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { EventRepository(database.eventDao()) }
}