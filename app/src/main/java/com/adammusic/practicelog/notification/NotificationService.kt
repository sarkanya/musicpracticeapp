package com.adammusic.practicelog.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.adammusic.practicelog.MainActivity
import com.adammusic.practicelog.R
import com.adammusic.practicelog.data.model.Reminder
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    
    companion object {
        const val CHANNEL_ID = "practice_reminders"
        const val NOTIFICATION_ID = 1001
        const val REQUEST_CODE = 2001
    }
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        val name = "Practice Reminders"
        val descriptionText = "Notifications for practice reminders"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        notificationManager.createNotificationChannel(channel)
    }
    
    fun scheduleReminder(reminder: Reminder) {
        cancelReminder(reminder.id)
        
        if (!reminder.isEnabled || reminder.days.isEmpty()) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                return
            }
        }

        for (day in reminder.days) {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, mapDayToCalendarDay(day))
                set(Calendar.HOUR_OF_DAY, reminder.hourOfDay)
                set(Calendar.MINUTE, reminder.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 7)
            }
            
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra("REMINDER_ID", reminder.id)
                putExtra("DAY", day)
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminder.id * 10 + day,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY * 7,
                pendingIntent
            )
        }
    }
    
    fun cancelReminder(reminderId: Int) {
        for (day in 1..7) {
            val intent = Intent(context, ReminderReceiver::class.java)
            val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getBroadcast(
                    context,
                    reminderId * 10 + day,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                PendingIntent.getBroadcast(
                    context,
                    reminderId * 10 + day,
                    intent,
                    0
                )
            }
            
            pendingIntent?.let {
                alarmManager.cancel(it)
                it.cancel()
            }
        }
    }
    
    fun showNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Practice Reminder")
            .setContentText("Time to practice! Don't forget your daily practice session.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun mapDayToCalendarDay(day: Int): Int {
        return when (day) {
            1 -> Calendar.MONDAY
            2 -> Calendar.TUESDAY
            3 -> Calendar.WEDNESDAY
            4 -> Calendar.THURSDAY
            5 -> Calendar.FRIDAY
            6 -> Calendar.SATURDAY
            7 -> Calendar.SUNDAY
            else -> Calendar.MONDAY
        }
    }
} 