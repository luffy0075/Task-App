package com.example.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import java.util.Calendar

object NotificationHelper {
    const val CHANNEL_TASKS = "reboot_channel_tasks"
    const val CHANNEL_STREAK = "reboot_channel_streak"
    const val CHANNEL_SMART = "reboot_channel_smart"
    const val CHANNEL_ACHIEVEMENTS = "reboot_channel_achievements"

    const val EXTRA_REMINDER_ID = "extra_reminder_id"
    const val EXTRA_TASK_ID = "extra_task_id"
    const val EXTRA_TASK_TITLE = "extra_task_title"
    const val EXTRA_NOTIFICATION_TYPE = "extra_notification_type"

    const val TYPE_MORNING = "morning"
    const val TYPE_AFTERNOON = "afternoon"
    const val TYPE_EVENING = "evening"
    const val TYPE_STREAK = "streak"

    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val tasksChannel = NotificationChannel(
                CHANNEL_TASKS,
                "Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily discipline task reminders and custom alarms"
                enableVibration(true)
                setShowBadge(true)
            }

            val streakChannel = NotificationChannel(
                CHANNEL_STREAK,
                "Streak & Deadline Warnings",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical streak-at-risk alerts and daily midnight deadlines"
                enableVibration(true)
                setShowBadge(true)
            }

            val smartChannel = NotificationChannel(
                CHANNEL_SMART,
                "Smart Motivation & Check-ins",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Morning mindset, afternoon progress check-ins, and evening reflections"
                setShowBadge(true)
            }

            val achievementChannel = NotificationChannel(
                CHANNEL_ACHIEVEMENTS,
                "Achievements & Level Ups",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Celebrations for leveling up, milestones, and unlocked rewards"
                enableVibration(true)
                setShowBadge(true)
            }

            manager.createNotificationChannels(
                listOf(tasksChannel, streakChannel, smartChannel, achievementChannel)
            )
        }
    }

    fun isQuietHours(startHour: Int, endHour: Int): Boolean {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        return if (startHour < endHour) {
            currentHour in startHour until endHour
        } else {
            currentHour >= startHour || currentHour < endHour
        }
    }

    private fun getLaunchPendingIntent(context: Context, reqCode: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            reqCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun showTaskReminder(
        context: Context,
        reminderId: Long,
        taskId: Long,
        taskTitle: String,
        snoozeAllowed: Boolean = true
    ) {
        initNotificationChannels(context)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val contentIntent = getLaunchPendingIntent(context, reminderId.toInt())

        val builder = NotificationCompat.Builder(context, CHANNEL_TASKS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Discipline Reminder")
            .setContentText("Time to complete: $taskTitle")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Keep your discipline strong today. Target task: $taskTitle. Small daily wins compound into legendary achievements!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)

        if (snoozeAllowed) {
            val snoozeIntent = Intent(context, ReminderReceiver::class.java).apply {
                action = ReminderReceiver.ACTION_SNOOZE
                putExtra(EXTRA_REMINDER_ID, reminderId)
                putExtra(EXTRA_TASK_ID, taskId)
                putExtra(EXTRA_TASK_TITLE, taskTitle)
            }
            val snoozePendingIntent = PendingIntent.getBroadcast(
                context,
                (reminderId + 100000).toInt(),
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.ic_lock_idle_alarm, "Snooze 15m", snoozePendingIntent)
        }

        manager.notify(reminderId.toInt(), builder.build())
    }

    fun showStreakAtRiskNotification(
        context: Context,
        currentStreak: Int,
        pendingTasksCount: Int
    ) {
        initNotificationChannels(context)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val contentIntent = getLaunchPendingIntent(context, 9001)

        val builder = NotificationCompat.Builder(context, CHANNEL_STREAK)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("⚠️ Day Ends Soon: Streak at Risk!")
            .setContentText("You have $pendingTasksCount required task(s) remaining to protect your $currentStreak-day streak.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Don't break the chain! Finish your remaining $pendingTasksCount required task(s) before midnight to secure Day ${currentStreak + 1} of discipline."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)

        manager.notify(9001, builder.build())
    }

    fun showSmartNotification(
        context: Context,
        type: String,
        title: String,
        message: String
    ) {
        initNotificationChannels(context)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notifId = when (type) {
            TYPE_MORNING -> 9002
            TYPE_AFTERNOON -> 9003
            TYPE_EVENING -> 9004
            else -> 9005
        }

        val contentIntent = getLaunchPendingIntent(context, notifId)

        val builder = NotificationCompat.Builder(context, CHANNEL_SMART)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)

        manager.notify(notifId, builder.build())
    }

    fun showAchievementNotification(
        context: Context,
        achievementTitle: String,
        rarity: String,
        xp: Int
    ) {
        initNotificationChannels(context)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val contentIntent = getLaunchPendingIntent(context, 9010)

        val builder = NotificationCompat.Builder(context, CHANNEL_ACHIEVEMENTS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("🏆 Achievement Unlocked: $achievementTitle")
            .setContentText("Rarity: $rarity • +$xp XP earned!")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Outstanding discipline! You unlocked '$achievementTitle' ($rarity tier) and claimed +$xp XP."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)

        manager.notify(9010, builder.build())
    }

    fun showLevelUpNotification(
        context: Context,
        newLevel: Int,
        newTitle: String
    ) {
        initNotificationChannels(context)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val contentIntent = getLaunchPendingIntent(context, 9020)

        val builder = NotificationCompat.Builder(context, CHANNEL_ACHIEVEMENTS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("⚡ Level Up! Reached Level $newLevel")
            .setContentText("New Rank: $newTitle")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Your relentless consistency has unlocked Level $newLevel: $newTitle. Continue forging your willpower!"
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)

        manager.notify(9020, builder.build())
    }

    fun scheduleTaskReminder(
        context: Context,
        reminderId: Long,
        taskId: Long,
        taskTitle: String,
        time: String // "HH:mm"
    ) {
        try {
            val parts = time.split(":")
            if (parts.size != 2) return
            val hour = parts[0].toIntOrNull() ?: return
            val minute = parts[1].toIntOrNull() ?: return

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (before(Calendar.getInstance())) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                action = ReminderReceiver.ACTION_TASK_REMINDER
                putExtra(EXTRA_REMINDER_ID, reminderId)
                putExtra(EXTRA_TASK_ID, taskId)
                putExtra(EXTRA_TASK_TITLE, taskTitle)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (_: Exception) {
            // AlarmManager permission or configuration error
        }
    }

    fun cancelTaskReminder(context: Context, reminderId: Long) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                action = ReminderReceiver.ACTION_TASK_REMINDER
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId.toInt(),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
            }
        } catch (_: Exception) {
            // Ignore
        }
    }

    fun scheduleSnooze(context: Context, reminderId: Long, taskId: Long, taskTitle: String, snoozeMinutes: Int = 15) {
        try {
            val triggerAtMillis = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                action = ReminderReceiver.ACTION_TASK_REMINDER
                putExtra(EXTRA_REMINDER_ID, reminderId)
                putExtra(EXTRA_TASK_ID, taskId)
                putExtra(EXTRA_TASK_TITLE, taskTitle)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (_: Exception) {
            // Ignore
        }
    }
}
