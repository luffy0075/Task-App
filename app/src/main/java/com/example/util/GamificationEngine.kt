package com.example.util

import com.example.data.local.entities.AchievementEntity
import com.example.data.local.entities.DailyQuestEntity
import com.example.data.local.entities.RewardItemEntity
import com.example.data.local.entities.WeeklyMissionEntity
import java.util.Calendar

data class UserLevelInfo(
    val level: Int,
    val title: String,
    val currentLevelXp: Int,
    val xpForNextLevel: Int,
    val progress: Float,
    val lifetimeXp: Int,
    val iconEmoji: String
)

object GamificationEngine {

    // Progressive XP curve calculation
    // Level 1: 0..99 (100 XP)
    // Level 2: 100..249 (150 XP)
    // Level 3: 250..449 (200 XP)
    // Formula: needed for level L = 100 + (L - 1) * 50
    fun calculateLevelInfo(lifetimeXp: Int): UserLevelInfo {
        var currentLevel = 1
        var remainingXp = maxOf(0, lifetimeXp)

        while (true) {
            val neededThisLevel = 100 + (currentLevel - 1) * 50
            if (remainingXp < neededThisLevel) {
                val progress = if (neededThisLevel > 0) remainingXp.toFloat() / neededThisLevel.toFloat() else 0f
                return UserLevelInfo(
                    level = currentLevel,
                    title = getTitleForLevel(currentLevel),
                    currentLevelXp = remainingXp,
                    xpForNextLevel = neededThisLevel,
                    progress = progress.coerceIn(0f, 1f),
                    lifetimeXp = lifetimeXp,
                    iconEmoji = getIconForLevel(currentLevel)
                )
            }
            remainingXp -= neededThisLevel
            currentLevel++
        }
    }

    fun getTitleForLevel(level: Int): String {
        return when (level) {
            1 -> "Beginner"
            2 -> "Initiate"
            3 -> "Disciplined"
            4 -> "Focused"
            5 -> "Resilient"
            6 -> "Warrior"
            7 -> "Elite"
            8 -> "Master"
            9 -> "Grandmaster"
            10 -> "Legend"
            11 -> "Mythic"
            12 -> "Transcendent"
            13 -> "Ascendant"
            14 -> "Apex Disciplinarian"
            15 -> "Immortal Titan"
            16 -> "Sovereign of Will I"
            17 -> "Sovereign of Will II"
            18 -> "Sovereign of Will III"
            19 -> "Universal Grandmaster"
            20 -> "Cosmic Disciplinarian"
            else -> "Zen Ascendant Tier ${level - 20}"
        }
    }

    fun getIconForLevel(level: Int): String {
        return when {
            level >= 20 -> "🌌"
            level >= 15 -> "👑"
            level >= 12 -> "✨"
            level >= 10 -> "🏆"
            level >= 8 -> "⚔️"
            level >= 6 -> "🛡️"
            level >= 4 -> "🎯"
            level >= 2 -> "🌱"
            else -> "🐣"
        }
    }

