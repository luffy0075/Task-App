package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entities.AchievementEntity
import com.example.data.local.entities.CategoryEntity
import com.example.data.local.entities.ChallengeDayEntity
import com.example.data.local.entities.CustomChallengeEntity
import com.example.data.local.entities.DailyQuestEntity
import com.example.data.local.entities.DailyRecordEntity
import com.example.data.local.entities.FocusSessionEntity
import com.example.data.local.entities.JournalEntryEntity
import com.example.data.local.entities.PurchasedRewardEntity
import com.example.data.local.entities.RewardItemEntity
import com.example.data.local.entities.TaskEntity
import com.example.data.local.entities.TaskLogEntity
import com.example.data.local.entities.TaskReminderEntity
import com.example.data.local.entities.WeeklyMissionEntity
import com.example.data.local.entities.XpTransactionEntity
import com.example.data.model.DisciplineLevel
import com.example.data.model.StreakMilestone
import com.example.data.model.TaskWithStatus
import com.example.data.repository.RebootRepository
import com.example.util.DateUtils
import com.example.util.DisciplineStreakEngine
import com.example.util.DisciplineStreakResult
import com.example.util.GamificationEngine
import com.example.util.NotificationHelper
import com.example.util.UserLevelInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class AppNavTab {
    HOME,
    TASKS,
    ACHIEVEMENTS,
    CHALLENGE,
    ANALYTICS,
    PROFILE
}

data class DashboardStats(
    val dayOfChallenge: Int = 1,
    val overallChallengePercentage: Float = 0f,
    val todayCompletionPercentage: Float = 0f,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalCompletedTasksCount: Int = 0,
    val totalActiveHabitsCount: Int = 0,
    val scheduledTasksCount: Int = 0,
    val completedTodayTasksCount: Int = 0
)

class RebootViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    val repository = RebootRepository(database)

    // Current navigation tab (6 tabs)
    private val _currentTab = MutableStateFlow(AppNavTab.HOME)
    val currentTab: StateFlow<AppNavTab> = _currentTab.asStateFlow()

    // Selected date for viewing / editing tasks & daily record
    private val _selectedDate = MutableStateFlow(DateUtils.getTodayDate())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Search & Filter state for tasks screen
    val taskSearchQuery = MutableStateFlow("")
    val taskSelectedCategoryId = MutableStateFlow<String?>(null)
    val taskSelectedPriority = MutableStateFlow<String?>(null)
    val taskStatusFilter = MutableStateFlow("ALL") // "ALL", "PENDING", "COMPLETED", "OVERDUE"
    val taskRequiredFilter = MutableStateFlow("ALL") // "ALL", "REQUIRED", "OPTIONAL"

    // Dialog & UI sheet states
    val showAddTaskDialog = MutableStateFlow(false)
    val editingTask = MutableStateFlow<TaskEntity?>(null)
    val showDailyCheckInDialog = MutableStateFlow(false)
    val selectedChallengeDay = MutableStateFlow<ChallengeDayEntity?>(null)
    val showBackupRestoreDialog = MutableStateFlow(false)
    val showCategoryDialog = MutableStateFlow(false)
    val confirmDeleteTask = MutableStateFlow<TaskEntity?>(null)
    val showResetConfirm = MutableStateFlow(false)
    val userToastMessage = MutableStateFlow<String?>(null)

    // Streak celebration dialog
    val showCelebrationDialog = MutableStateFlow(false)
    val celebratedStreakCount = MutableStateFlow(1)

    // Gamification Dialog States
    val showAchievementUnlockDialog = MutableStateFlow(false)
    val recentlyUnlockedAchievement = MutableStateFlow<AchievementEntity?>(null)
    val showLevelUpDialog = MutableStateFlow(false)
    val currentLevelUpInfo = MutableStateFlow<UserLevelInfo?>(null)
    val showRewardShopDialog = MutableStateFlow(false)
    val showCreateChallengeDialog = MutableStateFlow(false)

    // Productivity & Wellbeing Dialog States (Part 3)
    val showFocusTimerDialog = MutableStateFlow(false)
    val showDigitalDetoxDialog = MutableStateFlow(false)
    val showJournalDialog = MutableStateFlow(false)
    val showTaskReminderDialog = MutableStateFlow(false)
    val reminderSelectedTaskId = MutableStateFlow<Long?>(null)

    // Raw flows from repository
    val categories = repository.allCategories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allActiveTasks = repository.allActiveTasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val challengeDays = repository.allChallengeDays.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allDailyRecords = repository.allDailyRecords.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allLogs = repository.allLogs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val settingsList = repository.allSettings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Productivity flows
    val focusSessions: StateFlow<List<FocusSessionEntity>> = repository.allFocusSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val totalFocusMinutes: StateFlow<Int> = repository.totalFocusMinutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val journalEntries: StateFlow<List<JournalEntryEntity>> = repository.allJournalEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allTaskReminders: StateFlow<List<TaskReminderEntity>> = repository.allTaskReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val todayDailyRecord: StateFlow<DailyRecordEntity?> = _selectedDate.flatMapLatest { date: String ->
        repository.getRecordForDate(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val todayJournalEntry: StateFlow<JournalEntryEntity?> = _selectedDate.flatMapLatest { date ->
        repository.getJournalEntryForDate(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Gamification flows
    val allAchievements = repository.allAchievements.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allXpTransactions = repository.allXpTransactions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val totalXp: StateFlow<Int> = repository.totalXp.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val userLevelInfo: StateFlow<UserLevelInfo> = totalXp.map { xp ->
        GamificationEngine.calculateLevelInfo(xp)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        GamificationEngine.calculateLevelInfo(0)
    )

    val currentWeekId = GamificationEngine.getWeekIdentifier()
    val dailyQuests: StateFlow<List<DailyQuestEntity>> = _selectedDate.flatMapLatest { date ->
        repository.getDailyQuests(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weeklyMissions: StateFlow<List<WeeklyMissionEntity>> = repository.getWeeklyMissions(currentWeekId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customChallenges: StateFlow<List<CustomChallengeEntity>> = repository.allCustomChallenges
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rewardItems: StateFlow<List<RewardItemEntity>> = repository.allRewardItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val purchasedRewards: StateFlow<List<PurchasedRewardEntity>> = repository.purchasedRewards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Settings & Personalization
    val userDisplayName = MutableStateFlow("Warrior")
    val themeMode = MutableStateFlow("PREMIUM_PURPLE")
    val accentColor = MutableStateFlow("VIOLET")
    val fontScale = MutableStateFlow("DEFAULT")
    val cardStyle = MutableStateFlow("ELEVATED")
    val interfaceDensity = MutableStateFlow("COMFORTABLE")
    val reducedMotion = MutableStateFlow(false)
    val language = MutableStateFlow("EN")
    val challengeStartDate = MutableStateFlow(DateUtils.getTodayDate())
    val remindersEnabled = MutableStateFlow(true)
    val morningReminder = MutableStateFlow("07:00")
    val eveningReminder = MutableStateFlow("21:30")
    val activeFrame = MutableStateFlow("frame_classic")
    val activeFlameStyle = MutableStateFlow("flame_classic")
    val activeProfileTitle = MutableStateFlow("Disciplined Initiate")

    // Smart Notifications & Preferences (Part 3)
    val quietHoursEnabled = MutableStateFlow(false)
    val quietHoursStart = MutableStateFlow("22:00")
    val quietHoursEnd = MutableStateFlow("07:00")
    val soundEnabled = MutableStateFlow(true)
    val vibrationEnabled = MutableStateFlow(true)
    val notifyMorning = MutableStateFlow(true)
    val notifyAfternoon = MutableStateFlow(true)
    val notifyEvening = MutableStateFlow(true)
    val notifyStreakRisk = MutableStateFlow(true)
    val screenTimeGoalMinutes = MutableStateFlow(120)
    val weekStartDay = MutableStateFlow("MONDAY")

    private var previousLevel: Int = -1

    init {
        val todayStr = DateUtils.getTodayDate()
        viewModelScope.launch {
            repository.ensureInitialized()
            repository.ensureDailyQuestsForDate(todayStr)
            repository.ensureWeeklyMissionsForWeek(currentWeekId)
            NotificationHelper.initNotificationChannels(application)
        }

        // Observe settings changes
        viewModelScope.launch {
            settingsList.collect { list ->
                list.forEach { setting ->
                    when (setting.key) {
                        "user_display_name" -> userDisplayName.value = setting.value
                        "theme_mode" -> themeMode.value = setting.value
                        "accent_color" -> accentColor.value = setting.value
                        "font_scale" -> fontScale.value = setting.value
                        "card_style" -> cardStyle.value = setting.value
                        "interface_density" -> interfaceDensity.value = setting.value
                        "reduced_motion" -> reducedMotion.value = setting.value == "true"
                        "language" -> language.value = setting.value
                        "challenge_start_date" -> challengeStartDate.value = setting.value
                        "reminders_enabled" -> remindersEnabled.value = setting.value == "true"
                        "morning_reminder_time" -> morningReminder.value = setting.value
                        "evening_reminder_time" -> eveningReminder.value = setting.value
                        "active_frame" -> activeFrame.value = setting.value
                        "active_flame_style" -> activeFlameStyle.value = setting.value
                        "active_profile_title" -> activeProfileTitle.value = setting.value
                        "quiet_hours_enabled" -> quietHoursEnabled.value = setting.value == "true"
                        "quiet_hours_start" -> quietHoursStart.value = setting.value
                        "quiet_hours_end" -> quietHoursEnd.value = setting.value
                        "sound_enabled" -> soundEnabled.value = setting.value != "false"
                        "vibration_enabled" -> vibrationEnabled.value = setting.value != "false"
                        "notify_morning" -> notifyMorning.value = setting.value != "false"
                        "notify_afternoon" -> notifyAfternoon.value = setting.value != "false"
                        "notify_evening" -> notifyEvening.value = setting.value != "false"
                        "notify_streak_risk" -> notifyStreakRisk.value = setting.value != "false"
                        "screen_time_goal_minutes" -> screenTimeGoalMinutes.value = setting.value.toIntOrNull() ?: 120
                        "week_start_day" -> weekStartDay.value = setting.value
                    }
                }
            }
        }

        // Track Level Ups
        viewModelScope.launch {
            userLevelInfo.collect { info ->
                if (previousLevel == -1) {
                    previousLevel = info.level
                } else if (info.level > previousLevel) {
                    previousLevel = info.level
                    currentLevelUpInfo.value = info
                    showLevelUpDialog.value = true
                }
            }
        }
    }

    // Strict Infinite Daily Discipline Streak calculation
    val streakResult: StateFlow<DisciplineStreakResult> = combine(
        allActiveTasks,
        allLogs
    ) { tasks, logs ->
        DisciplineStreakEngine.calculateStreak(tasks, logs, DateUtils.getTodayDate())
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DisciplineStreakEngine.calculateStreak(emptyList(), emptyList(), DateUtils.getTodayDate())
    )

    val dashboardStats: StateFlow<DashboardStats> = combine(
        streakResult,
        challengeDays,
        challengeStartDate
    ) { streak, days, startDate ->
        val currentDay = DateUtils.calculateDayOfChallenge(startDate, DateUtils.getTodayDate())
        val completedDays = days.count { it.isCompleted }
        val overallPct = if (days.isNotEmpty()) completedDays.toFloat() / days.size.toFloat() else 0f
        val scheduled = streak.requiredTasksToday + streak.optionalTasksToday
        val completed = streak.completedRequiredToday + streak.completedOptionalToday
        val todayPct = if (scheduled > 0) completed.toFloat() / scheduled.toFloat() else 0f

        DashboardStats(
            dayOfChallenge = currentDay,
            overallChallengePercentage = overallPct,
            todayCompletionPercentage = todayPct,
            currentStreak = streak.currentStreak,
            longestStreak = streak.bestStreak,
            totalCompletedTasksCount = streak.totalCompletedTasks,
            totalActiveHabitsCount = scheduled,
            scheduledTasksCount = scheduled,
            completedTodayTasksCount = completed
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    // Tasks scheduled for the currently selected date, with completion status
    val tasksForSelectedDate: StateFlow<List<TaskWithStatus>> = combine(
        allActiveTasks,
        allLogs,
        categories,
        _selectedDate
    ) { tasks, logs, cats, date ->
        val logMap = logs.filter { it.date == date }.associateBy { it.taskId }
        val catMap = cats.associateBy { it.id }
        val todayStr = DateUtils.getTodayDate()

        tasks.filter { task ->
            DateUtils.isTaskScheduledForDate(task, date)
        }.map { task ->
            val log = logMap[task.id]
            val isDone = log?.isCompleted == true
            val isOverdue = !isDone && (date < todayStr || (task.dueDate != null && task.dueDate < todayStr))

            TaskWithStatus(
                task = task,
                category = catMap[task.categoryId],
                isCompleted = isDone,
                completedAt = log?.completedAt,
                logNotes = log?.notes,
                isOverdue = isOverdue
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Daily record for the selected date
    val currentDailyRecord: StateFlow<DailyRecordEntity?> = combine(
        allDailyRecords,
        _selectedDate
    ) { records, date ->
        records.firstOrNull { it.date == date }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Navigation and date actions
    fun setTab(tab: AppNavTab) {
        _currentTab.value = tab
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
        viewModelScope.launch {
            repository.ensureDailyQuestsForDate(date)
        }
    }

    fun shiftSelectedDate(deltaDays: Int) {
        val next = DateUtils.addDays(_selectedDate.value, deltaDays)
        setSelectedDate(next)
    }

    fun resetToToday() {
        setSelectedDate(DateUtils.getTodayDate())
    }

    // Task completion toggle with Gamification, XP economy, and Achievements
    fun toggleTaskCompletion(taskWithStatus: TaskWithStatus) {
        val newStatus = !taskWithStatus.isCompleted
        val date = _selectedDate.value
        val todayStr = DateUtils.getTodayDate()
        val taskId = taskWithStatus.task.id

        viewModelScope.launch {
            repository.setTaskCompletion(
                date = date,
                taskId = taskId,
                isCompleted = newStatus
            )

            val taskXp = when (taskWithStatus.task.priority) {
                "High" -> 15
                "Medium" -> 12
                else -> 10
            }

            if (newStatus) {
                // Award XP with unique sourceId
                repository.awardXp(
                    sourceType = "TASK_COMPLETION",
                    sourceId = "task_${taskId}_$date",
                    amount = taskXp,
                    description = "Completed task: ${taskWithStatus.task.title}",
                    date = date
                )

                // Check achievements based on actual activity
                val allCompletedLogs = allLogs.value.filter { it.isCompleted || (it.date == date && it.taskId == taskId) }
                val totalCompleted = allCompletedLogs.size

                // First Step
                checkAndUnlockAchievement("ach_first_step")

                // Task milestones
                if (totalCompleted >= 10) checkAndUnlockAchievement("ach_tasks_10")
                if (totalCompleted >= 50) checkAndUnlockAchievement("ach_tasks_50")
                if (totalCompleted >= 100) checkAndUnlockAchievement("ach_tasks_100")
                if (totalCompleted >= 500) checkAndUnlockAchievement("ach_tasks_500")
                if (totalCompleted >= 1000) checkAndUnlockAchievement("ach_tasks_1000")

                // Time of Day checks
                val cal = Calendar.getInstance()
                val hour = cal.get(Calendar.HOUR_OF_DAY)
                val minute = cal.get(Calendar.MINUTE)
                if (date == todayStr) {
                    if (hour < 6 || (hour == 6 && minute <= 30)) {
                        checkAndUnlockAchievement("ach_dawn_warrior")
                    } else if (hour < 8) {
                        checkAndUnlockAchievement("ach_early_bird")
                    } else if (hour >= 22) {
                        checkAndUnlockAchievement("ach_night_owl")
                    }
                }

                // Category-based achievements
                val catId = taskWithStatus.task.categoryId
                val completedInCat = allCompletedLogs.count { log ->
                    allActiveTasks.value.any { it.id == log.taskId && it.categoryId == catId }
                }
                when (catId) {
                    "cat_study" -> if (completedInCat >= 10) checkAndUnlockAchievement("ach_study_master")
                    "cat_fitness" -> if (completedInCat >= 10) checkAndUnlockAchievement("ach_iron_body")
                    "cat_digital_detox" -> if (completedInCat >= 10) checkAndUnlockAchievement("ach_digital_freedom")
                    "cat_self_control" -> if (completedInCat >= 10) checkAndUnlockAchievement("ach_iron_resolve")
                }

                // Check Polymath
                val uniqueCatCount = allActiveTasks.value
                    .filter { t -> allCompletedLogs.any { it.taskId == t.id } }
                    .map { it.categoryId }
                    .distinct()
                    .size
                if (uniqueCatCount >= 4) {
                    checkAndUnlockAchievement("ach_polymath")
                }

                // Check Daily Quests updates
                val currentQuests = dailyQuests.value
                currentQuests.forEach { quest ->
                    when (quest.questKey) {
                        "QUEST_EARLY_MOMENTUM" -> {
                            val newProgress = quest.currentProgress + 1
                            repository.updateDailyQuestProgress(quest.id, newProgress)
                        }
                        "QUEST_DEEP_FOCUS" -> {
                            if (catId == "cat_study" || catId == "cat_fitness") {
                                repository.updateDailyQuestProgress(quest.id, quest.currentProgress + 1)
                            }
                        }
                    }
                }

                // Check Weekly Missions
                val currentMissions = weeklyMissions.value
                currentMissions.forEach { mission ->
                    when (mission.missionKey) {
                        "MISSION_TASK_CENTURION" -> {
                            repository.updateWeeklyMissionProgress(mission.id, mission.currentProgress + 1)
                        }
                        "MISSION_MIND_BODY" -> {
                            if (catId == "cat_study" || catId == "cat_fitness") {
                                repository.updateWeeklyMissionProgress(mission.id, mission.currentProgress + 1)
                            }
                        }
                    }
                }

                // Check Custom Challenges progress
                val activeChallenges = customChallenges.value.filter { it.status == "ACTIVE" }
                activeChallenges.forEach { challenge ->
                    val shouldIncrement = when (challenge.targetMetric) {
                        "TOTAL_TASKS" -> true
                        "CATEGORY_TASKS" -> challenge.targetCategoryId == catId
                        else -> false
                    }
                    if (shouldIncrement) {
                        repository.updateCustomChallengeProgress(challenge.id, challenge.currentProgress + 1)
                    }
                }

                // Check All Required Tasks completion for today
                val currentTasks = tasksForSelectedDate.value
                val reqTasks = currentTasks.filter { it.task.isRequired }
                val willBeAllReqDone = reqTasks.isNotEmpty() && reqTasks.all {
                    if (it.task.id == taskId) true else it.isCompleted
                }

                if (willBeAllReqDone) {
                    // Award 30 bonus XP for completing all required tasks
                    repository.awardXp(
                        sourceType = "ALL_REQUIRED_BONUS",
                        sourceId = "all_req_$date",
                        amount = 30,
                        description = "All required tasks completed for $date",
                        date = date
                    )
                    checkAndUnlockAchievement("ach_daily_discipline")

                    // Update quest "Streak Guardian"
                    currentQuests.find { it.questKey == "QUEST_REQUIRED_DISCIPLINE" }?.let { quest ->
                        repository.updateDailyQuestProgress(quest.id, 1)
                    }

                    if (date == todayStr) {
                        // Celebration dialog
                        val nextStreak = streakResult.value.currentStreak + (if (streakResult.value.isTodayDisciplined) 0 else 1)
                        celebratedStreakCount.value = maxOf(1, nextStreak)
                        showCelebrationDialog.value = true

                        // Check Streak Achievements
                        when (nextStreak) {
                            1 -> checkAndUnlockAchievement("ach_streak_1")
                            3 -> checkAndUnlockAchievement("ach_streak_3")
                            7 -> checkAndUnlockAchievement("ach_streak_7")
                            14 -> checkAndUnlockAchievement("ach_streak_14")
                            30 -> checkAndUnlockAchievement("ach_streak_30")
                            60 -> checkAndUnlockAchievement("ach_streak_60")
                            100 -> checkAndUnlockAchievement("ach_streak_100")
                            180 -> checkAndUnlockAchievement("ach_streak_180")
                            365 -> checkAndUnlockAchievement("ach_streak_365")
                        }

                        // Check Comeback Achievements if rebuilding after reset
                        if (streakResult.value.bestStreak >= 3 && streakResult.value.currentStreak == 0) {
                            checkAndUnlockAchievement("ach_phoenix")
                        }
                    }
                }

                // Check Perfect Day (100% of required and optional)
                val allTasksDone = currentTasks.all {
                    if (it.task.id == taskId) true else it.isCompleted
                }
                if (allTasksDone && currentTasks.isNotEmpty()) {
                    checkAndUnlockAchievement("ach_perfect_day")
                }

            } else {
                // Task unchecked: revoke awarded XP to prevent farming
                repository.revokeXp("task_${taskId}_$date")
                repository.revokeXp("all_req_$date")
            }
        }
    }

    private suspend fun checkAndUnlockAchievement(achievementId: String) {
        val date = DateUtils.getTodayDate()
        val unlocked = repository.unlockAchievement(achievementId, date)
        if (unlocked) {
            val ach = allAchievements.value.find { it.id == achievementId }
            if (ach != null) {
                recentlyUnlockedAchievement.value = ach
                showAchievementUnlockDialog.value = true
            }
        }
    }

    // Daily Quests and Missions Actions
    fun claimDailyQuest(quest: DailyQuestEntity) {
        viewModelScope.launch {
            val success = repository.claimDailyQuest(quest, DateUtils.getTodayDate())
            if (success) {
                userToastMessage.value = "Claimed +${quest.xpReward} XP!"
            }
        }
    }

    fun claimWeeklyMission(mission: WeeklyMissionEntity) {
        viewModelScope.launch {
            val success = repository.claimWeeklyMission(mission, DateUtils.getTodayDate())
            if (success) {
                userToastMessage.value = "Claimed +${mission.xpReward} XP!"
            }
        }
    }

    fun createCustomChallenge(challenge: CustomChallengeEntity) {
        viewModelScope.launch {
            repository.insertCustomChallenge(challenge)
            showCreateChallengeDialog.value = false
            userToastMessage.value = "Challenge '${challenge.title}' launched!"
        }
    }

    fun claimCustomChallenge(challenge: CustomChallengeEntity) {
        viewModelScope.launch {
            val success = repository.claimCustomChallenge(challenge, DateUtils.getTodayDate())
            if (success) {
                checkAndUnlockAchievement("ach_challenge_crusher")
                userToastMessage.value = "Claimed +${challenge.xpReward} XP!"
            }
        }
    }

    fun deleteCustomChallenge(id: Long) {
        viewModelScope.launch {
            repository.deleteCustomChallenge(id)
        }
    }

    // Reward Shop Actions
    fun purchaseRewardItem(item: RewardItemEntity) {
        viewModelScope.launch {
            val success = repository.purchaseRewardItem(item, DateUtils.getTodayDate())
            if (success) {
                equipRewardItem(item)
                userToastMessage.value = "Purchased and equipped ${item.title}!"
            } else {
                userToastMessage.value = "Not enough Sparks!"
            }
        }
    }

    fun equipRewardItem(item: RewardItemEntity) {
        viewModelScope.launch {
            when (item.type) {
                "PROFILE_FRAME" -> {
                    activeFrame.value = item.id
                    repository.setSetting("active_frame", item.id)
                }
                "FLAME_STYLE" -> {
                    activeFlameStyle.value = item.id
                    repository.setSetting("active_flame_style", item.id)
                }
                "PROFILE_TITLE" -> {
                    activeProfileTitle.value = item.title
                    repository.setSetting("active_profile_title", item.title)
                }
                "THEME" -> {
                    when (item.id) {
                        "theme_cyberpunk" -> {
                            themeMode.value = "AMOLED_BLACK"
                            accentColor.value = "CYAN"
                            repository.setSetting("theme_mode", "AMOLED_BLACK")
                            repository.setSetting("accent_color", "CYAN")
                        }
                        "theme_sakura" -> {
                            accentColor.value = "ROSE"
                            repository.setSetting("accent_color", "ROSE")
                        }
                        "theme_crimson" -> {
                            accentColor.value = "AMBER"
                            repository.setSetting("accent_color", "AMBER")
                        }
                    }
                }
            }
        }
    }

    // Save or update a task
    fun saveTask(
        id: Long = 0,
        title: String,
        description: String,
        categoryId: String,
        iconName: String,
        priority: String,
        targetDurationMinutes: Int?,
        startDate: String,
        dueDate: String?,
        repeatSchedule: String,
        repeatDaysMask: Int,
        reminderTime: String?,
        dailyTargetCount: Int,
        isRequired: Boolean = true,
        customColorHex: String? = null
    ) {
        viewModelScope.launch {
            if (id == 0L) {
                val newTask = TaskEntity(
                    title = title.trim(),
                    description = description.trim(),
                    categoryId = categoryId,
                    iconName = iconName,
                    priority = priority,
                    targetDurationMinutes = targetDurationMinutes,
                    startDate = startDate,
                    dueDate = dueDate,
                    repeatSchedule = repeatSchedule,
                    repeatDaysMask = repeatDaysMask,
                    reminderTime = reminderTime,
                    dailyTargetCount = dailyTargetCount,
                    isDefault = false,
                    isRequired = isRequired,
                    customColorHex = customColorHex
                )
                repository.insertTask(newTask)
                userToastMessage.value = "Task created successfully!"
            } else {
                val existing = allActiveTasks.value.firstOrNull { it.id == id }
                val updatedTask = TaskEntity(
                    id = id,
                    title = title.trim(),
                    description = description.trim(),
                    categoryId = categoryId,
                    iconName = iconName,
                    priority = priority,
                    targetDurationMinutes = targetDurationMinutes,
                    startDate = startDate,
                    dueDate = dueDate,
                    repeatSchedule = repeatSchedule,
                    repeatDaysMask = repeatDaysMask,
                    reminderTime = reminderTime,
                    dailyTargetCount = dailyTargetCount,
                    isPaused = existing?.isPaused ?: false,
                    isDeleted = false,
                    isDefault = existing?.isDefault ?: false,
                    isRequired = isRequired,
                    customColorHex = customColorHex,
                    createdAt = existing?.createdAt ?: System.currentTimeMillis()
                )
                repository.updateTask(updatedTask)
                userToastMessage.value = "Task updated!"
            }
            showAddTaskDialog.value = false
            editingTask.value = null
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task.id)
            confirmDeleteTask.value = null
            userToastMessage.value = "Task deleted."
        }
    }

    fun toggleTaskPaused(task: TaskEntity) {
        viewModelScope.launch {
            repository.toggleTaskPause(task.id, !task.isPaused)
            userToastMessage.value = if (!task.isPaused) "Task paused." else "Task activated!"
        }
    }

    // Daily Record Check-in saving
    fun saveDailyProgress(
        screenTimeMinutes: Int,
        studyMinutes: Int,
        exerciseMinutes: Int,
        sleepMinutes: Int,
        mood: String,
        productivityRating: Int,
        journalNotes: String
    ) {
        viewModelScope.launch {
            val record = DailyRecordEntity(
                date = _selectedDate.value,
                screenTimeMinutes = screenTimeMinutes,
                studyMinutes = studyMinutes,
                exerciseMinutes = exerciseMinutes,
                sleepMinutes = sleepMinutes,
                mood = mood,
                productivityRating = productivityRating,
                journalNotes = journalNotes.trim(),
                updatedAt = System.currentTimeMillis()
            )
            repository.saveDailyRecord(record)
            showDailyCheckInDialog.value = false
            userToastMessage.value = "Daily progress saved!"
        }
    }

    // 30-Day Challenge Goal updates
    fun toggleChallengeGoal(day: ChallengeDayEntity, goalIndex: Int) {
        val mask = day.completedGoalsMask xor (1 shl goalIndex)
        val goals = day.goalsJson.split("|").filter { it.isNotBlank() }
        val allChecked = (0 until goals.size).all { (mask and (1 shl it)) != 0 }

        val updatedDay = day.copy(
            completedGoalsMask = mask,
            isCompleted = allChecked,
            completedAt = if (allChecked) System.currentTimeMillis() else null
        )
        viewModelScope.launch {
            repository.updateChallengeDay(updatedDay)
            selectedChallengeDay.value = updatedDay
        }
    }

    fun saveChallengeReflection(day: ChallengeDayEntity, notes: String) {
        val updatedDay = day.copy(reflectionNotes = notes.trim())
        viewModelScope.launch {
            repository.updateChallengeDay(updatedDay)
            selectedChallengeDay.value = updatedDay
            userToastMessage.value = "Reflection saved!"
        }
    }

    fun startOrResetChallenge(startDate: String = DateUtils.getTodayDate()) {
        viewModelScope.launch {
            repository.resetChallengeProgress(startDate)
            challengeStartDate.value = startDate
            showResetConfirm.value = false
            userToastMessage.value = "30-Day Challenge reset successfully!"
        }
    }

    fun resetChallengeProgress(startDate: String = DateUtils.getTodayDate()) {
        startOrResetChallenge(startDate)
    }

    // Category Management
    fun createCategory(name: String, iconName: String, colorHex: String) {
        viewModelScope.launch {
            val id = "cat_" + System.currentTimeMillis()
            val cat = CategoryEntity(
                id = id,
                name = name.trim(),
                iconName = iconName,
                colorHex = colorHex,
                isDefault = false,
                orderIndex = categories.value.size
            )
            repository.insertCategory(cat)
            userToastMessage.value = "Category added!"
        }
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch {
            repository.deleteCategory(id)
            userToastMessage.value = "Category removed."
        }
    }

    // Settings actions
    fun setUserDisplayName(name: String) {
        userDisplayName.value = name
        viewModelScope.launch { repository.setSetting("user_display_name", name) }
    }

    fun setTheme(mode: String) {
        themeMode.value = mode
        viewModelScope.launch { repository.setSetting("theme_mode", mode) }
    }

    fun setAccent(color: String) {
        accentColor.value = color
        viewModelScope.launch { repository.setSetting("accent_color", color) }
    }

    fun setFontScale(scale: String) {
        fontScale.value = scale
        viewModelScope.launch { repository.setSetting("font_scale", scale) }
    }

    fun setCardStyle(style: String) {
        cardStyle.value = style
        viewModelScope.launch { repository.setSetting("card_style", style) }
    }

    fun setInterfaceDensity(density: String) {
        interfaceDensity.value = density
        viewModelScope.launch { repository.setSetting("interface_density", density) }
    }

    fun setReducedMotion(reduced: Boolean) {
        reducedMotion.value = reduced
        viewModelScope.launch { repository.setSetting("reduced_motion", reduced.toString()) }
    }

    fun setLanguage(lang: String) {
        language.value = lang
        viewModelScope.launch { repository.setSetting("language", lang) }
    }

    fun setRemindersEnabled(enabled: Boolean) {
        remindersEnabled.value = enabled
        viewModelScope.launch { repository.setSetting("reminders_enabled", enabled.toString()) }
    }

    fun setReminderTimes(morning: String, evening: String) {
        morningReminder.value = morning
        eveningReminder.value = evening
        viewModelScope.launch {
            repository.setSetting("morning_reminder_time", morning)
            repository.setSetting("evening_reminder_time", evening)
            userToastMessage.value = "Reminder schedules updated!"
        }
    }

    fun triggerTestReminder() {
        val ctx = getApplication<Application>()
        val streak = streakResult.value
        val remainingRequired = (streak.requiredTasksToday - streak.completedRequiredToday).coerceAtLeast(0)
        if (remainingRequired > 0) {
            NotificationHelper.showStreakAtRiskNotification(
                ctx,
                streak.currentStreak,
                remainingRequired
            )
        } else {
            NotificationHelper.showSmartNotification(
                ctx,
                NotificationHelper.TYPE_MORNING,
                "☀️ Morning Mindset: Reboot 30",
                "Discipline is choosing between what you want now and what you want most!"
            )
        }
        userToastMessage.value = "Test notification sent!"
    }

    // Productivity & Focus Sessions (Part 3)
    fun saveFocusSession(
        durationMinutes: Int,
        sessionType: String,
        taskId: Long?,
        taskTitle: String?,
        notes: String?
    ) {
        val earnedXp = if (durationMinutes >= 45) 30 else 15
        viewModelScope.launch {
            val session = FocusSessionEntity(
                date = DateUtils.getTodayDate(),
                durationMinutes = durationMinutes,
                sessionType = sessionType,
                associatedTaskId = taskId,
                associatedTaskTitle = taskTitle,
                notes = notes,
                xpEarned = earnedXp
            )
            repository.insertFocusSession(session)
            if (sessionType == "FOCUS") {
                val todayStr = DateUtils.getTodayDate()
                repository.awardXp(
                    sourceType = "FOCUS_SESSION",
                    sourceId = "focus_${System.currentTimeMillis()}",
                    amount = earnedXp,
                    description = "Completed $durationMinutes min Deep Focus session",
                    date = todayStr
                )
                // Also update daily record study/exercise minutes if linked
                val currentRec = repository.getDailyRecordDirect(todayStr) ?: DailyRecordEntity(date = todayStr)
                repository.saveDailyRecord(
                    currentRec.copy(
                        studyMinutes = currentRec.studyMinutes + durationMinutes,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
            checkAndUnlockAchievement("ach_first_step")
            userToastMessage.value = "Focus session logged! +$earnedXp XP earned 🎯"
        }
    }

    // Daily Journal (Part 3)
    fun saveJournalEntry(
        mood: String,
        energyRating: Int,
        gratitude: String,
        reflection: String,
        highlights: String,
        challenges: String,
        tag: String
    ) {
        viewModelScope.launch {
            val todayStr = _selectedDate.value
            val existing = repository.getJournalEntryForDateDirect(todayStr)
            val entry = JournalEntryEntity(
                id = existing?.id ?: 0L,
                date = todayStr,
                mood = mood,
                energyRating = energyRating,
                gratitudeNotes = gratitude,
                reflectionNotes = reflection,
                positiveHighlights = highlights,
                challengesFaced = challenges,
                tags = tag,
                createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.saveJournalEntry(entry)

            // Update daily record mood & notes
            val currentRec = repository.getDailyRecordDirect(todayStr) ?: DailyRecordEntity(date = todayStr)
            repository.saveDailyRecord(
                currentRec.copy(
                    mood = mood,
                    journalNotes = if (reflection.isNotBlank()) reflection else currentRec.journalNotes,
                    updatedAt = System.currentTimeMillis()
                )
            )

            repository.awardXp(
                sourceType = "DAILY_JOURNAL",
                sourceId = "journal_${todayStr}_${System.currentTimeMillis()}",
                amount = 20,
                description = "Logged daily discipline reflection & gratitude",
                date = todayStr
            )
            checkAndUnlockAchievement("ach_first_step")
            userToastMessage.value = "Discipline journal saved! +20 XP awarded ✍️"
        }
    }

    // Task Reminders (Part 3)
    fun saveTaskReminder(taskId: Long, time: String, repeatMask: Int, label: String?) {
        viewModelScope.launch {
            val task = allActiveTasks.value.firstOrNull { it.id == taskId }
            val reminder = TaskReminderEntity(
                taskId = taskId,
                time = time,
                repeatDaysMask = repeatMask,
                label = label,
                isEnabled = true
            )
            val id = repository.saveTaskReminder(reminder)
            val ctx = getApplication<Application>()
            NotificationHelper.scheduleTaskReminder(
                context = ctx,
                reminderId = id,
                taskId = taskId,
                taskTitle = task?.title ?: "Disciplined Task",
                time = time
            )
            userToastMessage.value = "Reminder scheduled for $time ⏰"
        }
    }

    fun toggleTaskReminder(reminder: TaskReminderEntity, enabled: Boolean) {
        viewModelScope.launch {
            repository.updateTaskReminder(reminder.copy(isEnabled = enabled))
            val ctx = getApplication<Application>()
            if (enabled) {
                val task = allActiveTasks.value.firstOrNull { it.id == reminder.taskId }
                NotificationHelper.scheduleTaskReminder(
                    context = ctx,
                    reminderId = reminder.id,
                    taskId = reminder.taskId,
                    taskTitle = task?.title ?: "Disciplined Task",
                    time = reminder.time
                )
                userToastMessage.value = "Reminder enabled for ${reminder.time}"
            } else {
                NotificationHelper.cancelTaskReminder(ctx, reminder.id)
                userToastMessage.value = "Reminder disabled"
            }
        }
    }

    fun deleteTaskReminder(reminderId: Long) {
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            NotificationHelper.cancelTaskReminder(ctx, reminderId)
            repository.deleteTaskReminder(reminderId)
            userToastMessage.value = "Reminder deleted"
        }
    }

    fun snoozeTaskReminder(reminderId: Long, minutes: Int = 15) {
        viewModelScope.launch {
            val snoozeUntil = System.currentTimeMillis() + (minutes * 60 * 1000L)
            repository.snoozeTaskReminder(reminderId, snoozeUntil)
            val reminder = allTaskReminders.value.firstOrNull { it.id == reminderId }
            val task = allActiveTasks.value.firstOrNull { it.id == reminder?.taskId }
            val ctx = getApplication<Application>()
            NotificationHelper.scheduleSnooze(
                ctx,
                reminderId,
                reminder?.taskId ?: 0L,
                task?.title ?: "Disciplined Task",
                minutes
            )
            userToastMessage.value = "Snoozed for $minutes minutes ⏱️"
        }
    }

    // Digital Detox & Screen Time (Part 3)
    fun saveScreenTimeAndGoal(loggedMinutes: Int, goalMinutes: Int) {
        viewModelScope.launch {
            val todayStr = DateUtils.getTodayDate()
            val currentRec = repository.getDailyRecordDirect(todayStr) ?: DailyRecordEntity(date = todayStr)
            repository.saveDailyRecord(
                currentRec.copy(
                    screenTimeMinutes = loggedMinutes,
                    updatedAt = System.currentTimeMillis()
                )
            )
            repository.setSetting("screen_time_goal_minutes", goalMinutes.toString())
            screenTimeGoalMinutes.value = goalMinutes
            userToastMessage.value = "Screen time & detox goals updated!"
        }
    }

    // Smart Notification & Quiet Hours Settings (Part 3)
    fun updateQuietHours(enabled: Boolean, start: String, end: String) {
        quietHoursEnabled.value = enabled
        quietHoursStart.value = start
        quietHoursEnd.value = end
        viewModelScope.launch {
            repository.setSetting("quiet_hours_enabled", enabled.toString())
            repository.setSetting("quiet_hours_start", start)
            repository.setSetting("quiet_hours_end", end)
            userToastMessage.value = "Quiet hours preferences updated!"
        }
    }

    fun updateNotificationToggle(category: String, enabled: Boolean) {
        viewModelScope.launch {
            when (category) {
                "sound" -> {
                    soundEnabled.value = enabled
                    repository.setSetting("sound_enabled", enabled.toString())
                }
                "vibration" -> {
                    vibrationEnabled.value = enabled
                    repository.setSetting("vibration_enabled", enabled.toString())
                }
                "morning" -> {
                    notifyMorning.value = enabled
                    repository.setSetting("notify_morning", enabled.toString())
                }
                "afternoon" -> {
                    notifyAfternoon.value = enabled
                    repository.setSetting("notify_afternoon", enabled.toString())
                }
                "evening" -> {
                    notifyEvening.value = enabled
                    repository.setSetting("notify_evening", enabled.toString())
                }
                "streak" -> {
                    notifyStreakRisk.value = enabled
                    repository.setSetting("notify_streak_risk", enabled.toString())
                }
            }
        }
    }

    fun updateWeekStartDay(day: String) {
        weekStartDay.value = day
        viewModelScope.launch {
            repository.setSetting("week_start_day", day)
        }
    }

    // Backup & Restore
    suspend fun exportJson(): String {
        return repository.exportBackupJson()
    }

    fun importJson(json: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.importBackupJson(json)
            onResult(success)
            if (success) {
                userToastMessage.value = "Backup restored successfully!"
            } else {
                userToastMessage.value = "Error parsing backup JSON. Please check format."
            }
        }
    }
}
