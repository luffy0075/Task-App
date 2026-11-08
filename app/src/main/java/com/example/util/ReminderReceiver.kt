package com.example.util

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_TASK_REMINDER = "com.example.reboot30.ACTION_TASK_REMINDER"
        const val ACTION_SNOOZE = "com.example.reboot30.ACTION_SNOOZE"
        const val ACTION_SMART_REMINDER = "com.example.reboot30.ACTION_SMART_REMINDER"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_TASK_REMINDER -> {
                val reminderId = intent.getLongExtra(NotificationHelper.EXTRA_REMINDER_ID, 0L)
                val taskId = intent.getLongExtra(NotificationHelper.EXTRA_TASK_ID, 0L)
                val taskTitle = intent.getStringExtra(NotificationHelper.EXTRA_TASK_TITLE) ?: "Task Reminder"

                NotificationHelper.showTaskReminder(
                    context = context,
                    reminderId = reminderId,
                    taskId = taskId,
                    taskTitle = taskTitle,
                    snoozeAllowed = true
                )
            }
            ACTION_SNOOZE -> {
                val reminderId = intent.getLongExtra(NotificationHelper.EXTRA_REMINDER_ID, 0L)
                val taskId = intent.getLongExtra(NotificationHelper.EXTRA_TASK_ID, 0L)
                val taskTitle = intent.getStringExtra(NotificationHelper.EXTRA_TASK_TITLE) ?: "Task Reminder"

                // Dismiss current notification
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.cancel(reminderId.toInt())

                // Schedule snooze in 15 minutes
                NotificationHelper.scheduleSnooze(
                    context = context,
                    reminderId = reminderId,
                    taskId = taskId,
                    taskTitle = taskTitle,
                    snoozeMinutes = 15
                )
            }
            ACTION_SMART_REMINDER -> {
                val type = intent.getStringExtra(NotificationHelper.EXTRA_NOTIFICATION_TYPE) ?: NotificationHelper.TYPE_MORNING
                val title = when (type) {
                    NotificationHelper.TYPE_MORNING -> "☀️ Morning Mindset"
                    NotificationHelper.TYPE_AFTERNOON -> "⚡ Afternoon Momentum"
                    NotificationHelper.TYPE_EVENING -> "🌙 Evening Discipline Check"
                    else -> "Reboot 30 Reminder"
                }
                val msg = when (type) {
                    NotificationHelper.TYPE_MORNING -> "Win your morning to win your day. Check your first disciplined tasks."
                    NotificationHelper.TYPE_AFTERNOON -> "Midday check-in! Keep your focus unbroken and log your progress."
                    NotificationHelper.TYPE_EVENING -> "Review today's accomplishments. Finish remaining tasks to secure your streak."
                    else -> "Stay disciplined today!"
                }
                NotificationHelper.showSmartNotification(context, type, title, msg)
            }
        }
    }
}