    // Default pre-populated achievements catalog
    fun getDefaultAchievements(): List<AchievementEntity> {
        return listOf(
            // DAILY ACHIEVEMENTS
            AchievementEntity(
                id = "ach_first_step",
                category = "DAILY",
                title = "First Step",
                description = "Complete your very first task and ignite your journey.",
                rarity = "COMMON",
                iconEmoji = "👣",
                targetValue = 1,
                xpReward = 50,
                orderIndex = 1
            ),
            AchievementEntity(
                id = "ach_early_bird",
                category = "DAILY",
                title = "Early Bird",
                description = "Complete a task before 8:00 AM.",
                rarity = "UNCOMMON",
                iconEmoji = "🌅",
                targetValue = 1,
                xpReward = 100,
                orderIndex = 2
            ),
            AchievementEntity(
                id = "ach_productive_morning",
                category = "DAILY",
                title = "Productive Morning",
                description = "Complete at least 3 tasks before 12:00 PM.",
                rarity = "RARE",
                iconEmoji = "☕",
                targetValue = 3,
                xpReward = 200,
                orderIndex = 3
            ),
            AchievementEntity(
                id = "ach_daily_discipline",
                category = "DAILY",
                title = "Daily Discipline",
                description = "Complete 100% of all required tasks in a day.",
                rarity = "COMMON",
                iconEmoji = "🎯",
                targetValue = 1,
                xpReward = 50,
                orderIndex = 4
            ),
            AchievementEntity(
                id = "ach_perfect_day",
                category = "DAILY",
                title = "Perfect Day",
                description = "Complete all scheduled tasks (required and optional) in a single day.",
                rarity = "RARE",
                iconEmoji = "⭐",
                targetValue = 1,
                xpReward = 200,
                orderIndex = 5
            ),
            AchievementEntity(
                id = "ach_night_owl",
                category = "DAILY",
                title = "Night Owl",
                description = "Complete a scheduled task after 10:00 PM.",
                rarity = "UNCOMMON",
                iconEmoji = "🦉",
                targetValue = 1,
                xpReward = 100,
                orderIndex = 6
            ),

            // STREAK ACHIEVEMENTS
            AchievementEntity(
                id = "ach_streak_1",
                category = "STREAK",
                title = "Spark of Will",
                description = "Achieve a 1-day unbroken discipline streak.",
                rarity = "COMMON",
                iconEmoji = "🔥",
                targetValue = 1,
                xpReward = 50,
                orderIndex = 10
            ),
            AchievementEntity(
                id = "ach_streak_3",
                category = "STREAK",
                title = "Ignited Momentum",
                description = "Hold a 3-day discipline streak.",
                rarity = "COMMON",
                iconEmoji = "⚡",
                targetValue = 3,
                xpReward = 75,
                orderIndex = 11
            ),
            AchievementEntity(
                id = "ach_streak_7",
                category = "STREAK",
                title = "Iron Week",
                description = "Maintain a 7-day unbroken discipline streak.",
                rarity = "UNCOMMON",
                iconEmoji = "🛡️",
                targetValue = 7,
                xpReward = 150,
                orderIndex = 12
            ),
            AchievementEntity(
                id = "ach_streak_14",
                category = "STREAK",
                title = "Two Weeks of Steel",
                description = "Achieve a 14-day discipline streak.",
                rarity = "RARE",
                iconEmoji = "⚔️",
                targetValue = 14,
                xpReward = 300,
                orderIndex = 13
            ),
            AchievementEntity(
                id = "ach_streak_30",
                category = "STREAK",
                title = "Dopamine Rewired",
                description = "Conquer a 30-day discipline streak.",
                rarity = "EPIC",
                iconEmoji = "🧠",
                targetValue = 30,
                xpReward = 600,
                orderIndex = 14
            ),
            AchievementEntity(
                id = "ach_streak_60",
                category = "STREAK",
                title = "Monk Mode Master",
                description = "Maintain 60 unbroken days of discipline.",
                rarity = "EPIC",
                iconEmoji = "🧘",
                targetValue = 60,
                xpReward = 900,
                orderIndex = 15
            ),
            AchievementEntity(
                id = "ach_streak_100",
                category = "STREAK",
                title = "Centurion of Steel",
                description = "Reach a legendary 100-day discipline streak.",
                rarity = "LEGENDARY",
                iconEmoji = "🏛️",
                targetValue = 100,
                xpReward = 1500,
                orderIndex = 16
            ),
            AchievementEntity(
                id = "ach_streak_180",
                category = "STREAK",
                title = "Half-Year Sovereign",
                description = "Unbroken discipline for 180 continuous days.",
                rarity = "LEGENDARY",
                iconEmoji = "👑",
                targetValue = 180,
                xpReward = 2000,
                orderIndex = 17
            ),
            AchievementEntity(
                id = "ach_streak_365",
                category = "STREAK",
                title = "Year of Ascendancy",
                description = "Complete 365 days of relentless self-mastery.",
                rarity = "MYTHIC",
                iconEmoji = "🌌",
                targetValue = 365,
                xpReward = 3500,
                orderIndex = 18
            ),

            // TASK ACHIEVEMENTS
            AchievementEntity(
                id = "ach_tasks_10",
                category = "TASKS",
                title = "Tenacious Ten",
                description = "Complete 10 total tasks across your journey.",
                rarity = "COMMON",
                iconEmoji = "🌱",
                targetValue = 10,
                xpReward = 50,
                orderIndex = 20
            ),
            AchievementEntity(
                id = "ach_tasks_50",
                category = "TASKS",
                title = "Half-Century Action",
                description = "Complete 50 lifetime tasks.",
                rarity = "UNCOMMON",
                iconEmoji = "🌿",
                targetValue = 50,
                xpReward = 100,
                orderIndex = 21
            ),
            AchievementEntity(
                id = "ach_tasks_100",
                category = "TASKS",
                title = "Centurion Worker",
                description = "Execute 100 total completed tasks.",
                rarity = "RARE",
                iconEmoji = "🌳",
                targetValue = 100,
                xpReward = 250,
                orderIndex = 22
            ),
            AchievementEntity(
                id = "ach_tasks_500",
                category = "TASKS",
                title = "Master of Execution",
                description = "Complete 500 lifetime tasks.",
                rarity = "EPIC",
                iconEmoji = "⚡",
                targetValue = 500,
                xpReward = 750,
                orderIndex = 23
            ),
            AchievementEntity(
                id = "ach_tasks_1000",
                category = "TASKS",
                title = "Grand Executioner",
                description = "Conquer 1,000 completed lifetime tasks.",
                rarity = "MYTHIC",
                iconEmoji = "💎",
                targetValue = 1000,
                xpReward = 2000,
                orderIndex = 24
            ),
            AchievementEntity(
                id = "ach_polymath",
                category = "TASKS",
                title = "Polymath",
                description = "Complete tasks from at least 4 distinct categories.",
                rarity = "RARE",
                iconEmoji = "🎨",
                targetValue = 4,
                xpReward = 200,
                orderIndex = 25
            ),

            // CONSISTENCY ACHIEVEMENTS
            AchievementEntity(
                id = "ach_study_master",
                category = "CONSISTENCY",
                title = "Scholar's Focus",
                description = "Complete 10 Study & Learning tasks.",
                rarity = "UNCOMMON",
                iconEmoji = "📚",
                targetValue = 10,
                xpReward = 150,
                orderIndex = 30
            ),
            AchievementEntity(
                id = "ach_iron_body",
                category = "CONSISTENCY",
                title = "Physical Temple",
                description = "Complete 10 Fitness & Health tasks.",
                rarity = "UNCOMMON",
                iconEmoji = "💪",
                targetValue = 10,
                xpReward = 150,
                orderIndex = 31
            ),
            AchievementEntity(
                id = "ach_digital_freedom",
                category = "CONSISTENCY",
                title = "Digital Freedom",
                description = "Complete 10 Digital Detox tasks.",
                rarity = "RARE",
                iconEmoji = "📵",
                targetValue = 10,
                xpReward = 200,
                orderIndex = 32
            ),
            AchievementEntity(
                id = "ach_iron_resolve",
                category = "CONSISTENCY",
                title = "Iron Resolve",
                description = "Complete 10 Self-Control tasks.",
                rarity = "RARE",
                iconEmoji = "🧘‍♂️",
                targetValue = 10,
                xpReward = 200,
                orderIndex = 33
            ),

            // PERSONAL GROWTH ACHIEVEMENTS
            AchievementEntity(
                id = "ach_journal_keeper",
                category = "GROWTH",
                title = "Mindful Reflection",
                description = "Add personal notes or reflection on 5 completed tasks.",
                rarity = "UNCOMMON",
                iconEmoji = "📝",
                targetValue = 5,
                xpReward = 100,
                orderIndex = 40
            ),
            AchievementEntity(
                id = "ach_challenge_crusher",
                category = "GROWTH",
                title = "Goal Crusher",
                description = "Create and successfully complete a custom personal challenge.",
                rarity = "RARE",
                iconEmoji = "🚩",
                targetValue = 1,
                xpReward = 250,
                orderIndex = 41
            ),
            AchievementEntity(
                id = "ach_quest_master",
                category = "GROWTH",
                title = "Daily Quester",
                description = "Complete and claim 5 daily quests.",
                rarity = "UNCOMMON",
                iconEmoji = "📜",
                targetValue = 5,
                xpReward = 150,
                orderIndex = 42
            ),
            AchievementEntity(
                id = "ach_mission_elite",
                category = "GROWTH",
                title = "Mission Elite",
                description = "Complete and claim 3 weekly missions.",
                rarity = "RARE",
                iconEmoji = "🎖️",
                targetValue = 3,
                xpReward = 250,
                orderIndex = 43
            ),

            // HIDDEN ACHIEVEMENTS
            AchievementEntity(
                id = "ach_phoenix",
                category = "HIDDEN",
                title = "The Phoenix",
                description = "Rise from the ashes: start rebuilding your discipline after losing a streak.",
                rarity = "EPIC",
                iconEmoji = "🦅",
                targetValue = 1,
                xpReward = 500,
                isHidden = true,
                orderIndex = 50
            ),
            AchievementEntity(
                id = "ach_unbroken_spirit",
                category = "HIDDEN",
                title = "Unbroken Spirit",
                description = "Rebuild a 3-day streak after experiencing a streak reset.",
                rarity = "LEGENDARY",
                iconEmoji = "🔥",
                targetValue = 3,
                xpReward = 800,
                isHidden = true,
                orderIndex = 51
            ),
            AchievementEntity(
                id = "ach_comeback_titan",
                category = "HIDDEN",
                title = "Rise Again",
                description = "Complete 15 tasks after recovering from a streak reset.",
                rarity = "EPIC",
                iconEmoji = "⛰️",
                targetValue = 15,
                xpReward = 500,
                isHidden = true,
                orderIndex = 52
            ),
            AchievementEntity(
                id = "ach_dawn_warrior",
                category = "HIDDEN",
                title = "Dawn Warrior",
                description = "Complete a task before 6:30 AM before the rest of the world wakes up.",
                rarity = "RARE",
                iconEmoji = "🌄",
                targetValue = 1,
                xpReward = 250,
                isHidden = true,
                orderIndex = 53
            )
        )
    }

