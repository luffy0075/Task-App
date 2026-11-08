package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entities.AchievementEntity
import com.example.data.local.entities.AppSettingEntity
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
import com.example.util.GamificationEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class RebootRepository(private val database: AppDatabase) {
    val allCategories: Flow<List<CategoryEntity>> = database.categoryDao().getAllCategories()
    val allActiveTasks: Flow<List<TaskEntity>> = database.taskDao().getAllActiveTasks()
    val allChallengeDays: Flow<List<ChallengeDayEntity>> = database.challengeDayDao().getAllChallengeDays()
    val allDailyRecords: Flow<List<DailyRecordEntity>> = database.dailyRecordDao().getAllDailyRecords()
    val allLogs: Flow<List<TaskLogEntity>> = database.taskLogDao().getAllLogs()
    val allSettings: Flow<List<AppSettingEntity>> = database.appSettingDao().getAllSettings()

    // Gamification Flows
    val allAchievements: Flow<List<AchievementEntity>> = database.achievementDao().getAllAchievements()
    val allXpTransactions: Flow<List<XpTransactionEntity>> = database.xpTransactionDao().getAllTransactions()
    val totalXp: Flow<Int> = database.xpTransactionDao().getTotalXp()
    val allCustomChallenges: Flow<List<CustomChallengeEntity>> = database.customChallengeDao().getAllChallenges()
    val allRewardItems: Flow<List<RewardItemEntity>> = database.rewardDao().getAllRewardItems()
    val purchasedRewards: Flow<List<PurchasedRewardEntity>> = database.rewardDao().getPurchasedRewards()

    // Focus & Productivity Flows
    val allFocusSessions: Flow<List<FocusSessionEntity>> = database.focusSessionDao().getAllSessions()
    val totalFocusMinutes: Flow<Int> = database.focusSessionDao().getTotalFocusMinutes()

    // Journal & Reflection Flows
    val allJournalEntries: Flow<List<JournalEntryEntity>> = database.journalEntryDao().getAllEntries()

    // Task Reminders Flow
    val allTaskReminders: Flow<List<TaskReminderEntity>> = database.taskReminderDao().getAllReminders()

    fun getDailyQuests(date: String): Flow<List<DailyQuestEntity>> = database.dailyQuestDao().getQuestsForDate(date)
    fun getWeeklyMissions(week: String): Flow<List<WeeklyMissionEntity>> = database.weeklyMissionDao().getMissionsForWeek(week)
    fun getFocusSessionsForDate(date: String): Flow<List<FocusSessionEntity>> = database.focusSessionDao().getSessionsForDate(date)
    fun getJournalEntryForDate(date: String): Flow<JournalEntryEntity?> = database.journalEntryDao().getEntryForDate(date)
    fun getRemindersForTask(taskId: Long): Flow<List<TaskReminderEntity>> = database.taskReminderDao().getRemindersForTask(taskId)

    fun getLogsForDate(date: String): Flow<List<TaskLogEntity>> = database.taskLogDao().getLogsForDate(date)

    fun getRecordForDate(date: String): Flow<DailyRecordEntity?> = database.dailyRecordDao().getRecordForDate(date)
    fun getDailyRecord(date: String): Flow<DailyRecordEntity?> = database.dailyRecordDao().getRecordForDate(date)

    suspend fun getDailyRecordDirect(date: String): DailyRecordEntity? = withContext(Dispatchers.IO) {
        database.dailyRecordDao().getRecordForDateDirect(date)
    }

    fun getSetting(key: String): Flow<String?> = database.appSettingDao().getSetting(key)

    suspend fun getSettingDirect(key: String): String? = database.appSettingDao().getSettingDirect(key)

    suspend fun setSetting(key: String, value: String) = withContext(Dispatchers.IO) {
        database.appSettingDao().setSetting(AppSettingEntity(key, value))
    }

    suspend fun insertTask(task: TaskEntity): Long = withContext(Dispatchers.IO) {
        database.taskDao().insertTask(task)
    }

    suspend fun updateTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        database.taskDao().updateTask(task)
    }

    suspend fun deleteTask(id: Long) = withContext(Dispatchers.IO) {
        database.taskDao().softDeleteTask(id)
        database.taskLogDao().deleteLogsForTask(id)
    }

    suspend fun toggleTaskPause(id: Long, isPaused: Boolean) = withContext(Dispatchers.IO) {
        database.taskDao().setTaskPaused(id, isPaused)
    }

    suspend fun setTaskCompletion(date: String, taskId: Long, isCompleted: Boolean, notes: String? = null) = withContext(Dispatchers.IO) {
        val log = TaskLogEntity(
            date = date,
            taskId = taskId,
            isCompleted = isCompleted,
            completedAt = if (isCompleted) System.currentTimeMillis() else null,
            notes = notes
        )
        database.taskLogDao().insertOrUpdateLog(log)
    }

    suspend fun insertCategory(category: CategoryEntity) = withContext(Dispatchers.IO) {
        database.categoryDao().insertCategory(category)
    }

    suspend fun updateCategory(category: CategoryEntity) = withContext(Dispatchers.IO) {
        database.categoryDao().updateCategory(category)
    }

    suspend fun deleteCategory(id: String) = withContext(Dispatchers.IO) {
        database.categoryDao().deleteCategory(id)
    }

    suspend fun saveDailyRecord(record: DailyRecordEntity) = withContext(Dispatchers.IO) {
        database.dailyRecordDao().insertOrUpdateRecord(record)
    }

    suspend fun updateChallengeDay(day: ChallengeDayEntity) = withContext(Dispatchers.IO) {
        database.challengeDayDao().update(day)
    }

    suspend fun resetChallengeProgress(newStartDate: String) = withContext(Dispatchers.IO) {
        database.challengeDayDao().resetAllChallengeProgress()
        database.appSettingDao().setSetting(AppSettingEntity("challenge_start_date", newStartDate))
    }

    suspend fun ensureInitialized() = withContext(Dispatchers.IO) {
        val categories = database.categoryDao().getAllCategoriesList()
        if (categories.isEmpty()) {
            AppDatabase.populateInitialData(database)
        } else {
            // Check if achievements need initialization (e.g., after migration 2->3)
            val achievements = database.achievementDao().getAllAchievementsList()
            if (achievements.isEmpty()) {
                database.achievementDao().insertAll(GamificationEngine.getDefaultAchievements())
            }
            // Check reward items
            val rewards = database.rewardDao().getAllRewardItemsList()
            if (rewards.isEmpty()) {
                database.rewardDao().insertRewardItems(GamificationEngine.getDefaultRewardItems())
                database.rewardDao().insertPurchase(PurchasedRewardEntity("frame_classic"))
                database.rewardDao().insertPurchase(PurchasedRewardEntity("flame_classic"))
            }
        }
    }

    suspend fun ensureDailyQuestsForDate(date: String) = withContext(Dispatchers.IO) {
        val existing = database.dailyQuestDao().getQuestsForDateDirect(date)
        if (existing.isEmpty()) {
            val quests = GamificationEngine.generateDailyQuestsForDate(date)
            database.dailyQuestDao().insertQuests(quests)
        }
    }

    suspend fun ensureWeeklyMissionsForWeek(week: String) = withContext(Dispatchers.IO) {
        val existing = database.weeklyMissionDao().getMissionsForWeekDirect(week)
        if (existing.isEmpty()) {
            val missions = GamificationEngine.generateWeeklyMissionsForWeek(week)
            database.weeklyMissionDao().insertMissions(missions)
        }
    }

    // Award XP with strict anti-duplication
    suspend fun awardXp(
        sourceType: String,
        sourceId: String,
        amount: Int,
        description: String,
        date: String
    ): Boolean = withContext(Dispatchers.IO) {
        if (amount <= 0) return@withContext false
        val alreadyAwarded = database.xpTransactionDao().hasTransaction(sourceId)
        if (alreadyAwarded) return@withContext false

        val tx = XpTransactionEntity(
            sourceType = sourceType,
            sourceId = sourceId,
            amount = amount,
            description = description,
            date = date
        )
        val rowId = database.xpTransactionDao().insertTransaction(tx)
        rowId > 0
    }

    // Revoke XP if a task was unchecked on that day
    suspend fun revokeXp(sourceId: String) = withContext(Dispatchers.IO) {
        database.xpTransactionDao().deleteTransactionBySourceId(sourceId)
    }

    // Unlock achievement and grant XP
    suspend fun unlockAchievement(id: String, date: String): Boolean = withContext(Dispatchers.IO) {
        val achievement = database.achievementDao().getAchievementById(id) ?: return@withContext false
        if (achievement.isUnlocked) return@withContext false

        val rows = database.achievementDao().unlock(id, System.currentTimeMillis())
        if (rows > 0) {
            awardXp(
                sourceType = "ACHIEVEMENT",
                sourceId = "ach_${id}",
                amount = achievement.xpReward,
                description = "Unlocked achievement: ${achievement.title}",
                date = date
            )
            true
        } else {
            false
        }
    }

    suspend fun updateAchievementProgress(id: String, progress: Int) = withContext(Dispatchers.IO) {
        database.achievementDao().updateProgress(id, progress)
    }

    suspend fun updateDailyQuestProgress(id: Long, progress: Int) = withContext(Dispatchers.IO) {
        database.dailyQuestDao().updateProgress(id, progress)
    }

    suspend fun claimDailyQuest(quest: DailyQuestEntity, date: String): Boolean = withContext(Dispatchers.IO) {
        if (quest.isClaimed || !quest.isCompleted) return@withContext false
        val updated = database.dailyQuestDao().claimQuest(quest.id)
        if (updated > 0) {
            awardXp(
                sourceType = "DAILY_QUEST",
                sourceId = "quest_${quest.id}",
                amount = quest.xpReward,
                description = "Daily Quest: ${quest.title}",
                date = date
            )
            true
        } else {
            false
        }
    }

    suspend fun updateWeeklyMissionProgress(id: Long, progress: Int) = withContext(Dispatchers.IO) {
        database.weeklyMissionDao().updateProgress(id, progress)
    }

    suspend fun claimWeeklyMission(mission: WeeklyMissionEntity, date: String): Boolean = withContext(Dispatchers.IO) {
        if (mission.isClaimed || !mission.isCompleted) return@withContext false
        val updated = database.weeklyMissionDao().claimMission(mission.id)
        if (updated > 0) {
            awardXp(
                sourceType = "WEEKLY_MISSION",
                sourceId = "mission_${mission.id}",
                amount = mission.xpReward,
                description = "Weekly Mission: ${mission.title}",
                date = date
            )
            true
        } else {
            false
        }
    }

    suspend fun insertCustomChallenge(challenge: CustomChallengeEntity): Long = withContext(Dispatchers.IO) {
        database.customChallengeDao().insertChallenge(challenge)
    }

    suspend fun updateCustomChallengeProgress(id: Long, progress: Int) = withContext(Dispatchers.IO) {
        database.customChallengeDao().updateProgress(id, progress)
    }

    suspend fun claimCustomChallenge(challenge: CustomChallengeEntity, date: String): Boolean = withContext(Dispatchers.IO) {
        if (challenge.isClaimed || challenge.status != "COMPLETED") return@withContext false
        val updated = database.customChallengeDao().claimReward(challenge.id)
        if (updated > 0) {
            awardXp(
                sourceType = "CHALLENGE",
                sourceId = "challenge_${challenge.id}",
                amount = challenge.xpReward,
                description = "Custom Challenge: ${challenge.title}",
                date = date
            )
            true
        } else {
            false
        }
    }

    suspend fun deleteCustomChallenge(id: Long) = withContext(Dispatchers.IO) {
        database.customChallengeDao().deleteChallenge(id)
    }

    // Purchase cosmetic item
    suspend fun purchaseRewardItem(item: RewardItemEntity, date: String): Boolean = withContext(Dispatchers.IO) {
        val purchased = database.rewardDao().getPurchasedItemIds()
        if (purchased.contains(item.id)) return@withContext true // Already owned

        val totalXp = database.xpTransactionDao().getTotalXpDirect()
        if (totalXp < item.costSparks) return@withContext false

        // Insert purchase
        database.rewardDao().insertPurchase(PurchasedRewardEntity(item.id))

        // If cost > 0, record spending transaction
        if (item.costSparks > 0) {
            val tx = XpTransactionEntity(
                sourceType = "SHOP_PURCHASE",
                sourceId = "purchase_${item.id}",
                amount = -item.costSparks,
                description = "Purchased: ${item.title}",
                date = date
            )
            database.xpTransactionDao().insertTransaction(tx)
        }
        true
    }

    // Focus Session Operations
    suspend fun insertFocusSession(session: FocusSessionEntity): Long = withContext(Dispatchers.IO) {
        val id = database.focusSessionDao().insertSession(session)
        if (session.sessionType == "FOCUS" && session.xpEarned > 0) {
            awardXp(
                sourceType = "FOCUS_SESSION",
                sourceId = "focus_${id}_${session.date}",
                amount = session.xpEarned,
                description = "Focus Session: ${session.durationMinutes} min",
                date = session.date
            )
        }
        id
    }

    // Journal Entry Operations
    suspend fun saveJournalEntry(entry: JournalEntryEntity): Long = withContext(Dispatchers.IO) {
        database.journalEntryDao().insertOrUpdate(entry)
    }

    suspend fun getJournalEntryForDateDirect(date: String): JournalEntryEntity? = withContext(Dispatchers.IO) {
        database.journalEntryDao().getEntryForDateDirect(date)
    }

    suspend fun deleteJournalEntry(id: Long) = withContext(Dispatchers.IO) {
        database.journalEntryDao().deleteEntry(id)
    }

    // Task Reminder Operations
    suspend fun insertTaskReminder(reminder: TaskReminderEntity): Long = withContext(Dispatchers.IO) {
        database.taskReminderDao().insertReminder(reminder)
    }

    suspend fun saveTaskReminder(reminder: TaskReminderEntity): Long = insertTaskReminder(reminder)

    suspend fun updateTaskReminder(reminder: TaskReminderEntity) = withContext(Dispatchers.IO) {
        database.taskReminderDao().updateReminder(reminder)
    }

    suspend fun deleteTaskReminder(id: Long) = withContext(Dispatchers.IO) {
        database.taskReminderDao().deleteReminder(id)
    }

    suspend fun snoozeTaskReminder(id: Long, snoozeUntilMillis: Long) = withContext(Dispatchers.IO) {
        database.taskReminderDao().snoozeReminder(id, snoozeUntilMillis)
    }

    suspend fun exportBackupJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", 1)
        root.put("appName", "Reboot 30")
        root.put("exportTime", System.currentTimeMillis())

        // Categories
        val catsArray = JSONArray()
        database.categoryDao().getAllCategoriesList().forEach { cat ->
            val obj = JSONObject()
            obj.put("id", cat.id)
            obj.put("name", cat.name)
            obj.put("iconName", cat.iconName)
            obj.put("colorHex", cat.colorHex)
            obj.put("isDefault", cat.isDefault)
            obj.put("orderIndex", cat.orderIndex)
            catsArray.put(obj)
        }
        root.put("categories", catsArray)

        // Tasks
        val tasksArray = JSONArray()
        database.taskDao().getAllTasksRaw().forEach { task ->
            val obj = JSONObject()
            obj.put("id", task.id)
            obj.put("title", task.title)
            obj.put("description", task.description)
            obj.put("categoryId", task.categoryId)
            obj.put("iconName", task.iconName)
            obj.put("priority", task.priority)
            task.targetDurationMinutes?.let { obj.put("targetDurationMinutes", it) }
            obj.put("startDate", task.startDate)
            task.dueDate?.let { obj.put("dueDate", it) }
            obj.put("repeatSchedule", task.repeatSchedule)
            obj.put("repeatDaysMask", task.repeatDaysMask)
            task.reminderTime?.let { obj.put("reminderTime", it) }
            obj.put("dailyTargetCount", task.dailyTargetCount)
            obj.put("isPaused", task.isPaused)
            obj.put("isDeleted", task.isDeleted)
            obj.put("isDefault", task.isDefault)
            obj.put("isRequired", task.isRequired)
            task.customColorHex?.let { obj.put("customColorHex", it) }
            obj.put("createdAt", task.createdAt)
            tasksArray.put(obj)
        }
        root.put("tasks", tasksArray)

        // Task Logs
        val logsArray = JSONArray()
        database.taskLogDao().getAllLogsList().forEach { log ->
            val obj = JSONObject()
            obj.put("date", log.date)
            obj.put("taskId", log.taskId)
            obj.put("isCompleted", log.isCompleted)
            log.completedAt?.let { obj.put("completedAt", it) }
            log.notes?.let { obj.put("notes", it) }
            logsArray.put(obj)
        }
        root.put("taskLogs", logsArray)

        // Daily Records
        val recordsArray = JSONArray()
        database.dailyRecordDao().getAllDailyRecordsList().forEach { rec ->
            val obj = JSONObject()
            obj.put("date", rec.date)
            obj.put("screenTimeMinutes", rec.screenTimeMinutes)
            obj.put("studyMinutes", rec.studyMinutes)
            obj.put("exerciseMinutes", rec.exerciseMinutes)
            obj.put("sleepMinutes", rec.sleepMinutes)
            obj.put("mood", rec.mood)
            obj.put("productivityRating", rec.productivityRating)
            obj.put("journalNotes", rec.journalNotes)
            obj.put("updatedAt", rec.updatedAt)
            recordsArray.put(obj)
        }
        root.put("dailyRecords", recordsArray)

        // Challenge Days
        val daysArray = JSONArray()
        database.challengeDayDao().getAllChallengeDaysList().forEach { day ->
            val obj = JSONObject()
            obj.put("dayNumber", day.dayNumber)
            obj.put("phaseNumber", day.phaseNumber)
            obj.put("phaseName", day.phaseName)
            obj.put("title", day.title)
            obj.put("theme", day.theme)
            obj.put("goalsJson", day.goalsJson)
            obj.put("isCompleted", day.isCompleted)
            obj.put("completedGoalsMask", day.completedGoalsMask)
            obj.put("reflectionNotes", day.reflectionNotes)
            day.completedAt?.let { obj.put("completedAt", it) }
            daysArray.put(obj)
        }
        root.put("challengeDays", daysArray)

        // Settings
        val settingsArray = JSONArray()
        database.appSettingDao().getAllSettingsList().forEach { set ->
            val obj = JSONObject()
            obj.put("key", set.key)
            obj.put("value", set.value)
            settingsArray.put(obj)
        }
        root.put("settings", settingsArray)

        // Focus Sessions
        val focusArray = JSONArray()
        database.focusSessionDao().getAllSessionsList().forEach { fs ->
            val obj = JSONObject()
            obj.put("id", fs.id)
            obj.put("date", fs.date)
            obj.put("durationMinutes", fs.durationMinutes)
            obj.put("sessionType", fs.sessionType)
            fs.associatedTaskId?.let { obj.put("associatedTaskId", it) }
            fs.associatedTaskTitle?.let { obj.put("associatedTaskTitle", it) }
            fs.notes?.let { obj.put("notes", it) }
            obj.put("completedAt", fs.completedAt)
            obj.put("xpEarned", fs.xpEarned)
            focusArray.put(obj)
        }
        root.put("focusSessions", focusArray)

        // Journal Entries
        val journalArray = JSONArray()
        database.journalEntryDao().getAllEntriesList().forEach { je ->
            val obj = JSONObject()
            obj.put("id", je.id)
            obj.put("date", je.date)
            obj.put("mood", je.mood)
            obj.put("energyRating", je.energyRating)
            obj.put("gratitudeNotes", je.gratitudeNotes)
            obj.put("reflectionNotes", je.reflectionNotes)
            obj.put("positiveHighlights", je.positiveHighlights)
            obj.put("challengesFaced", je.challengesFaced)
            obj.put("tags", je.tags)
            obj.put("createdAt", je.createdAt)
            obj.put("updatedAt", je.updatedAt)
            journalArray.put(obj)
        }
        root.put("journalEntries", journalArray)

        // Task Reminders
        val remindersArray = JSONArray()
        database.taskReminderDao().getAllRemindersList().forEach { tr ->
            val obj = JSONObject()
            obj.put("id", tr.id)
            obj.put("taskId", tr.taskId)
            obj.put("time", tr.time)
            obj.put("repeatDaysMask", tr.repeatDaysMask)
            tr.label?.let { obj.put("label", it) }
            obj.put("isEnabled", tr.isEnabled)
            tr.snoozeUntilMillis?.let { obj.put("snoozeUntilMillis", it) }
            obj.put("createdAt", tr.createdAt)
            remindersArray.put(obj)
        }
        root.put("taskReminders", remindersArray)

        root.toString(2)
    }

    suspend fun importBackupJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            if (!root.has("appName") || root.getString("appName") != "Reboot 30") {
                return@withContext false
            }

            // Restore categories
            if (root.has("categories")) {
                val catsArray = root.getJSONArray("categories")
                val cats = mutableListOf<CategoryEntity>()
                for (i in 0 until catsArray.length()) {
                    val obj = catsArray.getJSONObject(i)
                    cats.add(
                        CategoryEntity(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            iconName = obj.optString("iconName", "category"),
                            colorHex = obj.optString("colorHex", "#818CF8"),
                            isDefault = obj.optBoolean("isDefault", false),
                            orderIndex = obj.optInt("orderIndex", i)
                        )
                    )
                }
                if (cats.isNotEmpty()) {
                    database.categoryDao().insertCategories(cats)
                }
            }

            // Restore tasks
            if (root.has("tasks")) {
                val tasksArray = root.getJSONArray("tasks")
                val tasks = mutableListOf<TaskEntity>()
                for (i in 0 until tasksArray.length()) {
                    val obj = tasksArray.getJSONObject(i)
                    tasks.add(
                        TaskEntity(
                            id = obj.getLong("id"),
                            title = obj.getString("title"),
                            description = obj.optString("description", ""),
                            categoryId = obj.getString("categoryId"),
                            iconName = obj.optString("iconName", "check_circle"),
                            priority = obj.optString("priority", "Medium"),
                            targetDurationMinutes = if (obj.has("targetDurationMinutes")) obj.getInt("targetDurationMinutes") else null,
                            startDate = obj.optString("startDate", "2026-01-01"),
                            dueDate = if (obj.has("dueDate")) obj.getString("dueDate") else null,
                            repeatSchedule = obj.optString("repeatSchedule", "EVERY_DAY"),
                            repeatDaysMask = obj.optInt("repeatDaysMask", 127),
                            reminderTime = if (obj.has("reminderTime")) obj.getString("reminderTime") else null,
                            dailyTargetCount = obj.optInt("dailyTargetCount", 1),
                            isPaused = obj.optBoolean("isPaused", false),
                            isDeleted = obj.optBoolean("isDeleted", false),
                            isDefault = obj.optBoolean("isDefault", false),
                            isRequired = obj.optBoolean("isRequired", true),
                            customColorHex = if (obj.has("customColorHex")) obj.getString("customColorHex") else null,
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
                if (tasks.isNotEmpty()) {
                    database.taskDao().insertTasks(tasks)
                }
            }

            // Restore task logs
            if (root.has("taskLogs")) {
                val logsArray = root.getJSONArray("taskLogs")
                val logs = mutableListOf<TaskLogEntity>()
                for (i in 0 until logsArray.length()) {
                    val obj = logsArray.getJSONObject(i)
                    logs.add(
                        TaskLogEntity(
                            date = obj.getString("date"),
                            taskId = obj.getLong("taskId"),
                            isCompleted = obj.getBoolean("isCompleted"),
                            completedAt = if (obj.has("completedAt")) obj.getLong("completedAt") else null,
                            notes = if (obj.has("notes")) obj.getString("notes") else null
                        )
                    )
                }
                if (logs.isNotEmpty()) {
                    database.taskLogDao().insertLogs(logs)
                }
            }

            // Restore daily records
            if (root.has("dailyRecords")) {
                val recsArray = root.getJSONArray("dailyRecords")
                val recs = mutableListOf<DailyRecordEntity>()
                for (i in 0 until recsArray.length()) {
                    val obj = recsArray.getJSONObject(i)
                    recs.add(
                        DailyRecordEntity(
                            date = obj.getString("date"),
                            screenTimeMinutes = obj.optInt("screenTimeMinutes", 0),
                            studyMinutes = obj.optInt("studyMinutes", 0),
                            exerciseMinutes = obj.optInt("exerciseMinutes", 0),
                            sleepMinutes = obj.optInt("sleepMinutes", 0),
                            mood = obj.optString("mood", "Good"),
                            productivityRating = obj.optInt("productivityRating", 3),
                            journalNotes = obj.optString("journalNotes", ""),
                            updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                        )
                    )
                }
                if (recs.isNotEmpty()) {
                    database.dailyRecordDao().insertRecords(recs)
                }
            }

            // Restore challenge days
            if (root.has("challengeDays")) {
                val daysArray = root.getJSONArray("challengeDays")
                val days = mutableListOf<ChallengeDayEntity>()
                for (i in 0 until daysArray.length()) {
                    val obj = daysArray.getJSONObject(i)
                    days.add(
                        ChallengeDayEntity(
                            dayNumber = obj.getInt("dayNumber"),
                            phaseNumber = obj.getInt("phaseNumber"),
                            phaseName = obj.getString("phaseName"),
                            title = obj.getString("title"),
                            theme = obj.getString("theme"),
                            goalsJson = obj.getString("goalsJson"),
                            isCompleted = obj.optBoolean("isCompleted", false),
                            completedGoalsMask = obj.optInt("completedGoalsMask", 0),
                            reflectionNotes = obj.optString("reflectionNotes", ""),
                            completedAt = if (obj.has("completedAt")) obj.getLong("completedAt") else null
                        )
                    )
                }
                if (days.isNotEmpty()) {
                    database.challengeDayDao().insertAll(days)
                }
            }

            // Restore settings
            if (root.has("settings")) {
                val setsArray = root.getJSONArray("settings")
                val sets = mutableListOf<AppSettingEntity>()
                for (i in 0 until setsArray.length()) {
                    val obj = setsArray.getJSONObject(i)
                    sets.add(AppSettingEntity(obj.getString("key"), obj.getString("value")))
                }
                if (sets.isNotEmpty()) {
                    database.appSettingDao().insertAll(sets)
                }
            }

            // Restore focus sessions
            if (root.has("focusSessions")) {
                val focusArray = root.getJSONArray("focusSessions")
                val sessions = mutableListOf<FocusSessionEntity>()
                for (i in 0 until focusArray.length()) {
                    val obj = focusArray.getJSONObject(i)
                    sessions.add(
                        FocusSessionEntity(
                            id = obj.optLong("id", 0L),
                            date = obj.getString("date"),
                            durationMinutes = obj.getInt("durationMinutes"),
                            sessionType = obj.optString("sessionType", "FOCUS"),
                            associatedTaskId = if (obj.has("associatedTaskId")) obj.getLong("associatedTaskId") else null,
                            associatedTaskTitle = if (obj.has("associatedTaskTitle")) obj.getString("associatedTaskTitle") else null,
                            notes = if (obj.has("notes")) obj.getString("notes") else null,
                            completedAt = obj.optLong("completedAt", System.currentTimeMillis()),
                            xpEarned = obj.optInt("xpEarned", 15)
                        )
                    )
                }
                if (sessions.isNotEmpty()) {
                    database.focusSessionDao().insertSessions(sessions)
                }
            }

            // Restore journal entries
            if (root.has("journalEntries")) {
                val journalArray = root.getJSONArray("journalEntries")
                val entries = mutableListOf<JournalEntryEntity>()
                for (i in 0 until journalArray.length()) {
                    val obj = journalArray.getJSONObject(i)
                    entries.add(
                        JournalEntryEntity(
                            id = obj.optLong("id", 0L),
                            date = obj.getString("date"),
                            mood = obj.optString("mood", "Good"),
                            energyRating = obj.optInt("energyRating", 3),
                            gratitudeNotes = obj.optString("gratitudeNotes", ""),
                            reflectionNotes = obj.optString("reflectionNotes", ""),
                            positiveHighlights = obj.optString("positiveHighlights", ""),
                            challengesFaced = obj.optString("challengesFaced", ""),
                            tags = obj.optString("tags", "Discipline"),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                            updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                        )
                    )
                }
                if (entries.isNotEmpty()) {
                    database.journalEntryDao().insertEntries(entries)
                }
            }

            // Restore task reminders
            if (root.has("taskReminders")) {
                val remindersArray = root.getJSONArray("taskReminders")
                val reminders = mutableListOf<TaskReminderEntity>()
                for (i in 0 until remindersArray.length()) {
                    val obj = remindersArray.getJSONObject(i)
                    reminders.add(
                        TaskReminderEntity(
                            id = obj.optLong("id", 0L),
                            taskId = obj.getLong("taskId"),
                            time = obj.getString("time"),
                            repeatDaysMask = obj.optInt("repeatDaysMask", 127),
                            label = if (obj.has("label")) obj.getString("label") else null,
                            isEnabled = obj.optBoolean("isEnabled", true),
                            snoozeUntilMillis = if (obj.has("snoozeUntilMillis")) obj.getLong("snoozeUntilMillis") else null,
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
                if (reminders.isNotEmpty()) {
                    database.taskReminderDao().insertReminders(reminders)
                }
            }

            true
        } catch (_: Exception) {
            false
        }
    }
}
