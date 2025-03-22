package com.adammusic.practicelog.di

import android.content.Context
import com.adammusic.practicelog.data.db.PracticeDatabase
import com.adammusic.practicelog.data.db.dao.CategoryDao
import com.adammusic.practicelog.data.db.dao.PracticeDao
import com.adammusic.practicelog.data.db.dao.ReminderDao
import com.adammusic.practicelog.data.db.dao.SessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PracticeDatabase {
        return PracticeDatabase.getDatabase(context)
    }

    @Provides
    fun provideCategoryDao(database: PracticeDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    fun providePracticeDao(database: PracticeDatabase): PracticeDao {
        return database.practiceDao()
    }

    @Provides
    fun provideSessionDao(database: PracticeDatabase): SessionDao {
        return database.sessionDao()
    }
    
    @Provides
    fun provideReminderDao(database: PracticeDatabase): ReminderDao {
        return database.reminderDao()
    }
} 