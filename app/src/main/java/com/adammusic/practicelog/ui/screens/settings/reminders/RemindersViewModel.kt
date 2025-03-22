package com.adammusic.practicelog.ui.screens.settings.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adammusic.practicelog.data.model.Reminder
import com.adammusic.practicelog.data.repository.ReminderRepository
import com.adammusic.practicelog.notification.NotificationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RemindersViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val notificationService: NotificationService
) : ViewModel() {
    
    val reminders = reminderRepository.getAllReminders().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    fun addReminder(days: List<Int>, hourOfDay: Int, minute: Int) {
        viewModelScope.launch {
            val reminderId = reminderRepository.addReminder(days, hourOfDay, minute)
            val reminder = reminderRepository.getReminderById(reminderId.toInt())
            reminder?.let {
                notificationService.scheduleReminder(it)
            }
        }
    }
    
    fun deleteReminder(id: Int) {
        viewModelScope.launch {
            notificationService.cancelReminder(id)
            reminderRepository.deleteReminder(id)
        }
    }
    
    fun toggleReminderEnabled(id: Int, isEnabled: Boolean) {
        viewModelScope.launch {
            reminderRepository.toggleReminderEnabled(id, isEnabled)
            val reminder = reminderRepository.getReminderById(id)
            reminder?.let {
                if (isEnabled) {
                    notificationService.scheduleReminder(it)
                } else {
                    notificationService.cancelReminder(id)
                }
            }
        }
    }
} 