package com.example.util

import com.example.data.local.entities.TaskEntity
import com.example.data.local.entities.TaskLogEntity
import com.example.data.model.DisciplineLevel
import com.example.data.model.StreakMilestone
import java.util.Calendar
import java.util.concurrent.TimeUnit

data class DisciplineStreakResult(
    val currentStreak: Int,
    val bestStreak: Int,
    val totalDisciplinedDays: Int,
    val totalCompletedTasks: Int,
    val isTodayDisciplined: Boolean,
    val isStreakAtRisk: Boolean,
    val requiredTasksToday: Int,
    val completedRequiredToday: Int,
    val optionalTasksToday: Int,
    val completedOptionalToday: Int,
    val nextMilestone: StreakMilestone,
    val daysToNextMilestone: Int,
    val disciplineLevel: DisciplineLevel,
    val hoursRemainingToday: Long,
    val minutesRemainingToday: Long,
    val disciplinedDates: Set<String>
)

object DisciplineStreakEngine {

    val MILESTONES = listOf(
        StreakMilestone(3, "Bronze Spark", "🥉", "Completed 3 consecutive disciplined days.", false),
        StreakMilestone(7, "Steel Will", "⚔️", "One full unbroken week of self-mastery.", false),
        StreakMilestone(14, "Iron Resolve", "🛡️", "Two weeks of neural rewiring.", false),
        StreakMilestone(21, "Habit Rewired", "🧠", "21 days: psychological habit formation lock.", false),
        StreakMilestone(30, "Titanium Focus", "💎", "One entire month of zero compromises.", false),
        StreakMilestone(50, "Unshakable", "⚡", "50 days of unyielding personal accountability.", false),
        StreakMilestone(60, "Diamond Master", "👑", "Two months of elite consistency.", false),
        StreakMilestone(100, "Century Legend", "🔥", "100 unbroken days of relentless discipline.", false),
        StreakMilestone(150, "Titan Discipline", "🏛️", "150 days of sovereign self-control.", false),
        StreakMilestone(365, "Infinite Sovereign", "🌌", "One entire year of pure discipline mastery.", false)
    )

    val LEVELS = listOf(
        DisciplineLevel(1, "Novice Disciple", 0, 2, "🌱"),
        DisciplineLevel(2, "Bronze Seeker", 3, 6, "🥉"),
        DisciplineLevel(3, "Steel Warrior", 7, 13, "⚔️"),
        DisciplineLevel(4, "Iron Guardian", 14, 20, "🛡️"),
        DisciplineLevel(5, "Habit Breaker", 21, 29, "🧠"),
        DisciplineLevel(6, "Titanium Master", 30, 49, "💎"),
        DisciplineLevel(7, "Focus Champion", 50, 99, "⚡"),
        DisciplineLevel(8, "Century Legend", 100, 199, "🔥"),
        DisciplineLevel(9, "Infinite Sage", 200, 364, "🏛️"),
        DisciplineLevel(10, "Ascended Grandmaster", 365, 9999, "🌌")
    )

