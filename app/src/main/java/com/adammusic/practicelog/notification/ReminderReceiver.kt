package com.adammusic.practicelog.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var notificationService: NotificationService
    
    override fun onReceive(context: Context, intent: Intent) {
        notificationService.showNotification()
    }
} 