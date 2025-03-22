package com.adammusic.practicelog.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.adammusic.practicelog.data.repository.ReminderRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var reminderRepository: ReminderRepository
    
    @Inject
    lateinit var notificationService: NotificationService
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {
                val reminders = reminderRepository.getAllReminders().first()
                for (reminder in reminders) {
                    if (reminder.isEnabled) {
                        notificationService.scheduleReminder(reminder)
                    }
                }
            }
        }
    }
} 