    // Default daily quests generator
    fun generateDailyQuestsForDate(date: String): List<DailyQuestEntity> {
        return listOf(
            DailyQuestEntity(
                date = date,
                questKey = "QUEST_EARLY_MOMENTUM",
                title = "Daily Momentum",
                description = "Complete at least 2 tasks today.",
                iconEmoji = "⚡",
                targetCount = 2,
                xpReward = 30
            ),
            DailyQuestEntity(
                date = date,
                questKey = "QUEST_REQUIRED_DISCIPLINE",
                title = "Streak Guardian",
                description = "Complete all required tasks scheduled for today.",
                iconEmoji = "🛡️",
                targetCount = 1,
                xpReward = 45
            ),
            DailyQuestEntity(
                date = date,
                questKey = "QUEST_DEEP_FOCUS",
                title = "Focus Pillar",
                description = "Complete at least 1 Study or Fitness task today.",
                iconEmoji = "🎯",
                targetCount = 1,
                xpReward = 35
            )
        )
    }

    // Default weekly missions generator
    fun generateWeeklyMissionsForWeek(week: String): List<WeeklyMissionEntity> {
        return listOf(
            WeeklyMissionEntity(
                weekIdentifier = week,
                missionKey = "MISSION_WEEKLY_CONSISTENCY",
                title = "7-Day Consistency",
                description = "Complete all required tasks on at least 5 days this week.",
                iconEmoji = "🔥",
                targetCount = 5,
                xpReward = 150
            ),
            WeeklyMissionEntity(
                weekIdentifier = week,
                missionKey = "MISSION_TASK_CENTURION",
                title = "Execution Machine",
                description = "Complete 15 total tasks throughout this week.",
                iconEmoji = "⚔️",
                targetCount = 15,
                xpReward = 120
            ),
            WeeklyMissionEntity(
                weekIdentifier = week,
                missionKey = "MISSION_MIND_BODY",
                title = "Mind & Body Mastery",
                description = "Complete at least 4 study and 4 fitness tasks this week.",
                iconEmoji = "🧘",
                targetCount = 8,
                xpReward = 160
            )
        )
    }

