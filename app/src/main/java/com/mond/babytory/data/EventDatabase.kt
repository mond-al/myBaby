package com.mond.babytory.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [Event::class], version = 1)
@TypeConverters(DateConverters::class)
abstract class EventDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    private class EventDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database.eventDao())
                }
            }
        }

        suspend fun populateDatabase(eventDao: EventDao) {
            eventDao.deleteAll()

        }
    }

    companion object {
        @Volatile
        var INSTANCE: EventDatabase? = null
        fun getDatabase(context: Context, scope: CoroutineScope): EventDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EventDatabase::class.java,
                    "event_database"
                ).addCallback(EventDatabaseCallback(scope))
                    .allowMainThreadQueries().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
