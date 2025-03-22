package com.adammusic.practicelog.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.adammusic.practicelog.data.db.dao.CategoryDao
import com.adammusic.practicelog.data.db.dao.PracticeDao
import com.adammusic.practicelog.data.db.dao.ReminderDao
import com.adammusic.practicelog.data.db.dao.SessionDao
import com.adammusic.practicelog.data.db.entities.*

@Database(
    entities = [
        CategoryEntity::class,
        PracticeEntity::class,
        SessionEntity::class,
        ReminderEntity::class
    ],
    version = 1
)
@TypeConverters(Converters::class)
abstract class PracticeDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun practiceDao(): PracticeDao
    abstract fun sessionDao(): SessionDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var INSTANCE: PracticeDatabase? = null

        fun getDatabase(context: Context): PracticeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PracticeDatabase::class.java,
                    "practice_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}