    // Reward Shop items catalog
    fun getDefaultRewardItems(): List<RewardItemEntity> {
        return listOf(
            // Profile Frames
            RewardItemEntity(
                id = "frame_classic",
                type = "PROFILE_FRAME",
                title = "Classic Minimalist",
                description = "Clean, refined border for your profile avatar.",
                iconEmoji = "🔘",
                costSparks = 0,
                previewColorHex = "#94A3B8",
                orderIndex = 1
            ),
            RewardItemEntity(
                id = "frame_neon_violet",
                type = "PROFILE_FRAME",
                title = "Neon Violet Glow",
                description = "Pulsing violet aura that shines on your avatar.",
                iconEmoji = "🟣",
                costSparks = 250,
                previewColorHex = "#8B5CF6",
                orderIndex = 2
            ),
            RewardItemEntity(
                id = "frame_emerald_aura",
                type = "PROFILE_FRAME",
                title = "Emerald Life Aura",
                description = "Lush botanical energy representing vital discipline.",
                iconEmoji = "🟢",
                costSparks = 400,
                previewColorHex = "#10B981",
                orderIndex = 3
            ),
            RewardItemEntity(
                id = "frame_obsidian_gold",
                type = "PROFILE_FRAME",
                title = "Obsidian Gold Armor",
                description = "Hardened obsidian frame edged in burnished gold.",
                iconEmoji = "🛡️",
                costSparks = 600,
                previewColorHex = "#F59E0B",
                orderIndex = 4
            ),
            RewardItemEntity(
                id = "frame_dragon_crest",
                type = "PROFILE_FRAME",
                title = "Mythic Dragon Crest",
                description = "Prismatic imperial crest reserved for apex warriors.",
                iconEmoji = "🐉",
                costSparks = 1000,
                previewColorHex = "#EC4899",
                orderIndex = 5
            ),

            // Flame Styles
            RewardItemEntity(
                id = "flame_classic",
                type = "FLAME_STYLE",
                title = "Classic Inferno",
                description = "The classic vibrant orange flame of discipline.",
                iconEmoji = "🔥",
                costSparks = 0,
                previewColorHex = "#FF6B00",
                orderIndex = 10
            ),
            RewardItemEntity(
                id = "flame_violet_inferno",
                type = "FLAME_STYLE",
                title = "Violet Cosmic Inferno",
                description = "Deep ultraviolet flare radiating psychic focus.",
                iconEmoji = "🔮",
                costSparks = 300,
                previewColorHex = "#A855F7",
                orderIndex = 11
            ),
            RewardItemEntity(
                id = "flame_cyan_plasma",
                type = "FLAME_STYLE",
                title = "Cyan Plasma Arc",
                description = "Electric hyper-focused plasma blaze.",
                iconEmoji = "⚡",
                costSparks = 350,
                previewColorHex = "#06B6D4",
                orderIndex = 12
            ),
            RewardItemEntity(
                id = "flame_emerald_mystic",
                type = "FLAME_STYLE",
                title = "Emerald Phoenix",
                description = "Mystical green fire signifying rejuvenation and resilience.",
                iconEmoji = "🍃",
                costSparks = 400,
                previewColorHex = "#059669",
                orderIndex = 13
            ),
            RewardItemEntity(
                id = "flame_golden_sun",
                type = "FLAME_STYLE",
                title = "Solar Flare Gold",
                description = "Radiant golden brilliance representing pure mastery.",
                iconEmoji = "☀️",
                costSparks = 600,
                previewColorHex = "#FBBF24",
                orderIndex = 14
            ),

            // Decorative Profile Titles
            RewardItemEntity(
                id = "title_unshakable",
                type = "PROFILE_TITLE",
                title = "The Unshakable",
                description = "Title: Calm and unmoved in the face of urges.",
                iconEmoji = "🗿",
                costSparks = 200,
                previewColorHex = "#8B5CF6",
                orderIndex = 20
            ),
            RewardItemEntity(
                id = "title_iron_will",
                type = "PROFILE_TITLE",
                title = "Iron Will",
                description = "Title: Possessor of an indomitable mindset.",
                iconEmoji = "⚔️",
                costSparks = 300,
                previewColorHex = "#F59E0B",
                orderIndex = 21
            ),
            RewardItemEntity(
                id = "title_zen_master",
                type = "PROFILE_TITLE",
                title = "Zen Master",
                description = "Title: Action without friction, discipline without suffering.",
                iconEmoji = "🎋",
                costSparks = 500,
                previewColorHex = "#10B981",
                orderIndex = 22
            ),
            RewardItemEntity(
                id = "title_apex_warrior",
                type = "PROFILE_TITLE",
                title = "Apex Disciplinarian",
                description = "Title: Top-tier master of daily habits and consistency.",
                iconEmoji = "👑",
                costSparks = 800,
                previewColorHex = "#EC4899",
                orderIndex = 23
            ),

            // Themes / Ambient Accents
            RewardItemEntity(
                id = "theme_cyberpunk",
                type = "THEME",
                title = "Cyberpunk Neon",
                description = "High-tech electric neon accents across cards and headers.",
                iconEmoji = "🌆",
                costSparks = 450,
                previewColorHex = "#06B6D4",
                orderIndex = 30
            ),
            RewardItemEntity(
                id = "theme_sakura",
                type = "THEME",
                title = "Sakura Blossom",
                description = "Soft blossom pink tones for mindful contemplation.",
                iconEmoji = "🌸",
                costSparks = 400,
                previewColorHex = "#F472B6",
                orderIndex = 31
            ),
            RewardItemEntity(
                id = "theme_crimson",
                type = "THEME",
                title = "Crimson Samurai",
                description = "Fiery ruby courage accentuating critical actions.",
                iconEmoji = "⛩️",
                costSparks = 450,
                previewColorHex = "#EF4444",
                orderIndex = 32
            )
        )
    }

    // Helper to get week identifier string: "YYYY-Www"
    fun getWeekIdentifier(calendar: Calendar = Calendar.getInstance()): String {
        val year = calendar.get(Calendar.YEAR)
        val week = calendar.get(Calendar.WEEK_OF_YEAR)
        return String.format(java.util.Locale.US, "%d-W%02d", year, week)
    }
}
