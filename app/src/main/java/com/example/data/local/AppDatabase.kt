package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AchievementDao
import com.example.data.local.dao.AppSettingDao
import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.ChallengeDayDao
import com.example.data.local.dao.CustomChallengeDao
import com.example.data.local.dao.DailyQuestDao
import com.example.data.local.dao.DailyRecordDao
import com.example.data.local.dao.FocusSessionDao
import com.example.data.local.dao.JournalEntryDao
import com.example.data.local.dao.RewardDao
import com.example.data.local.dao.TaskDao
import com.example.data.local.dao.TaskLogDao
import com.example.data.local.dao.TaskReminderDao
import com.example.data.local.dao.WeeklyMissionDao
import com.example.data.local.dao.XpTransactionDao
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CategoryEntity::class,
        TaskEntity::class,
        TaskLogEntity::class,
        DailyRecordEntity::class,
        ChallengeDayEntity::class,
        AppSettingEntity::class,
        AchievementEntity::class,
        XpTransactionEntity::class,
        DailyQuestEntity::class,
        WeeklyMissionEntity::class,
        CustomChallengeEntity::class,
        RewardItemEntity::class,
        PurchasedRewardEntity::class,
        FocusSessionEntity::class,
        JournalEntryEntity::class,
        TaskReminderEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun taskDao(): TaskDao
    abstract fun taskLogDao(): TaskLogDao
    abstract fun dailyRecordDao(): DailyRecordDao
    abstract fun challengeDayDao(): ChallengeDayDao
    abstract fun appSettingDao(): AppSettingDao
    abstract fun achievementDao(): AchievementDao
    abstract fun xpTransactionDao(): XpTransactionDao
    abstract fun dailyQuestDao(): DailyQuestDao
    abstract fun weeklyMissionDao(): WeeklyMissionDao
    abstract fun customChallengeDao(): CustomChallengeDao
    abstract fun rewardDao(): RewardDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun journalEntryDao(): JournalEntryDao
    abstract fun taskReminderDao(): TaskReminderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN isRequired INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE tasks ADD COLUMN customColorHex TEXT DEFAULT NULL")
            }
        }

        private val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS achievements (
                        id TEXT NOT NULL PRIMARY KEY,
                        category TEXT NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        rarity TEXT NOT NULL,
                        iconEmoji TEXT NOT NULL,
                        targetValue INTEGER NOT NULL,
                        currentProgress INTEGER NOT NULL DEFAULT 0,
                        xpReward INTEGER NOT NULL,
                        isUnlocked INTEGER NOT NULL DEFAULT 0,
                        unlockedAt INTEGER,
                        isHidden INTEGER NOT NULL DEFAULT 0,
                        orderIndex INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS xp_transactions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        sourceType TEXT NOT NULL,
                        sourceId TEXT NOT NULL,
                        amount INTEGER NOT NULL,
                        description TEXT NOT NULL,
                        date TEXT NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_xp_transactions_sourceId ON xp_transactions(sourceId)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS daily_quests (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        date TEXT NOT NULL,
                        questKey TEXT NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        iconEmoji TEXT NOT NULL,
                        targetCount INTEGER NOT NULL,
                        currentProgress INTEGER NOT NULL DEFAULT 0,
                        xpReward INTEGER NOT NULL,
                        isCompleted INTEGER NOT NULL DEFAULT 0,
                        isClaimed INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_daily_quests_date_questKey ON daily_quests(date, questKey)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS weekly_missions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        weekIdentifier TEXT NOT NULL,
                        missionKey TEXT NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        iconEmoji TEXT NOT NULL,
                        targetCount INTEGER NOT NULL,
                        currentProgress INTEGER NOT NULL DEFAULT 0,
                        xpReward INTEGER NOT NULL,
                        isCompleted INTEGER NOT NULL DEFAULT 0,
                        isClaimed INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_weekly_missions_weekIdentifier_missionKey ON weekly_missions(weekIdentifier, missionKey)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS custom_challenges (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        startDate TEXT NOT NULL,
                        endDate TEXT NOT NULL,
                        durationDays INTEGER NOT NULL,
                        targetMetric TEXT NOT NULL,
                        targetCategoryId TEXT,
                        targetCount INTEGER NOT NULL,
                        currentProgress INTEGER NOT NULL DEFAULT 0,
                        xpReward INTEGER NOT NULL,
                        status TEXT NOT NULL DEFAULT 'ACTIVE',
                        colorHex TEXT NOT NULL DEFAULT '#8B5CF6',
                        isClaimed INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS reward_items (
                        id TEXT NOT NULL PRIMARY KEY,
                        type TEXT NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        iconEmoji TEXT NOT NULL,
                        costSparks INTEGER NOT NULL,
                        previewColorHex TEXT NOT NULL DEFAULT '#8B5CF6',
                        orderIndex INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS purchased_rewards (
                        itemId TEXT NOT NULL PRIMARY KEY,
                        purchasedAt INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS focus_sessions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        date TEXT NOT NULL,
                        durationMinutes INTEGER NOT NULL,
                        sessionType TEXT NOT NULL,
                        associatedTaskId INTEGER,
                        associatedTaskTitle TEXT,
                        notes TEXT,
                        completedAt INTEGER NOT NULL,
                        xpEarned INTEGER NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS journal_entries (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        date TEXT NOT NULL,
                        mood TEXT NOT NULL,
                        energyRating INTEGER NOT NULL,
                        gratitudeNotes TEXT NOT NULL,
                        reflectionNotes TEXT NOT NULL,
                        positiveHighlights TEXT NOT NULL,
                        challengesFaced TEXT NOT NULL,
                        tags TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS task_reminders (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        taskId INTEGER NOT NULL,
                        time TEXT NOT NULL,
                        repeatDaysMask INTEGER NOT NULL,
                        label TEXT,
                        isEnabled INTEGER NOT NULL,
                        snoozeUntilMillis INTEGER,
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "reboot30_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            // Default categories
            val defaultCategories = listOf(
                CategoryEntity("cat_digital_detox", "Digital Detox", "phone_disabled", "#38BDF8", true, 0),
                CategoryEntity("cat_study", "Study & Learning", "school", "#818CF8", true, 1),
                CategoryEntity("cat_fitness", "Fitness & Health", "fitness_center", "#34D399", true, 2),
                CategoryEntity("cat_personal_dev", "Personal Development", "psychology", "#F472B6", true, 3),
                CategoryEntity("cat_sleep", "Sleep & Routine", "bedtime", "#A78BFA", true, 4),
                CategoryEntity("cat_self_control", "Self-Control", "shield", "#F59E0B", true, 5),
                CategoryEntity("cat_japanese", "Japanese Learning", "translate", "#EC4899", true, 6),
                CategoryEntity("cat_app_dev", "App Development", "code", "#06B6D4", true, 7),
                CategoryEntity("cat_custom", "Custom Category", "category", "#94A3B8", true, 8)
            )
            database.categoryDao().insertCategories(defaultCategories)

            // Today's date as string
            val todayDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())

            // Default pre-built tasks
            val defaultTasks = listOf(
                TaskEntity(
                    id = 1,
                    title = "No phone for the first hour after waking up",
                    description = "Protect your morning dopamine baseline. Avoid scrolling or notifications.",
                    categoryId = "cat_digital_detox",
                    iconName = "phone_disabled",
                    priority = "High",
                    targetDurationMinutes = 60,
                    startDate = todayDate,
                    repeatSchedule = "EVERY_DAY",
                    repeatDaysMask = 127,
                    isDefault = true
                ),
                TaskEntity(
                    id = 2,
                    title = "Exercise or walking for 20–30 minutes",
                    description = "Cardio, weight training, or brisk walking to stimulate neuroplasticity.",
                    categoryId = "cat_fitness",
                    iconName = "fitness_center",
                    priority = "High",
                    targetDurationMinutes = 30,
                    startDate = todayDate,
                    repeatSchedule = "EVERY_DAY",
                    repeatDaysMask = 127,
                    isDefault = true
                ),
                TaskEntity(
                    id = 3,
                    title = "Study for at least 2 hours",
                    description = "Deep undistracted study or high-value skill building in Pomodoro blocks.",
                    categoryId = "cat_study",
                    iconName = "school",
                    priority = "High",
                    targetDurationMinutes = 120,
                    startDate = todayDate,
                    repeatSchedule = "EVERY_DAY",
                    repeatDaysMask = 127,
                    isDefault = true
                ),
                TaskEntity(
                    id = 4,
                    title = "Avoid unnecessary social media scrolling",
                    description = "Say no to infinite feeds, short-form reels, and doomscrolling.",
                    categoryId = "cat_digital_detox",
                    iconName = "block",
                    priority = "High",
                    targetDurationMinutes = null,
                    startDate = todayDate,
                    repeatSchedule = "EVERY_DAY",
                    repeatDaysMask = 127,
                    isDefault = true
                ),
                TaskEntity(
                    id = 5,
                    title = "Avoid pornography",
                    description = "Maintain mental clarity, self-respect, and clean neurological reward pathways.",
                    categoryId = "cat_self_control",
                    iconName = "shield",
                    priority = "High",
                    targetDurationMinutes = null,
                    startDate = todayDate,
                    repeatSchedule = "EVERY_DAY",
                    repeatDaysMask = 127,
                    isDefault = true
                ),
                TaskEntity(
                    id = 6,
                    title = "Sleep on time (target 7–9 hours)",
                    description = "Turn off screens 45 minutes before bed and wake up fully refreshed.",
                    categoryId = "cat_sleep",
                    iconName = "bedtime",
                    priority = "High",
                    targetDurationMinutes = 480,
                    startDate = todayDate,
                    repeatSchedule = "EVERY_DAY",
                    repeatDaysMask = 127,
                    isDefault = true
                )
            )
            database.taskDao().insertTasks(defaultTasks)

            // 30-Day Challenge pre-population
            val challengeDays = generateChallengeDays()
            database.challengeDayDao().insertAll(challengeDays)

            // Initial settings
            val initialSettings = listOf(
                AppSettingEntity("theme_mode", "PREMIUM_PURPLE"),
                AppSettingEntity("accent_color", "VIOLET"),
                AppSettingEntity("language", "EN"),
                AppSettingEntity("challenge_start_date", todayDate),
                AppSettingEntity("challenge_duration_days", "30"),
                AppSettingEntity("reminders_enabled", "true"),
                AppSettingEntity("morning_reminder_time", "07:00"),
                AppSettingEntity("evening_reminder_time", "21:30"),
                AppSettingEntity("active_frame", "frame_classic"),
                AppSettingEntity("active_flame_style", "flame_classic"),
                AppSettingEntity("active_profile_title", "Disciplined Initiate")
            )
            database.appSettingDao().insertAll(initialSettings)

            // Populate Achievements
            val defaultAchievements = GamificationEngine.getDefaultAchievements()
            database.achievementDao().insertAll(defaultAchievements)

            // Populate Reward Shop items & default free purchases
            val defaultRewards = GamificationEngine.getDefaultRewardItems()
            database.rewardDao().insertRewardItems(defaultRewards)
            database.rewardDao().insertPurchase(PurchasedRewardEntity("frame_classic"))
            database.rewardDao().insertPurchase(PurchasedRewardEntity("flame_classic"))

            // Populate Daily Quests for today
            val todayQuests = GamificationEngine.generateDailyQuestsForDate(todayDate)
            database.dailyQuestDao().insertQuests(todayQuests)

            // Populate Weekly Missions for current week
            val weekId = GamificationEngine.getWeekIdentifier()
            val weeklyMissions = GamificationEngine.generateWeeklyMissionsForWeek(weekId)
            database.weeklyMissionDao().insertMissions(weeklyMissions)
        }

        private fun generateChallengeDays(): List<ChallengeDayEntity> {
            val list = mutableListOf<ChallengeDayEntity>()
            // 4 Phases:
            // Week 1 (Days 1–7): Control Yourself
            val p1Goals = listOf(
                "Identify 3 major digital habit triggers|Put phone in airplane mode 1 hour after waking|Log 20m exercise or brisk walk|Complete 2h focused study",
                "Delete or log out of 1 time-wasting app|Drink 2.5L water today|Finish daily 30m physical workout|2h distraction-free study",
                "No phone in bed before sleeping|Replace 1 urge with 10 pushups or deep breathing|Read 15 pages of a productive book|Track all screen time",
                "Observe cravings without acting on them|30m outdoor walk in natural daylight|Study 2h with phone in another room|Zero social media during working hours",
                "Reflect on first 4 days in journal|Zero pornographic or hyper-stimulating content|Complete full cardio / workout session|2h deep work study block",
                "Clean your workspace and study desk|Plan tomorrow's task schedule tonight|Engage in 25m continuous stretching or running|2h uninterrupted learning",
                "Week 1 Milestone Review!|Celebrate 7 days of resisting initial urges|Review baseline screen-time reduction|Commit with conviction to Week 2"
            )

            for (i in 1..7) {
                list.add(
                    ChallengeDayEntity(
                        dayNumber = i,
                        phaseNumber = 1,
                        phaseName = "Phase 1: Control Yourself",
                        title = "Day $i: " + when(i) {
                            1 -> "The Awakening"
                            2 -> "Friction Against Distractions"
                            3 -> "Reclaiming The Morning"
                            4 -> "Mastering The Urge"
                            5 -> "The Dopamine Reset"
                            6 -> "Environment Design"
                            else -> "Week 1 Breakthrough"
                        },
                        theme = when(i) {
                            1 -> "Understand unhealthy habits and cut morning phone usage."
                            2 -> "Build initial barriers against impulsive browsing."
                            3 -> "Protect cognitive energy by owning the morning."
                            4 -> "Watch urges come and pass without reacting."
                            5 -> "Feel dopamine stabilization through raw discipline."
                            6 -> "Design an environment where failure is difficult."
                            else -> "Celebrate 1 week of conscious self-mastery."
                        },
                        goalsJson = p1Goals[i - 1]
                    )
                )
            }

            // Week 2 (Days 8–14): Build Discipline
            val p2Goals = listOf(
                "Increase study consistency to 2.5h today|Push exercise to 35m active movement|Strict zero phone while having meals|Bedtime set at 10:30 PM",
                "Study session with zero browser tabs unrelated to task|Say NO to 3 instant gratifications|Cardio or strength session 30m|Reflect on improved mental clarity",
                "Turn off all non-essential phone push notifications|Log total study time accurately|30m brisk run or bodyweight workout|Evening meditation or calm breathing",
                "Tackle your hardest academic or career topic first|Zero short-form video feeds today|Daily 30m workout completed|Sleep 8 hours continuous",
                "Practice delay of gratification by 15 min when tempted|35m high-energy workout|Study 2.5 hours with full attention|Review your 30-day vision",
                "Weekend discipline check: stay on routine!|Read 20 pages of educational material|30m exercise outdoor|Prepare for upcoming Week 3 Deep Focus",
                "Week 2 Milestone: Halfway to habit rewiring!|Record your biggest victory so far|Clean bedroom and digital files|Affirm your identity as a disciplined person"
            )

            for (i in 8..14) {
                list.add(
                    ChallengeDayEntity(
                        dayNumber = i,
                        phaseNumber = 2,
                        phaseName = "Phase 2: Build Discipline",
                        title = "Day $i: " + when(i) {
                            8 -> "Raising The Standard"
                            9 -> "The Power of Single-Tasking"
                            10 -> "Notification Silence"
                            11 -> "Eat The Frog First"
                            12 -> "The Urge Delay Technique"
                            13 -> "Unshakable Weekend"
                            else -> "Discipline Solidified"
                        },
                        theme = when(i) {
                            8 -> "Elevate your daily baseline across study and health."
                            9 -> "Eliminate divided attention and cognitive fragmentation."
                            10 -> "Starve interruptions to feed concentration."
                            11 -> "Confront the hardest task first thing in the day."
                            12 -> "Train mental endurance when friction feels high."
                            13 -> "Do not let weekends derail your momentum."
                            else -> "Witness tangible changes in your stamina and mood."
                        },
                        goalsJson = p2Goals[i - 8]
                    )
                )
            }

            // Week 3 (Days 15–21): Deep Focus
            val p3Goals = listOf(
                "3 hours total deep study in structured 50m blocks|Zero social media browsing all day|35m workout with intensity|Evening review of what you mastered",
                "Learn 1 completely new concept or skill today|Eliminate ambient background noise while studying|High-effort exercise 30m|Sleep within 15 min of lights out",
                "Enter uninterrupted flow state for 90m block|No phone within arm reach all afternoon|Engage body with 30m fitness|Reflect on sharper mental sharpness",
                "Write down solutions to a difficult project or study goal|Zero porn or digital indulgence|Complete all daily habits before 8 PM|Gratitude reflection for self-discipline",
                "Push through afternoon slump without reaching for phone|30m cardio or brisk walk|2.5h deep study sessions|Prepare for final stretch",
                "Weekend deep study session 2h|Cook or eat clean wholesome food|35m exercise session|Visualize your transformed life at Day 30",
                "Week 3 Conquered: Elite Focus Achieved!|Check off all daily tasks 100%|Total screen time under 2.5 hours|Pride in your new identity"
            )

            for (i in 15..21) {
                list.add(
                    ChallengeDayEntity(
                        dayNumber = i,
                        phaseNumber = 3,
                        phaseName = "Phase 3: Deep Focus",
                        title = "Day $i: " + when(i) {
                            15 -> "The Flow State Portal"
                            16 -> "Deliberate Practice"
                            17 -> "The Unreachable Mind"
                            18 -> "Resilience Under Pressure"
                            19 -> "Afternoon Mastery"
                            20 -> "Elite Consistency"
                            else -> "Focus Master"
                        },
                        theme = when(i) {
                            15 -> "Dive into true uninterrupted deep work blocks."
                            16 -> "Actively stretch your intellectual capabilities."
                            17 -> "Guard your mind against any trivial inputs."
                            18 -> "Turn resistance into fuel for concentration."
                            19 -> "Overcome energy dips with breath and posture."
                            20 -> "Maintain elite standards while others drift."
                            else -> "Recognize that focus is now your natural default state."
                        },
                        goalsJson = p3Goals[i - 15]
                    )
                )
            }

            // Week 4 & Beyond (Days 22–30): New Lifestyle
            val p4Goals = listOf(
                "Execute entire morning routine flawlessly on auto-pilot|2.5h quality study|30m exercise|Reflect on how much easier discipline has become",
                "Help or inspire someone else with your discipline mindset|Zero toxic browsing or porn triggers|Study 2h deep focus|Target 8 hours quality sleep",
                "Review habit tracker: celebrate zero zero-days!|30m invigorating fitness routine|2h focused intellectual endeavor|Evening journal entry",
                "Challenge yourself to 3h peak study|Screen time under 2 hours today|35m physical exercise|Clean sleep environment",
                "Identify 3 long-term goals for the next 6 months|Complete all pre-built and custom tasks|30m workout|Reflect on emotional stability",
                "Notice the death of old bad habits|Stay true to your vision through social pressures|2.5h study session|Consistent sleep schedule",
                "Write down your personal Reboot 30 manifesto|Complete daily workout and 2h study|Screen time strictly limited to essentials|Evening reflection",
                "Final countdown: 100% task completion today!|Reflect on the contrast between Day 1 and today|30m workout with gratitude|Deep rest tonight",
                "Day 29 Mastery: You are no longer who you were|All habits locked in seamlessly|2.5h study excellence|Review your 30-day analytics",
                "DAY 30 VICTORY! You Rebooted your life!|Celebrate your incredible 30-day transformation|Export your journey backup & plan your next 30 days|Hold your head high with earned self-respect!"
            )

            for (i in 22..30) {
                list.add(
                    ChallengeDayEntity(
                        dayNumber = i,
                        phaseNumber = 4,
                        phaseName = "Phase 4: New Lifestyle",
                        title = "Day $i: " + when(i) {
                            22 -> "Autonomous Routine"
                            23 -> "Quiet Strength"
                            24 -> "The Momentum Engine"
                            25 -> "Peak Performance"
                            26 -> "Future Self Alignment"
                            27 -> "The Old Self Dissolves"
                            28 -> "The Discipline Manifesto"
                            29 -> "Eve of Victory"
                            else -> "Reboot 30 Ascendancy"
                        },
                        theme = when(i) {
                            22 -> "Discipline has transformed into effortless routine."
                            23 -> "Carry inner peace and firm resolve."
                            24 -> "Enjoy the compound interest of consistency."
                            25 -> "Operating at higher energy and productivity."
                            26 -> "Connect current habits with your lifelong aspirations."
                            27 -> "Bad habits have lost their neurological power."
                            28 -> "Document the principles that made you victorious."
                            29 -> "Appreciate the grit and resilience you proved."
                            else -> "You crossed the 30-day finish line as a renewed person!"
                        },
                        goalsJson = p4Goals[i - 22]
                    )
                )
            }

            return list
        }
    }
}
