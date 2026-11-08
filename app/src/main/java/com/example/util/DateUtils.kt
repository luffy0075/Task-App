package com.example.util

import com.example.data.local.entities.TaskEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateUtils {
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayFormat = SimpleDateFormat("EEEE, MMM d", Locale.US)
    private val shortDisplayFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.US)

    fun getTodayDate(): String = isoFormat.format(Date())

    fun formatDisplayDate(dateStr: String): String {
        return try {
            val date = isoFormat.parse(dateStr) ?: return dateStr
            displayFormat.format(date)
        } catch (_: Exception) {
            dateStr
        }
    }

    fun formatShortDate(dateStr: String): String {
        return try {
            val date = isoFormat.parse(dateStr) ?: return dateStr
            shortDisplayFormat.format(date)
        } catch (_: Exception) {
            dateStr
        }
    }

    fun formatMonthYear(calendar: Calendar): String {
        return monthYearFormat.format(calendar.time)
    }

    fun formatIso(calendar: Calendar): String {
        return isoFormat.format(calendar.time)
    }

    fun formatDayAbbr(calendar: Calendar): String {
        return SimpleDateFormat("EEE", Locale.US).format(calendar.time)
    }

    fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 4..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "Night focus"
        }
    }

    fun addDays(dateStr: String, days: Int): String {
        return try {
            val cal = Calendar.getInstance()
            val date = isoFormat.parse(dateStr) ?: Date()
            cal.time = date
            cal.add(Calendar.DAY_OF_YEAR, days)
            isoFormat.format(cal.time)
        } catch (_: Exception) {
            dateStr
        }
    }

    fun calculateDayOfChallenge(startDateStr: String, currentDateStr: String = getTodayDate()): Int {
        return try {
            val start = isoFormat.parse(startDateStr) ?: return 1
            val current = isoFormat.parse(currentDateStr) ?: return 1
            val diffMs = current.time - start.time
            val diffDays = TimeUnit.MILLISECONDS.toDays(diffMs).toInt()
            val day = diffDays + 1
            day.coerceIn(1, 30)
        } catch (_: Exception) {
            1
        }
    }

    /**
     * Checks whether a task should appear on a given target date
     */
    fun isTaskScheduledForDate(task: TaskEntity, dateStr: String): Boolean {
        if (task.isDeleted || task.isPaused) return false
        
        // Before start date?
        if (dateStr < task.startDate) return false
        
        // After due date?
        task.dueDate?.let { due ->
            if (dateStr > due) return false
        }

        return when (task.repeatSchedule) {
            "ONE_TIME" -> dateStr == task.startDate
            "EVERY_DAY" -> true
            "WEEKDAYS", "CUSTOM" -> {
                try {
                    val cal = Calendar.getInstance()
                    val date = isoFormat.parse(dateStr) ?: return true
                    cal.time = date
                    // Calendar.MONDAY is 2, SUNDAY is 1
                    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                    val maskBit = when (dayOfWeek) {
                        Calendar.MONDAY -> 1
                        Calendar.TUESDAY -> 2
                        Calendar.WEDNESDAY -> 4
                        Calendar.THURSDAY -> 8
                        Calendar.FRIDAY -> 16
                        Calendar.SATURDAY -> 32
                        Calendar.SUNDAY -> 64
                        else -> 1
                    }
                    (task.repeatDaysMask and maskBit) != 0
                } catch (_: Exception) {
                    true
                }
            }
            "WEEKLY" -> {
                try {
                    val calStart = Calendar.getInstance()
                    val startDate = isoFormat.parse(task.startDate) ?: return true
                    calStart.time = startDate
                    val startDayOfWeek = calStart.get(Calendar.DAY_OF_WEEK)

                    val calTarget = Calendar.getInstance()
                    val targetDate = isoFormat.parse(dateStr) ?: return true
                    calTarget.time = targetDate
                    val targetDayOfWeek = calTarget.get(Calendar.DAY_OF_WEEK)

                    startDayOfWeek == targetDayOfWeek
                } catch (_: Exception) {
                    true
                }
            }
            else -> true
        }
    }
}
