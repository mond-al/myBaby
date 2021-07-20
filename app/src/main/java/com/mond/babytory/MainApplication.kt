package com.mond.babytory

import android.app.Application
import com.mond.babytory.data.EventDatabase
import com.mond.babytory.data.EventRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class MainApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { EventDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { EventRepository(database.eventDao()) }
}