    fun calculateStreak(
        allTasks: List<TaskEntity>,
        allLogs: List<TaskLogEntity>,
        todayStr: String = DateUtils.getTodayDate()
    ): DisciplineStreakResult {
        val logsByDate = allLogs.groupBy { it.date }
        val totalCompletedTasks = allLogs.count { it.isCompleted }

        // Find all distinct dates recorded
        val datesWithLogs = allLogs.map { it.date }.toSet()
        val allDates = (datesWithLogs + todayStr).filter { it <= todayStr }.sorted()

        // Map each date to whether it was disciplined
        val disciplinedDates = mutableSetOf<String>()

        for (d in allDates) {
            val scheduledTasks = allTasks.filter { DateUtils.isTaskScheduledForDate(it, d) }
            val requiredTasks = scheduledTasks.filter { it.isRequired }

            if (requiredTasks.isNotEmpty()) {
                val dayLogs = logsByDate[d]?.associateBy { it.taskId } ?: emptyMap()
                val allReqDone = requiredTasks.all { task ->
                    dayLogs[task.id]?.isCompleted == true
                }
                if (allReqDone) {
                    disciplinedDates.add(d)
                }
            }
        }

        // Today's specific metrics
        val scheduledToday = allTasks.filter { DateUtils.isTaskScheduledForDate(it, todayStr) }
        val reqToday = scheduledToday.filter { it.isRequired }
        val optToday = scheduledToday.filter { !it.isRequired }

        val todayLogs = logsByDate[todayStr]?.associateBy { it.taskId } ?: emptyMap()
        val compReqToday = reqToday.count { todayLogs[it.id]?.isCompleted == true }
        val compOptToday = optToday.count { todayLogs[it.id]?.isCompleted == true }

        val isTodayDisciplined = reqToday.isNotEmpty() && compReqToday == reqToday.size
        val isStreakAtRisk = reqToday.isNotEmpty() && compReqToday < reqToday.size

        // Calculate current streak
        var currentStreak = 0
        val yesterdayStr = DateUtils.addDays(todayStr, -1)

        if (isTodayDisciplined) {
            currentStreak = 1
            var checkDate = yesterdayStr
            while (disciplinedDates.contains(checkDate)) {
                currentStreak++
                checkDate = DateUtils.addDays(checkDate, -1)
            }
        } else {
            // Today not yet complete. Check if yesterday was disciplined.
            // If yesterday was disciplined, streak from yesterday is preserved until 11:59 PM today!
            if (disciplinedDates.contains(yesterdayStr)) {
                currentStreak = 1
                var checkDate = DateUtils.addDays(yesterdayStr, -1)
                while (disciplinedDates.contains(checkDate)) {
                    currentStreak++
                    checkDate = DateUtils.addDays(checkDate, -1)
                }
            } else {
                currentStreak = 0
            }
        }

        // Calculate all-time Best Streak
        var bestStreak = 0
        if (disciplinedDates.isNotEmpty()) {
            val sortedDisciplined = disciplinedDates.sorted()
            var currentRun = 0
            var previousDate: String? = null

            for (date in sortedDisciplined) {
                if (previousDate == null) {
                    currentRun = 1
                } else {
                    val expectedNext = DateUtils.addDays(previousDate, 1)
                    if (date == expectedNext) {
                        currentRun++
                    } else {
                        currentRun = 1
                    }
                }
                if (currentRun > bestStreak) {
                    bestStreak = currentRun
                }
                previousDate = date
            }
        }
        bestStreak = maxOf(bestStreak, currentStreak)

        val totalDisciplinedDays = disciplinedDates.size

        // Milestone
        val nextMilestone = MILESTONES.firstOrNull { it.days > currentStreak } ?: MILESTONES.last()
        val daysToNext = maxOf(0, nextMilestone.days - currentStreak)

        // Level
        val disciplineLevel = LEVELS.lastOrNull { totalDisciplinedDays >= it.minDays } ?: LEVELS.first()

        // Time remaining until 11:59 PM today
        val now = Calendar.getInstance()
        val endOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }
        val diffMs = maxOf(0L, endOfDay.timeInMillis - now.timeInMillis)
        val hoursRemaining = TimeUnit.MILLISECONDS.toHours(diffMs)
        val minutesRemaining = TimeUnit.MILLISECONDS.toMinutes(diffMs) % 60

        return DisciplineStreakResult(
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            totalDisciplinedDays = totalDisciplinedDays,
            totalCompletedTasks = totalCompletedTasks,
            isTodayDisciplined = isTodayDisciplined,
            isStreakAtRisk = isStreakAtRisk,
            requiredTasksToday = reqToday.size,
            completedRequiredToday = compReqToday,
            optionalTasksToday = optToday.size,
            completedOptionalToday = compOptToday,
            nextMilestone = nextMilestone,
            daysToNextMilestone = daysToNext,
            disciplineLevel = disciplineLevel,
            hoursRemainingToday = hoursRemaining,
            minutesRemainingToday = minutesRemaining,
            disciplinedDates = disciplinedDates
        )
    }
}
