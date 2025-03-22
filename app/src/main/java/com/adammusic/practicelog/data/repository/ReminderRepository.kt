package com.adammusic.practicelog.data.repository

import com.adammusic.practicelog.data.db.dao.ReminderDao
import com.adammusic.practicelog.data.db.entities.ReminderEntity
import com.adammusic.practicelog.data.model.Reminder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepository @Inject constructor(
    private val reminderDao: ReminderDao
) {
    fun getAllReminders(): Flow<List<Reminder>> {
        return reminderDao.getAllReminders().map { entities ->
            entities.map { it.toReminder() }
        }
    }
    
    suspend fun getReminderById(id: Int): Reminder? {
        return reminderDao.getReminderById(id)?.toReminder()
    }
    
    suspend fun addReminder(days: List<Int>, hourOfDay: Int, minute: Int): Long {
        val entity = ReminderEntity(
            days = days.joinToString(","),
            hourOfDay = hourOfDay,
            minute = minute
        )
        return reminderDao.insertReminder(entity)
    }
    
    suspend fun updateReminder(reminder: Reminder) {
        reminderDao.updateReminder(reminder.toEntity())
    }
    
    suspend fun deleteReminder(id: Int) {
        reminderDao.deleteReminderById(id)
    }
    
    suspend fun toggleReminderEnabled(id: Int, isEnabled: Boolean) {
        val reminder = reminderDao.getReminderById(id) ?: return
        reminderDao.updateReminder(reminder.copy(isEnabled = isEnabled))
    }
    
    private fun ReminderEntity.toReminder(): Reminder {
        return Reminder(
            id = id,
            days = days.split(",").filter { it.isNotEmpty() }.map { it.toInt() },
            hourOfDay = hourOfDay,
            minute = minute,
            isEnabled = isEnabled
        )
    }
    
    private fun Reminder.toEntity(): ReminderEntity {
        return ReminderEntity(
            id = id,
            days = days.joinToString(","),
            hourOfDay = hourOfDay,
            minute = minute,
            isEnabled = isEnabled
        )
    }
} 